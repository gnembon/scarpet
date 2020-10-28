// Reimplementation of Angel Blocks from RandomThings mod in scarpet 1.4
// By "Pegasus Epsilon" <pegasus@pimpninjas.org>

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

// specify your preferred angel block here
global_angel_block = block('bedrock');

// utility function _find_space_for
// finds inventory space for one of the specified item
// returns list containing slot number and existing item count
_find_space_for (inv, item) -> (
        limit = stack_limit(item);
        size = inventory_size(inv);

        // do/while or do/until would be nice here...
        slot = inventory_find(inv, item);
        if (null == slot, return(l(inventory_find(inv, null), 0)));
        count = get(inventory_get(inv, slot), 1);
        while (count >= limit && null != slot && slot < size, size,
                slot = inventory_find(inv, item, slot + 1);
                if (null == slot, return(l(inventory_find(inv, null), 0)));
                count = get(inventory_get(inv, slot), 1)
        );

        if (null != slot, l(slot, count), l(inventory_find(inv, null), 0))
);

// utility function _add_stackable_to_inventory(inventory, item)
// adds a stackable item to an inventory.
// returns true if it succeeds
// FIXME/WARNING: will happily destroy metadata
_add_stackable_to_inventory (inv, item) -> (
        l(slot, count) = _find_space_for(inv, item);
        if (null != slot, inventory_set(inv, slot, count + 1, item); true)
);

// no drop instant mine angel blocks while holding angel blocks
// no-op in creative
__on_player_clicks_block (player, block, face) -> (
        if (player ~ 'gamemode' == 'creative', return());

        if (block != global_angel_block, return());
        tool = player ~ 'holds';
        if (!tool || get(tool, 0) != global_angel_block, return());
        data = block_data(pos(block));
        if (_add_stackable_to_inventory(player, global_angel_block), destroy(block))
);

// angel place angel blocks
__on_player_uses_item (player, item, hand) -> (
        if (!item || get(item, 0) != global_angel_block, return());
        l(x, y, z) = pos(player);
        f = player ~ 'facing';
        y += 1; // eye level please
        if (f == 'up', y = y + 1.25);
        if (f == 'down', y = y - 2);
        if (f == 'north', z = z - 1.25);
        if (f == 'south', z = z + 1.25);
        if (f == 'east', x = x + 1.25);
        if (f == 'west', x = x - 1.25);
        if (!set(x, y, z, global_angel_block), return());

        if (player ~ 'gamemode' == 'creative', return());
        slot = query(player, 'selected_slot');
        inventory_set(player, slot, get(inventory_get(player, slot), 1) - 1)
);
