
//keeps script loaded upon server start
__config() -> {
    'stay_loaded' -> true,
    'scope' -> 'global',
};

debug = false;
// Block Parameters
// <float> hardness
//      - this must be less than the default value currently or there's a good chance
//        that the drop functionality won't trigger
// <string or string list> tools
// <list> [item dropped if silk touch, item droped normal]
// save block nbt - currently works only with spawners;
//                  will require edits to functions __break_block and __on_player_places_block
//                  to properly support other nbt blocks
// sound to play when broken
global_blocks = {
    'bedrock' -> [75, 'netherite_pickaxe', [null, null], false, 'stone' ],
    'budding_amethyst' -> [1, ['diamond_pickaxe', 'netherite_pickaxe'], ['budding_amethyst', null], false, 'amethyst_block' ],
    'deepslate' -> [2, 'netherite_pickaxe', ['deepslate', 'cobbled_deepslate'], false, 'deepslate' ],
    'end_portal_frame' -> [25, ['diamond_pickaxe', 'netherite_pickaxe'], ['end_portal_frame', null], false, 'stone' ],
    'spawner' -> [1, ['diamond_pickaxe', 'netherite_pickaxe'], ['spawner', null], true, 'metal' ],
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

__check_tool (player, tools) -> (
    [tool, count, nbt] = player ~ 'holds' || ['None', 0, null];

    type(tools) !='list' && (tools = l(tools));
    for (tools,
    	str(tool) == _ && (valid_tool = true);
    );

    return(valid_tool);
);

__get_enchantment_level(tool_nbt, enchantment) -> (
    level = 0;
	if ((enchantments = get(tool_nbt,'Enchantments[]')),
		if (type(enchantments)!='list', enchantments = l(enchantments));
		for (enchantments,
			if ( get(_,'id') == 'minecraft:'+enchantment,
				level = max(level, get(_,'lvl'))
			)
		)
	);
	level
);

__calculate_step (player, hardness) -> (

    [tool, count, nbt] = player ~ 'holds' || ['None', 0, null];

    speedMultiplier = global_tool_speeds:str(tool);

    efficiency_level = __get_enchantment_level(nbt, 'efficiency');
    efficiency_level && (
        speedMultiplier = speedMultiplier + 1 + (efficiency_level * efficiency_level);
    );

    haste_level = query(player,'effect','haste'):0;
    haste_level && (
        speedMultiplier = speedMultiplier * (1 + (0.2 * (haste_level + 1)))
    );

    mining_fatigue_level = query(player,'effect','mining_fatigue'):0 || -1;
    if (mining_fatigue_level == 0,
        (
            speedMultiplier = speedMultiplier * 0.3;
        ),
        // else if
        mining_fatigue_level == 1,
        (
            speedMultiplier = speedMultiplier * 0.09;
        ),
        // else if
        mining_fatigue_level == 2,
        (
            speedMultiplier = speedMultiplier * 0.0027;
        ),
        // else if
        mining_fatigue_level >= 3,
        (
            speedMultiplier = speedMultiplier * 0.00081;
        )
    );

    aqua_affinity = bool(query(player, 'holds', 'head'):2 ~ 'aqua_affinity');
    query(player, 'swimming') && !aqua_affinity && (
        speedMultiplier = speedMultiplier * 0.2;
    );

    !query(player, 'on_ground') && (
        speedMultiplier = speedMultiplier * 0.2;
    );

    block_damage = speedMultiplier / hardness;
    block_damage = block_damage / 30;
    break_ticks = ceil(1 / block_damage);

    break_time = break_ticks / 20;
    debug && print(break_time);

    if( break_time <= 0.08,
        step = 0,
        step = break_ticks / 10;
    );

    return(step);
);


__break_block(player, block_pos, block_name) -> (

    [hardness, tools, drop_block_list, save_nbt, break_sound] = global_blocks:block_name;
    [tool, count, tool_nbt] = player ~ 'holds' || ['None', 0, null];

    drop_block = get(drop_block_list, 1);

    silk_touch = __get_enchantment_level(tool_nbt, 'silk_touch');
    silk_touch && (
        drop_block = get(drop_block_list, 0);
    );

    drop_block && (

        item_nbt = nbt(str('{Item:{id:"minecraft:%s",Count:1b}}',drop_block));
        save_nbt && (
            block_entity_data = block_data(block_pos);
            put(item_nbt, 'Item.tag.BlockEntityTag', block_entity_data);
            block_name == 'spawner' && (
                spawner_type = title(get(split(':', block_entity_data:'SpawnData.entity.id'), 1));
        		put(item_nbt,'Item.tag.Enchantments','[{}]');
        		put(item_nbt,'Item.tag.display.Name', str('\'[{"text":"%s Spawner","italic":false}]\'', spawner_type));
        		put(item_nbt,'Item.tag.BlockEntityTag.Delay', '5s');
            );
        );

        put(item_nbt,'PickupDelay','10');
        put(item_nbt,'Motion',str('[%.1f,%.1f,%.1f]', str('%.1f',rand(0.1)), str('%.1f',rand(0.1)+0.1), str('%.1f',rand(0.1))));
        spawn('item', block_pos + [0.5,0.5,0.5], item_nbt);
    );
    break_sound && sound(str('minecraft:block.%s.break',break_sound), block_pos,1.0,1.0,'block');
    set(block_pos, 'air');

    slot = player~'selected_slot';

    unbreaking_chance = 100.0 / (__get_enchantment_level(tool_nbt,'unbreaking') + 1);
    tool_damage = get(tool_nbt,'Damage') || 0;

    unbreaking_chance > rand(100.0) && (
        put(tool_nbt,'Damage',tool_damage + 1);
        inventory_set(player, slot, count, tool, tool_nbt);
    );
);

__on_player_clicks_block(player, block, face) ->
(
    block_name = str(block);
    block_pos = pos(block);

    [hardness, tools, drop_block_list, save_nbt, break_sound] = global_blocks:block_name || [9999, null, null, false, null];

    valid_tool = __check_tool(player, tools);

    valid_tool && hardness != 9999 && (
        step = __calculate_step(player, hardness);
        if (step == 0, // instamine
            (
                __break_block(player, block_pos, block_name)
            ),
            (
                schedule(step, '_break', player, block_pos, block_name, step, 1);
            )
        );
    );
);

_break(player, block_pos, block_name, step, lvl) ->
(
    active_block = player~'active_block';
    if (active_block != block_name || pos(active_block) != block_pos,
        (
            modify(player, 'breaking_progress', null);
        ),
        (
            modify(player, 'breaking_progress', lvl);
            if(lvl >= 10,
                (
                    __break_block(player, block_pos, block_name);
                ),
                (
                    schedule(step, '_break', player, block_pos, block_name, step, lvl+1);
                )
            )
        )
    );
);

// unbreak BlockEntityData for spawners (security risk on creative servers)
// mojang intentionally disabled this for a reason, but we need it.
__on_player_places_block (player, item, hand, block) ->
(
    item:0 == 'spawner' && (
        block_entity_data = item:2:'BlockEntityTag{}';
        block_entity_data && (
            set(pos(block), 'spawner'+block_entity_data);
        );
    );
);
