// Conversion of Silk Spwaners; Implemented by Scott Gudeman (DragonCrafted87)
// See https://github.com/gnembon/scarpet/blob/master/programs/survival/silk_spawners.sc for Original Basis

//keeps script loaded upon server start
__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

// private __silk_budding_amethyst (player, block)
// returns: true if and only if
//  - player is holding a silk touch diamond or netherite pickaxe
//  - block is a budding_amethyst
__silk_budding_amethyst (player, block) -> (
    if(
        block == 'budding_amethyst',
        (
            holds = player ~ 'holds';;
            if(
                holds,
                [tool, count, nbt] = holds;
                if(
                    (
                        tool ~ 'diamond_pickaxe' ||
                        tool ~ 'netherite_pickaxe'
                    ) &&
                    nbt ~ 'minecraft:silk_touch',
                    (
                        return(true);
                    )
                )
            )
        ),
        return(false);
    )
);

__on_player_breaks_block (player, block) -> (
        if (!__silk_budding_amethyst(player, block), return());
        data = block_data(pos(block));
		item_nbt = nbt('{Item:{id:"minecraft:budding_amethyst",Count:1b,PickupDelay:10}}');
		budding_amethyst_item = spawn('item', pos(block) + [0.5,0.5,0.5], item_nbt);
);
