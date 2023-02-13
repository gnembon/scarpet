// shows the count of bees on that specific block
// right click while sneaking on a beehive or beenest
// By: Crec0

__config() -> { 'stay_loaded' -> true };

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (hand ~ 'mainhand' && item_tuple == null && player ~ 'sneaking' && (block ~ 'beehive' || block ~ 'bee_nest'),
        num_bees = length(parse_nbt(block_data(block):'Bees'));
        print(player, str('%s at %s contains %d bees', title(block), pos(block), num_bees));
    );
);