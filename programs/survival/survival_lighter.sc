__on_player_uses_item(player, item_tuple, hand)->(
	l(item,count,nbt)=item_tuple;
	if(hand=='mainhand'&&item == 'torch'&&nbt==null,
		print('Started placing torches ... ');//You can turn off feedback
		l(cx,cy,cz)=pos(player);
		scan(cx,cy,cz,128,128,128,
			if(_x*_x+_y*_y+_z*_z>128*128,continue);
			if(inventory_find(player,'torch')==null,return(print('You ran out of torches')));
			lpos=pos(_);
			if(air(lpos)&&air(lpos+l(0,1,0))&&block_light(lpos)<3&&solid(pos_offset(lpos, 'down')),//&&!rand(10), optional
					set(lpos,'torch');
					if(player~'gamemode'=='survival',inventory_remove(player, 'torch'));
					loop(4,game_tick(50))
			);
		);
		print('Done')//You can turn off feedback
	)
);
