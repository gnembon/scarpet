// Conversion of Silk Spwaners; Implemented by Scott Gudeman (DragonCrafted87)
// See https://github.com/gnembon/scarpet/blob/master/programs/survival/silk_spawners.sc for Original Basis

//keeps script loaded upon server start
__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

// private __silk_budding_amethyst (player, block)
// returns: true if and only if
//  - player is holding a silk touch diamond or netherite pickaxe
//  - block is a budding_amethyst
__silk_budding_amethyst (player, block) -> (
    [tool, count, nbt] = player ~ 'holds';
    block=='budding_amethyst' &&
    tool ~ ['diamond_pickaxe', 'netherite_pickaxe'] &&
    nbt ~ 'silk_touch';
);



__on_player_breaks_block (player, block) -> (
    if (
        __silk_budding_amethyst(player, block),
        (
            item_nbt = nbt('{Item:{id:"minecraft:budding_amethyst",Count:1b,PickupDelay:10}}');
            budding_amethyst_item = spawn('item', pos(block) + [0.5,0.5,0.5], item_nbt);
        )
    )
);
