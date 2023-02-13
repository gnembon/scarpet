///
// Simply Harvest
// by Gnottero & BisUmTo
// (Carpet Mod 1.4.20)
//
// Allows the player to right-click on a crop to harvest it. The "Fortune" enchantment affects drops
///

__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

global_seeds = {
    'wheat' -> 'wheat_seeds',
    'potatoes' -> 'potato',
    'carrots' -> 'carrot',
    'beetroots' -> 'beetroot_seeds',
    'nether_wart' -> 'nether_wart'
};

// variable indicating that harvesting occurred
global_harvesting = false;

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if(hand=='offhand' || player~'gamemode_id' == 3, return());
    if(block_tags(block,'crops') || block == 'nether_wart',
        (
            crop_age = block_state(block, 'age');
            if(((crop_age == 7 && (block == 'wheat' || block == 'potatoes' || block == 'carrots')) || (crop_age == 3 && (block == 'beetroots' || block == 'nether_wart'))),
                (
                    can_destroy = item_tuple:2:'CanDestroy[]';
                    if(type(can_destroy) != 'list', can_destroy = [can_destroy]);
                    if(player~'gamemode_id' == 2 && can_destroy~('minecraft:' + block) == null, return());
                    harvest(player, pos(block));
                    if(player~'gamemode_id' == 1, set(pos(block), block, 'age', 0); return());
                    global_harvesting = true;
                    schedule(0,_(outer(block)) -> (
                            global_harvesting = false;
                            for(entity_area('item', pos(block), [2, 2, 2]),
                                if(query(_, 'age') <= 1 && query(_, 'item'):0 == global_seeds:str(block),
                                    nbt = query(_, 'nbt');
                                    nbt:'Item.Count' = nbt:'Item.Count' - 1;
                                    modify(_, 'nbt', nbt);
                                    set(pos(block), block, 'age', 0);
                                    return();
                                )
                            );
                        )
                    );
                    // reset hand slot to prevent using up blocks
                    // apparently mainhand can get checked right away
                    // harvesting was just set to true, no need to check
                    if(hand == 'mainhand',
                        // visually update inventory
                        inventory_set(player, slot = player~'selected_slot', inventory_get(player, slot):1);
                        // cancel the rest of the event to prevent the item from being used
                        return('cancel')
                    )
                );
            );
        );
    );
);

// reset hand slot to prevent using up blocks
// offhand requires this event for some reason
__on_player_placing_block(player, item_tuple, hand, block) ->
(
    // only applies while harvesting
    if(global_harvesting && hand == 'offhand',
        // visually update inventory
        inventory_set(player, slot = 40, inventory_get(player, slot):1);
        // cancel the rest of the event to prevent the item from being used
        return('cancel')
    );
);
