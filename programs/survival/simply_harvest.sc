__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if(hand=='offhand', return());
    if(block_tags(block,'crops') || block == 'nether_wart',
        (
            crop_age = block_state(block, 'age');
            if((crop_age == 7 && (block == 'wheat' || block == 'potatoes' || block == 'carrots')) || (crop_age == 3 && (block == 'beetroots' || block == 'nether_wart')),
                (
                    harvest(player, pos(block));
                    set(pos(block), block, 'age', 0);
                );
                else,
                    return();
            );
        );
        else,
            return();
    );
);