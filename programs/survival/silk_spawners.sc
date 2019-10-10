// Reimplementation of Silk Spawners mod in scarpet 1.5
// By "Pegasus Epsilon" <pegasus@pimpninjas.org>

// utility function _adjacent_block (block, face)
// returns the block next to the provided block, in the specified direction
_adjacent_block (b, f) -> (
        l(x, y, z) = pos(b);
        if (f == 'up', y = y + 1);
        if (f == 'down', y = y - 1);
        if (f == 'north', z = z - 1);
        if (f == 'south', z = z + 1);
        if (f == 'east', x = x + 1);
        if (f == 'west', x = x - 1);
        block(x, y, z)
);

// private __silk_spawner (player, block)
// returns: true if and only if
//  - player is holding a silk touch diamond pickaxe
//  - block is a spawner
__silk_spawner (player, block) -> (
        if (block != 'spawner', return());
        tool = player ~ 'holds';
        if (!tool || get(tool, 0) != 'diamond_pickaxe', return());
        nbt = get(tool, 2);
        if (!nbt, return());
        ench = get(nbt, 'Enchantments[]');
        if (!ench, return());
        if (type(ench) != 'list', ench = l(ench));
        for (ench, if (get(_, 'id') == 'minecraft:silk_touch', return(true)));
        false
);

// no drop instant mine any spawner with silk touch diamond pickaxe
// adds useful text to spawner tooltip, and enchant glow just because
__on_player_clicks_block (player, block, face) -> (
        if (!__silk_spawner(player, block), return());
        data = block_data(pos(block));
        type = title(get(split(':', data.'SpawnData.id'), 1));
		item_nbt = nbt('{Item:{id:"minecraft:spawner",Count:1b}}');
        spawner_nbt = nbt('{Enchantments:[{}],display:{Name:"{\\\"text\\\":\\\"' + type + ' Spawner\\\",\\\"italic\\\":\\\"false\\\",\\\"color\\\":\\\"aqua\\\"}"}}');
		put(spawner_nbt, 'BlockEntityTag', data);
		put(item_nbt,'Item.tag',spawner_nbt);
		spawner_item = spawn('item', pos(block), item_nbt);
		modify(spawner_item,'pickup_delay',10);
		destroy(block)
);

// unbreak BlockEntityData for spawners (security risk on creative servers)
// mojang intentionally disabled this for a reason, but we need it.
__on_player_right_clicks_block (player, item, hand, block, face, hitvec) -> (
        if (!item || item.0 != 'spawner', return());
		if (hand != 'mainhand', return());
        nbt = item.2;
        if (!nbt, return());
        data = nbt.'BlockEntityTag{}';
        if (!data, return());
        tgt = _adjacent_block(block, face);
        if (tgt != block('air'), return());
        l(x, y, z) = pos(tgt);
        if (!set(x, y, z, 'spawner'+data), return());
        // don't consume items in creative
        if (player ~ 'gamemode' != 'creative', destroy(tgt));
        slot = query(player, 'selected_slot');
        inventory_set(player, slot, get(inventory_get(player, slot), 1) - 1)
);
