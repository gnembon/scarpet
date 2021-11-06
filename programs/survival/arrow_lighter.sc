//holding a torch in your left hand while firing an arrow will try to set a torch where that arrow lands. 

__config() -> 
(
	m(
		l('stay_loaded', true)
	)
);
__on_player_releases_item(player, item_tuple, hand) ->
(
	
	holds_mainhand = query(player, 'holds', 'mainhand');
	holds_offhand = query(player, 'holds', 'offhand');
	l(main_item,main_count,main_nbt) = holds_mainhand || l('None', 0, null);
	l(off_item,off_count,off_nbt) = holds_offhand || l('None', 0, null);
	
	if (main_item == 'bow' && off_item == 'torch',
	
	
	__look_for_arrow(player,2)
	)
);

__look_for_arrow(player, dynam) ->
(
l(x, y, z) = player ~ 'pos';

	//The problem with this method is that it can find an arrow that does not belong to the player. 
	//for example if you get shot by a skeleton at the same time firing an arrow. the Skeletons arrow could be the one selected
	//If there is a way to identify who the arrow belong to, I could fix that bug and make one for TNT. woulnt that be fun. 
	first(entity_area('arrow', x, y, z, dynam, dynam, dynam), item =  _);

	if (item == 'Arrow',//probably dont need this check. part of old code

		
		if(query(player, 'gamemode') == 'survival', //probably need to do the same for adventure?
			inventory_remove(player(), 'torch', 1); // will remove the torch from inventory first... offhand last
		);
		
		//add a tick an on remove to pop the torch after it lands or hits a mob
		entity_event(item, 'on_tick', '__did_arrow_stop', pos(item));
		entity_event(item,'on_removed', '__did_arrow_hit_e');
		
		
	);


);


__did_arrow_stop(entity, location) ->
(
	
	info = query(entity,'nbt');


	test = nbt(info):'inGround';
	if(test == 1,
		found_block = false;
		position = pos(entity);
		modify(entity,'kill');

		
		l(x, y, z) = position;
		ground = pos(block(x, y-1,z));
		north = pos(block(x, y,z-1));
		south = pos(block(x, y,z+1));
		east = pos(block(x+1, y,z));
		west = pos(block(x-1, y,z));
		item_nbt = nbt('{Item:{id:"minecraft:torch",Count:1b}}');
		
		if(!air(position),
			if(block(position) != 'snow',
				//if the position is a small "block" like chess or sign or it is water then we will just drop the item.
				found_block = true; // set to true to skip the remaining checks
				spawn('item', position, item_nbt);
		
			);
		);
		
		
		//This is the UGLY part of the code. I could not figure out how to calculate the 
		//angle of the arrow and position of the block it hit or else this would work a lot different.
		//so instead it will try to place the torch at the ground then north ,south, east, and west walls. 
		//if it cant then it will just drop the item. 
		//if the arrow hits the edge of the block or corner it may not find a spot to place a torch. 
		if(found_block == false && solid(ground),
			set(position,'torch');
			found_block = true;
		
		);
		if(found_block == false && solid(north),
			set(position,'wall_torch[facing=south]');
			found_block = true;
		
		);
		if(found_block == false && solid(south),
			set(position,'wall_torch[facing=north]');
			found_block = true;
		
		);
		if(found_block == false && solid(east),
			set(position,'wall_torch[facing=west]');
			found_block = true;
		
		);
		if(found_block == false && solid(west),
			set(position,'wall_torch[facing=east]');
			found_block = true;
		
		);
		if(found_block == false,
			spawn('item', position, item_nbt);
		)
	);
);

__did_arrow_hit_e(entity) ->
(
	info = query(entity,'nbt');
	test = nbt(info):'inGround';
	position = pos(entity);
		
	item_nbt = nbt('{Item:{id:"minecraft:torch",Count:1b}}');
	if(test != 1,
		
		spawn('item', position, item_nbt);
	);
	
	
);



//see if entity is in motion
//query(e,'motion')
//query(e,'motion_x'), query(e,'motion_y'), query(e,'motion_z')


//query(e,'pos')


//place_item('carrot',x,y,z)

//query(e,'id')

//query(e,'pitch'), query(e,'yaw')

//query(e,'tags')

//query(e,'name'), query(e,'custom_name'), query(e,'type')

//query(e,'holds',slot?)

//modify(e, 'custom_name'), modify(e, 'custom_name', name )

//modify(e, 'tag', tag, ? ...), modify(e, 'tag', l(tags) )

//modify(e, 'clear_tag', tag, ? ...), modify(e, 'clear_tag', l(tags) )

//inventory_remove(inventory, item, amount?)

//query(e,'x'), query(e,'y'), query(e,'z')

//entity_list(type)

//entity_area(type, cx, cy, cz, dx, dy, dz)

//query(e,'nbt',path?)











