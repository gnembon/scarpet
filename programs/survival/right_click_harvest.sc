//right click harvest for crops by Sir_gav

//to add support for other crops, add to this map with the name and the max age
global_crops = {'wheat' -> 7, 'carrots' -> 7, 'potatoes' -> 7, 'beetroots' -> 3, 'nether_wart' -> 3};

__config()-> {'stay_loaded' -> true};

__harvest_replant(player,block) -> (
    b_pos = pos(block);
    harvest(player,b_pos);
    set(b_pos,block,'age',1)
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec)-> (
    if(
        has(global_crops,str(block)),
        age = property(pos(block),'age');
        if(
            age == global_crops:str(block),
            __harvest_replant(player,block);
        );
    );
)