///
// Milkable Sheeps
// by Gnottero
// (Carpet Mod 1.4.20)
//
// Allows the player to milk sheeps using a bucket.
///

__config() -> {'stay_loaded' -> true};

global_ticked = {};

__on_player_interacts_with_entity(player, entity, hand) -> (
    if(query(entity, 'type') != 'sheep', return());
    if(query(player, 'holds', hand):0 == 'bucket' && !has(global_ticked, player), 
        (
            milk_sheep(player, hand);
            global_ticked:player = null;
            schedule(0,_() -> global_ticked = {});
        );
    );
);

milk_sheep(player, hand) -> (
    [item, count, data] = query(player, 'holds', hand);
    hand_id = if(hand == 'offhand', 40, hand == 'mainhand', player~'selected_slot');
    print(hand_id);
    if(item == 'bucket',
        (
            if((empty_slot = inventory_find(player, null)) >= 36, empty_slot = null);
            if(query(player, 'gamemode_id') == 0,
                (
                    inventory_set(player, hand_id, count-1, item, data);
                    if(empty_slot != null,
                        (
                            inventory_set(player, empty_slot, 1, 'milk_bucket');
                        );
                        else,
                            spawn('item', pos(player), str('{Item:{id:"minecraft:milk_bucket",Count:1b}}'));
                    );
                ),
                query(player, 'gamemode_id') == 1 && inventory_find(player, 'milk_bucket') == null,
                    if(empty_slot != null,
                        (
                            inventory_set(player, empty_slot, 1, 'milk_bucket');
                        )
                    );
            );
            sound('entity.cow.milk', pos(player));
        );
    );
);
