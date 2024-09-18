// Reimplementation of Silk Spawners mod in scarpet 1.5
// By "Pegasus Epsilon" <pegasus@pimpninjas.org>
// 1.16 update by Jackson Harada
// 1.21 update by vlad2305m

//keeps script loaded upon server start
__config() -> (m(
  l('stay_loaded','true')
));

// private __silk_spawner (player, block)
// returns: true if and only if
//  - player is holding a silk touch diamond or netherite pickaxe
//  - block is a spawner
__silk_spawner (player, block) -> (
        if (block != 'spawner', return());
        tool = player ~ 'holds';
        if (!tool || get(tool, 0) != 'diamond_pickaxe', if(!tool || get(tool, 0) != 'netherite_pickaxe', return()));
        nbt = get(tool, 2);
        if (!nbt, return());
        ench = get(nbt, 'components');
        ench = get(ench, 'minecraft:enchantments');
        if (!ench, return());
        ench = get(ench, 'levels');
        if (get(ench, 'minecraft:silk_touch'), return(true));
        false
);

// no drop instant mine any spawner with silk touch diamond or netherite pickaxe
// adds useful text to spawner tooltip, and enchant glow just because
__on_player_clicks_block (player, block, face) -> (
        if (!__silk_spawner(player, block), return());
        data = block_data(pos(block));
        type = title(get(split(':', data:'SpawnData.entity.id'), 1));
		item_nbt = nbt('{Item:{id:"minecraft:spawner",count:1b}}');
        spawner_nbt = nbt('{"minecraft:enchantment_glint_override":1b,"minecraft:custom_name":\'{"color":"aqua","italic":false,"text":"'+type+' Spawner"}\'}');
        put(data, 'id', 'minecraft:spawner');
		put(spawner_nbt, 'minecraft:block_entity_data', data);
		put(item_nbt,'Item.components',spawner_nbt);
		spawner_item = spawn('item', pos(block), item_nbt);
		modify(spawner_item,'pickup_delay',10);
		destroy(block)
);

// unbreak BlockEntityData for spawners (security risk on creative servers)
// mojang intentionally disabled this for a reason, but we need it.
__on_player_right_clicks_block (player, item, hand, block, face, hitvec) -> (
        if (item:0 != 'spawner', return());
		if (hand != 'mainhand', return());
        data = item:2:'components."minecraft:block_entity_data"{}';
        if (!data, return());
        tgt = block(pos_offset(block, face));
        if (tgt != block('air'), return());
        l(x, y, z) = pos(tgt);
        if (!set(x, y, z, 'spawner'+data), return());
        // don't consume items in creative
        if (player ~ 'gamemode' != 'creative', destroy(tgt));
        slot = query(player, 'selected_slot');
        inventory_set(player, slot, get(inventory_get(player, slot), 1) - 1)
);
