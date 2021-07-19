
// hardness, tools, drop block, require silktouch

global_blocks = {
    'spawner' -> [5, ['diamond_pickaxe', 'netherite_pickaxe'], true, true ],
    'budding_amethyst' -> [1.5, ['diamond_pickaxe', 'netherite_pickaxe'], true, true ],
    'end_portal_frame' -> [25, ['diamond_pickaxe', 'netherite_pickaxe'], true, false ],
    'bedrock' -> [50, 'netherite_pickaxe', false, false ],
};

global_tool_speeds = {
'wooden_axe' -> 2,
'stone_axe' -> 4,
'iron_axe' -> 6,
'diamond_axe' -> 8,
'netherite_axe' -> 9,
'golden_axe' -> 12,
'wooden_pickaxe' -> 2,
'stone_pickaxe' -> 4,
'iron_pickaxe' -> 6,
'diamond_pickaxe' -> 8,
'netherite_pickaxe' -> 9,
'golden_pickaxe' -> 12,
'wooden_shovel' -> 2,
'stone_shovel' -> 4,
'iron_shovel' -> 6,
'diamond_shovel' -> 8,
'netherite_shovel' -> 9,
'golden_shovel' -> 12,
'wooden_hoe' -> 2,
'stone_hoe' -> 4,
'iron_hoe' -> 6,
'diamond_hoe' -> 8,
'netherite_hoe' -> 9,
'golden_hoe' -> 12,
};

__check_tool (player, tools, requires_silk) -> (
    [tool, count, nbt] = player ~ 'holds' || ['None', 0, null];

    if (type(tools)!='list', tools = l(tools));
    for (tools,
        if ( str(tool) == _,
            valid_tool = true;
        );
    );

    silk_touch = bool(nbt ~ 'silk_touch') || !requires_silk;

    return(valid_tool && silk_touch);
);


__calculate_step (player, hardness) -> (

    [tool, count, nbt] = player ~ 'holds' || ['None', 0, null];

    base_speed = global_tool_speeds:str(tool);

    if (  (enchs = get(nbt,'Enchantments[]')),
        if (type(enchs)!='list', enchs = l(enchs));
        for (enchs,
            if ( get(_,'id') == 'minecraft:efficiency',
                efficiency_level = max(lvl, get(_,'lvl'))
            )
        )
    );
    efficiency_speed = efficiency_level && 1 + ((efficiency_level + 1) * (efficiency_level + 1));

    haste_level = query(player,'effect','haste'):0;
    haste_speed = haste_level && 20 * (haste_level + 1);


    block_damage = ((base_speed + efficiency_speed + haste_speed) / hardness) /30;
    break_time = ceil(1 / block_damage) / 20;

    if( break_time <= 0.08,
        step = 0,
        step = break_time / 10;
        );

    return(step);
);

__on_player_clicks_block(player, block, face) ->
(
    if (global_blocks:str(block), [hardness, tools, drop_block, requires_silk] = global_blocks:str(block));

    valid_tool = __check_tool(player, tools, requires_silk);

    if (!valid_tool || !hardness, return);

    step = __calculate_step(player, hardness);

   if (valid_tool && step == 0, // instamine
        if (drop_block,
            destroy(pos(block), -1),
            set(pos(block), 'air');
    ),
      valid_tool,
      schedule(step, '_break', player, pos(block), str(block), step, 1, drop_block);
   )
);

_break(player, pos, name, step, lvl, drop_block) ->
(
   current = player~'active_block';
   if (current != name || pos(current) != pos,
      modify(player, 'breaking_progress', null);
   ,
      modify(player, 'breaking_progress', lvl);
      if (lvl >= 10,
        if (drop_block,
            destroy(pos, -1),
            set(pos, 'air');
    ));
      schedule(step, '_break', player, pos, name, step, lvl+1, drop_block)
   );
)
