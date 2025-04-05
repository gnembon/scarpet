// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

// seems like drops are generated later, before the callback is made
// so we execute moving items at the end of the tick
__on_player_breaks_block(player, block) -> 
(
	schedule(0,'_move_items_to_inventory', player, pos(block))
);


// (_ ~ 'pickup_delay') == 10 check is optional
// it will only pick up items that are 'pick-broken'.
// Removal of this one will also affect contents of chests etc.

// this will teleport items to the player and let them pick it up if they can.
// un-picked items will end up on the ground next to the player
_move_items_to_player(player, coords) ->
(
	tploc = pos(player)+l(0,((player ~ 'eye_height')/2),0);
	selector = str('@e[type=item,x=%d,y=%d,z=%d,dx=1,dy=1,dz=1]',coords);
	for ( filter(entity_selector(selector), _~'pickup_delay' == 10 ),
		modify(_,'pos',tploc);
		modify(_,'pickup_delay',0)
	)
);

// this finds a place for the item in the inventory
// and leaves the items on the ground if not picked up
// setup is crude and doesn't partially fill stacks etc
// items that won't make it to the inventory, will stay where they dropped.
_move_items_to_inventory(player, coords) ->
(
	selector = str('@e[type=item,x=%d,y=%d,z=%d,dx=1,dy=1,dz=1]',coords);
	for (filter(entity_selector(selector), _~'pickup_delay' == 10 ),
		current_entity_item = _ ;		
		try
		(
			l(item_name, count, item_nbt) = (current_entity_item ~ 'item');
			// special-case shulker boxes because of the carpet rule stackableShulkerBoxes
			if (!(item_name ~ 'shulker_box' && has(item_nbt, 'components')),
				slot = -1;
				while( (slot = inventory_find(player, item_name, slot+1)) != null, 41,
					current = inventory_get(player, slot);
					matched_count_nbt = current:2;
					if ( has(item_nbt, 'count'),
						put(matched_count_nbt, 'count', count)
					);
					if ( current:1+count <= stack_limit(item_name) && matched_count_nbt == item_nbt,
						inventory_set(player, slot, count+current:1);
						throw()
					)			
				)
			);
			slot = inventory_find(player, null);
			if (slot != null && slot < 36, // skip #40, like in vanilla
				inventory_set(player, slot, count, item_name, item_nbt);
				throw()
			)
		, // exception block
			// found a spot, must remove
			modify(current_entity_item, 'remove')
		)
	)
)
