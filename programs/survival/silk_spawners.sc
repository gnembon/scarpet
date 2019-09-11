// Reimplementation of Silk Spawners mod in scarpet 1.4
// By "Pegasus Epsilon" <pegasus@pimpninjas.org>

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

// we *need* dicts or hashes or something
_adjacent_block (block, face) -> (
        l(x, y, z) = pos(block);
        if (face == 'up', y = y + 1);
        if (face == 'down', y = y - 1);
        if (face == 'north', z = z - 1);
        if (face == 'south', z = z + 1);
        if (face == 'east', x = x + 1);
        if (face == 'west', x = x - 1);
        block(x, y, z)
);

// unbreak BlockEntityData for spawners (security risk on creative servers)
__on_player_right_clicks_block (player, item, hand, block, face, hitvec) -> (
        if (!item || get(item, 0) != 'spawner', return());
        nbt = get(item, 2);
        if (!nbt, return());
        data = get(nbt, 'BlockEntityTag{}');
        if (!data, return());
        tgt = _adjacent_block(block, face);
        if (tgt != block('air'), return());
        slot = query(player, 'selected_slot');
        inventory_set(player, slot, get(inventory_get(player, slot), 1) - 1);
        l(x, y, z) = tgt;
        set(x, y, z, 'spawner'+data)
);

global_lowercase = split('', 'abcdefghijklmnopqrstuvwxyz');
global_uppercase = split('', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ');
_remap (c, s, d) -> (
        i = first(range(0, length(s)), c == get(s, _));
        if (!i, return(c));
        get(d, i)
);
_uc_first (s) -> (_remap(slice(s, 0, 1), global_lowercase, global_uppercase) + slice(s, 1));

__on_player_clicks_block (player, block, face) -> (
        if (__silk_spawner(player, block),
                data = block_data(pos(block));
                type = _uc_first(get(split(':', get(data, 'SpawnData.id')), 1));
                frill = 'Enchantments:[{}],display:{Name:"{\\\"text\\\":\\\"' + type + ' Spawner\\\",\\\"italic\\\":\\\"false\\\",\\\"color\\\":\\\"aqua\\\"}"},';
                inventory_set(player, inventory_find(player, null, 0), 1, 'spawner', '{' + frill + 'BlockEntityTag:' + data + '}');
                destroy(block)
        )
);
