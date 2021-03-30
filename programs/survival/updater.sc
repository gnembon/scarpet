run( str( 'give %s minecraft:bamboo{display:{Name:\'{"text":"Updater"}\',Lore:[\'{"text":"Any bamboo wil do"}\']},Enchantments:[{id:"minecraft:mending",lvl:1s}]} 1' , player() ));

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
	if(item_tuple:0 == 'bamboo',
		update(block);
		if(block == 'observer',
			offset_pos = pos_offset(pos(block), face, 1);
			current_block = block(offset_pos);
			set( offset_pos, 'barrier');
			set( offset_pos, current_block);
		)
	)
);