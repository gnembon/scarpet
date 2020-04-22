__on_player_uses_item(player, item_tuple, hand)->(
	l(item,count,nbt)=item_tuple;
	if(hand=='mainhand'&&item == 'torch'&&nbt==null,
		print('Started placing torches ... ');//You can turn off feedback
		l(cx,cy,cz)=pos(player);
		spots=0;
		scan(cx,cy,cz,128,128,128,
			lpos=l(_x,_y,_z);
			if(_x*_x+_y*_y+_z*_z>128*128,continue);
			if(inventory_find(player,'torch')==null,return(print('You ran out of torches')));
			if(air(lpos)&&air(lpos+l(0,1,0))&&block_light(lpos)<3&&solid(pos_offset(lpos, 'down')),//&&!rand(10), optional
					set(lpos,'torch');
					block_tick(lpos);//updates lightlevel accordingly
					spots+=1;
					if(player~'gamemode'=='survival',inventory_remove(player, 'torch'));
					loop(4,game_tick(50))
			);
		);
		scan(cx,cy,cz,128,128,128,
			lpos=l(_x,_y,_z);
			if(_x*_x+_y*_y+_z*_z>128*128,continue);
			if(inventory_find(player,'torch')==null,return(print('You ran out of torches')));
			if(air(lpos)&&air(lpos+l(0,1,0))&&block_light(lpos)<8&&solid(pos_offset(lpos, 'down')),//&&!rand(10), optional
					set(lpos,'torch');
					block_tick(lpos);//updates lightlevel accordingly
					spots+=1;
					if(player~'gamemode'=='survival',inventory_remove(player, 'torch'));
					loop(4,game_tick(50))
			);
		);
		print(spots+' torches placed');
		print('Done')//You can turn off feedback
	)
);
