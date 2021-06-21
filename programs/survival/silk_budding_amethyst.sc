// Conversion of Silk Spwaners; Implemented by Scott Gudeman (DragonCrafted87)
// See https://github.com/gnembon/scarpet/blob/master/programs/survival/silk_spawners.sc for Original Basis

//keeps script loaded upon server start
__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

// private __silk_budding_amethyst (player, block)
// returns: true if and only if
//  - player is holding a silk touch diamond or netherite pickaxe
//  - block is a budding_amethyst
__silk_budding_amethyst (player, block) -> (
        if (block != 'budding_amethyst', return());
        tool = player ~ 'holds';
        if ((!tool || (get(tool, 0) != 'diamond_pickaxe' && get(tool, 0) != 'netherite_pickaxe')), return());
        nbt = get(tool, 2);
        if (!nbt, return());
        ench = get(nbt, 'Enchantments[]');
        if (!ench, return());
        if (type(ench) != 'list', ench = l(ench));
        for (ench, if (get(_, 'id') == 'minecraft:silk_touch', return(true)));
        false
);

__on_player_clicks_block (player, block, face) -> (
        if (!__silk_budding_amethyst(player, block), return());
        data = block_data(pos(block));
		item_nbt = nbt('{Item:{id:"minecraft:budding_amethyst",Count:1b}}');
		budding_amethyst_item = spawn('item', pos(block), item_nbt);
		modify(budding_amethyst_item,'pickup_delay',10);
		destroy(block)
);
