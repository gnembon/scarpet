// 
// by Gnottero
// (Carpet Mod 1.4.169)
// Allows the player to harvest crops by right-clicking (setting the age of the crop back to zero)
// Compatible with minecraft 1.21 and with any crop + nether warts
//

__config() -> {
    'stay_loaded' -> true
};


// [Begin] Scarpet Events

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (hand != 'mainhand', return());
    if (!canHarvest(player, item_tuple, hand, block), return());
    age = block_state(block, 'age');
    try(
        block_state(str('%s[age=%d]', block, number(age) + 1)), 
        'unknown_block',
        harvestCrop(player, item_tuple, hand, block);
    );
);

// [End] Scarpet Events


// [Begin] Custom Functions

canHarvest(player, item_tuple, hand, block) -> (
    
    if (query(player, 'gamemode_id') == 3, return(false));
    if (block_tags(block)~'crops' == null && block != 'nether_wart', return(false));
    if (query(player, 'gamemode_id') == 2,
        (
            canBreak = parse_nbt(item_tuple:2:'components'):'minecraft:can_break';
            if (type(canBreak) != null,
                blockList = getBreakableBlocks(canBreak);
                return(blockList~block != null);
            );
            return(false);
        ),
        return(true)
    );
);


harvestCrop(player, item_tuple, hand, block) -> (
    harvest(player, pos(block));
    set(pos(block), block, 'age', 0);
    modify(player, 'swing', hand);
);


getBreakableBlocks(components) -> (
    blockList = [];
    if (type(components) == 'list',
        (
            for (components,
                if (_:'blocks'~'#minecraft:',
                    reduce(block_list(replace(_:'blocks', '#', '')), blockList += _, 0),
                    blockList += replace(_:'blocks', 'minecraft:', '');
                );
            )
        ),
        (
            if (components:'blocks'~'#minecraft:',
                reduce(block_list(replace(components:'blocks', '#', '')), blockList += _, 0),
                blockList += replace(components:'blocks', 'minecraft:', '');
            );
        )
    );    
    return(blockList)
);

// [End] Custom Functions