__config() ->
(
	//change these number to whatever you want the maximum stack size to be
	//note that the max scarpet can inventory_set() is 2^31 - 1
	global_buckets = m(
		l('water',1),
		l('lava',1),
		l('milk',1),
		l('pufferfish',1),
		l('salmon',1),
		l('cod',1),
		l('tropical_fish',1),
		l('axolotl',1),
		l('powder_snow',1)
	);
	m(
		l('scope','player'),
		l('stay_loaded',true)
	)
);

__on_player_collides_with_entity(player,entity) ->
(
	if(entity ~ 'pickup_delay' == 0,
		//show some pretty particles when we pick an item up off the ground(delay == 0)
		global_particle_bool = true;
		__pickup_bucket_entity(player,entity)
	);
);

__on_player_uses_item(player, item_tuple, hand) ->
(
	l(item,count,nbt) = item_tuple;
	if(item == 'bucket',
		bucket_item = query(player,'trace',4.5,'liquids') + '_bucket';
		schedule(0,'__get_bucket_entity',player,bucket_item)
	)
);

__on_player_interacts_with_entity(player, entity, hand) ->
(
	//only check everything else after we know that the player has no empty slots
	//otherwise the vanilla behavior will do the hard work for us
	if(inventory_find(player,null) > 35,
		if(hand == 'mainhand',
			slot = player~'selected_slot',
			//-1 is offhand
			slot = -1
		);
		item = inventory_get(player,slot):0;
		bucket_prefix = lower(replace(entity,' ','_'));
		if(item == 'bucket' && (entity == 'Cow' || entity == 'Goat'),
			product_item = 'milk_bucket'
		);
		//lower because entity names are capitalized, bucket names are lowercase
		//replace for tropical fish
		if(item == 'water_bucket' && keys(global_buckets) ~ bucket_prefix,
			product_item = bucket_prefix + '_bucket'
		);
		//use schedule() because item entity doesn't exist until end of tick
		if(product_item,
			schedule(0,'__get_bucket_entity',player,product_item)
		)
	)
);

__get_bucket_entity(player,item) ->
(
	//get item entities with low age and matching name that are exactly at player's eyes
	entity_list = filter(entity_area('item',player~'pos',l(0,player~'eye_height',0)),_ ~ 'age' < 3 && _ ~ 'item':0 == item);
	if(entity_list,
		//no need for fireworks if we are just injecting the item into the inventory
		global_particle_bool = false;
		__pickup_bucket_entity(player,entity_list:0)
	)
);

__pickup_bucket_entity(player,entity) ->
(
	l(item,count,nbt) = entity~'item';
	if(item~'_bucket',
		//if it's an item we're picking up that is a non-empty bucket
		//first determine the max stack size
		max_stack = global_buckets:(item - '_bucket') || 1;
		//then find a non-full slot to put it in
		//modified lines 103 and 104 of gnembon's shulkerboxes.sc to find a non-full slot
		for(l(item,null),
			slot = -1;
			while(count && (slot = inventory_find(player,item,slot+1)) != null && (slot < 36 || slot == 40),inventory_size(player)-4,
				//using copy() because script kept throwing errors here
				l(slot_item,slot_count,slot_nbt) = copy(inventory_get(player,slot));
				//stack must have room and nbt must match
				if(slot_count < max_stack && slot_nbt == nbt,
					//attempt to pick up items
					count = __pickup(player,entity,item,slot,count,slot_count,max_stack,nbt),
				)
			)
		)
	)
);

__pickup(player,entity,item,slot,count,slot_count,max_stack,nbt) ->
(
	//figure out how many items to move
	//and move them
	move = min(max_stack - slot_count,count);
	//if all possible slots are full, leave whatever is left on the ground
	count = count - move;
	
	if(move && global_particle_bool,
		//lines 85,95, and 68 of gnembon's shulkerboxes.sc for particles, item pickup animation, and item pickup sound
		particle('portal', pos(entity)-[0,0.3,0], 10, 0.1, 0);
		modify(entity, 'pickup_delay', 1);
		sound('entity.item.pickup',pos(player),0.2,(rand(1)-rand(1))*1.4+2.0, 'player')
	);
	
	//modified line 90 of gnembon's shulkerboxes.sc to change the number of items in the item entity
	modify(entity,'nbt_merge','{Item:{Count:' + count + 'b}}');
	//always set player inventory after removing items from world to prevent duping
	inventory_set(player,slot,slot_count + move,item,nbt);
	return(count)
);

//WRITTEN BY FIRIGION

__on_player_right_clicks_block(player,item_tuple,hand,block,face,hitvec) ->
(
	if(hand == 'mainhand',
		slot = player~'selected_slot',
		//-1 is offhand
		slot = -1
	);
	if(item_tuple:0~'_bucket' && !item_tuple:0~'milk',
		schedule(0,'__test_bucket_used',player,hand,item_tuple,slot)
	)
);

//only for milk
__on_player_finishes_using_item(player,item_tuple,hand) ->
(
	if(hand == 'mainhand',
		slot = player~'selected_slot',
		//-1 is offhand
		slot = -1
	);
	if(item_tuple:0 == 'milk_bucket',
		schedule(0,'__test_bucket_used',player,hand,item_tuple,slot)
	)
);

__test_bucket_used(player, hand, item_tuple, slot) ->
(
	l(item,count,nbt) = item_tuple;
	//trigger only if bucket was used
	if(inventory_get(player,slot):0 == 'bucket' || item~'milk',
		//do nothing if it was the last bucket in the stack
		if(count != 1,
			//otherwise, they should have the same bucket, just one less than before
			inventory_set(player, slot,count - 1,item,nbt);
			//now we have to give them an empty bucket
			//first try to put it in a non-full stack, then look for empty slots
			for(l('bucket',null),
				item = _;
				bucket_slot = -1;
				//bucket_slot < 36 || bucket_slot == 40 and inventory_size(player)-4 unless you want to wear the buckets
				while((bucket_slot = inventory_find(player,item,bucket_slot+1)) != null && (bucket_slot < 36 || bucket_slot == 40),inventory_size(player)-4,
					slot_count = inventory_get(player,bucket_slot):1;
					//dont want to overfill a slot or overwrite the original slot
					if(slot_count < stack_limit('bucket') && bucket_slot != slot,
						inventory_set(player,bucket_slot,slot_count + 1,'bucket');
						return();
					)
				)
			);
			//if there were absolutely no places to put the bucket, just spawn a bucket item
			spawn('item',pos(player)+l(0,((player~'eye_height')/2),0),'{Item:{Count:1b,id:"minecraft:bucket"},PickupDelay:0}');
		)
	)
);
