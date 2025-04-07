__on_player_uses_item(player, item_tuple, hand) -> 
	refill(player, hand, item_tuple);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
	refill(player, hand, item_tuple);

__on_player_breaks_block(player, block) -> 
	if (item = player ~ 'holds', refill(player, 'mainhand', item));

__on_player_interacts_with_entity(player, entity, hand) -> 
	if (item = query(player,'holds',hand), refill(player, hand, item));

__on_player_releases_item(player, item_tuple, hand) -> 
	refill(player, hand, item_tuple);

__on_player_finishes_using_item(player, item_tuple, hand) -> 
	refill(player, hand, item_tuple);

__on_player_attacks_entity(player, entity) -> 
(
	// weapon may break, or you may run out of bows in the offhand, possibly
	// can you even attack with the offhand? don't know
	for(l('mainhand', 'offhand'),
		if (item = query(player, 'holds', _), 
			refill(player, _, item)
		)	
	)
);

refill(player, hand, item_tuple) ->
(
	if (!item_tuple, return());
	slot = if(hand=='offhand',40, player ~ 'selected_slot');
	schedule(0, '__refill_endtick', player, item_tuple, slot)
);

new_shulker_replace(player, shulker_box, item_name, slot, sbox_slot) ->
(
	sbox_items = shulker_box:2:'components':'minecraft:container[]';
	// skip empty or uninitialized sboxes				
	if (sbox_items,
		//making sure we have a list, since one element is a scalar response
		if (type(sbox_items)!='list', sbox_items = l(sbox_items));
		long_item_name = 'minecraft:' + item_name;			
		for (sbox_items,
			if (_:'item':'id' == long_item_name, // found it
				inventory_set(player,slot, _:'item':'count', _:'item':'id', _:'item');
				data = shulker_box:2;
				delete(sbox_items, _i);
				put(data, 'components."minecraft:container"', sbox_items);
				inventory_set(player, sbox_slot, shulker_box:1, shulker_box:0, data);
				return(true)
			)
		)
	)
);

old_shulker_replace(player, shulker_box, item_name, slot, sbox_slot) ->
(
	sbox_items = shulker_box:2:'BlockEntityTag.Items[]';
	// skip empty or uninitialized sboxes				
	if (sbox_items,
		//making sure we have a list, since one element is a scalar response
		if (type(sbox_items)!='list', sbox_items = l(sbox_items));
		long_item_name = 'minecraft:'+item_name;				
		for (sbox_items,
			if (_:'id' == long_item_name, // found it
				inventory_set(player,slot, _:'Count', item_name, _:'tag');
				delete(shulker_box:2, 'BlockEntityTag.Items['+_i+']');
				inventory_set(player, sbox_slot, shulker_box:1, shulker_box:0, shulker_box:2);
				return(true)
			)
		)
	)
);

version_major = system_info('game_major_target');
global_new_version = version_major > 20 || (version_major == 20 && system_info('game_minor_target') >= 5);

__refill_endtick(player, old_item, slot) ->
(
	if(inventory_get(player,slot), return()); // player still has items on that slot - no need to pick new ones
	item_name = old_item:0;
	// first internal inventory slots
	// loose items
	for(range(35, 0, -1), 
		if ((inv_item = inventory_get(player, _)):0 == item_name, // we found it
			inventory_set(player,slot,inv_item:1, inv_item:0, inv_item:2);
			inventory_set(player, _, if(global_new_version, 0, null));
			return()		
		)
	);
	// now search in sboxes
	for(range(35, 0, -1), sbox_slot = _;
		if ( (shulker_box = inventory_get(player, sbox_slot)):0 ~ 'shulker_box', // found a shulkerbox
			if(			
				if(global_new_version,  // Shulker box replacement must be handled differently with the new component system
					new_shulker_replace(player, shulker_box, item_name, slot, sbox_slot),
				// Else
					old_shulker_replace(player, shulker_box, item_name, slot, sbox_slot)
				),
				break() // Both functions return true if a sbox is found, so this if/break stops the search loop
			)
		)
	)
)
