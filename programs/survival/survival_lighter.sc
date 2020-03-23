__on_player_uses_item(player, item_tuple, hand)->(
	l(item,count,nbt)=item_tuple;
	if(hand=='mainhand'&&item == 'torch'&&nbt==null,
		print('Started placing torches ... ');//You can turn off feedback
		l(cx,cy,cz)=pos(player);
		scan(cx,cy,cz,128,128,128,
			if(!inventory_find(player,'torch'),return(print('You ran out of torches')));//it gives weird error messages in surival for some reason, but works otherwise.
			lpos=pos(_);
			if((block(lpos)=='air'||block(lpos)=='cave_air')&&light(lpos)<8&&solid(pos_offset(lpos, 'down')),//&&!rand(10), optional
					set(lpos,'torch');
					if(player~'gamemode'=='survival',inventory_remove(player, 'torch'));
					game_tick()
			);
		);
		print('Done')//You can turn off feedback
	)
);
