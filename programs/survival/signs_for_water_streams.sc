// signs_for_water_streams by indoorjetpacks
// hold any bucket in your off hand while placing signs to place the sign without getting the 'Edit sign message' prompt. 
// Useful for placing water/lava flows if you make a lot of them. Lookin' at you, SciCraft server.

//written for scarpet 1.6 in carpet mod 1.3.0. Both from the amazing gnembon (although...how'd you get this far without knowing that?)
//https://github.com/gnembon/fabric-carpet/releases


__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	l(item, count, nbt) = item_tuple || l('None', 0, null);
	
	//check what's behind held in main and off hand to make sure player is holding bucket and placing signs
	// mh = main hand oh = offhand
	
	mh_query = query(player,'holds', 'mainhand');
	oh_query = query(player,'holds', 'offhand');
	
	// from what i can tell, the || part keeps an empty hand from returning unwanted info to the query
	l(mh_item, mh_count, mh_nbt) = mh_query || l('None',0,null); 
	l(oh_item, oh_count, oh_nbt) = oh_query || l('None',0,null);
	if
	(
		oh_item ~ 'bucket' && mh_item ~ 'sign',
		//player_rclick_on_top(player, block, face); 
		player_rclick_on_side(player, mh_item, block, face);
	);
	
	//future idea - placing a trapdoor/slab while holding water bucket will place a waterlogged trapdoor/slab
	//if
	//(
	//	oh_item = 'water_bucket' && mh_item ~ 'slab',
	//	player_rclick_with_slab(player,mh_item, block, face);
		
	//);
	
);

// currently unused, but there for possible future placement of sign atop blocks with rotation values
// not pursuing because usually with water streams you place signs on the side of blocks, or on other signs.
player_rclick_on_top(player, block, face) ->
(
	if
	(
		face == 'up',
			print('clicked the top');
			return(true)
	);
);

player_rclick_on_side(player, mh_item, block, face) ->
(
	//the namespaces are different between wall/standing signs, so check what the player is holding and replace it with the wall version of that in the set() command
	if (mh_item ~ 'oak', wallsign = 'oak_wall_sign');
	if (mh_item ~ 'spruce', wallsign = 'spruce_wall_sign');
	if (mh_item ~ 'birch', wallsign = 'birch_wall_sign');
	if (mh_item ~ 'jungle', wallsign = 'jungle_wall_sign');
	if (mh_item ~ 'acacia', wallsign = 'acacia_wall_sign');
	if (mh_item ~ 'dark_oak', wallsign = 'dark_oak_wall_sign'); 
	
	//make sure the player clicked on the side of a block, not the top or looking upward. might be annoying for two high sign places tho. should be a simple fix if you need it tho.
	if
	(
		face != 'up' && face != 'down',
			clicked_block_pos = map(pos(block), str('%.2f',_));
			offset_by_one = pos_offset(block,face,1);
			
			//without the offset, the block you clicked on will be turned into a sign.
			//this could probably be swapped with a place_item() call but I can't get that working for some reason right now
			
			//make sure there's air there, otherwise you could auto-destroy blocks with this. 
			if
			( material(offset_by_one) == 'air',
				set(offset_by_one, wallsign, 'facing', face); 
			)
	);

);

// future implementation - clicking while holding a waterbucket in offhand waterlogs the slab/trapdoor	
//player_rclick_with_slab(player, mh_item, block, face) ->
//(
//	print('slab');
//);
