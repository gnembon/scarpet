__config() ->
(
	m(
		l('scope','player'),
		l('stay_loaded',true),

		l('commands',
			m(
				l('<bucket>','__print'),
				l('<bucket> <stack>','__change')
			)
		),

		l('arguments',
			m(
				l('bucket',
					m(
						l('type','term'),
						l('options',l('all',...(filter(item_list(),_ ~ '_bucket') - '_bucket'))),
						l('suggest',l('all',...(filter(item_list(),_ ~ '_bucket') - '_bucket')))
					)
				),
				l('stack',
					m(
						l('type','int'),
						l('min',1),
						l('max',2^31-1),
						l('suggest',l(1,2,4,8,16,32,64))
					)
				)
			)
		)
	)
);

//used when a player encounters a bucket item
__on_player_collides_with_entity(player,entity) ->
(
	if(entity ~ 'pickup_delay' == 0,
		//show some pretty particles when we pick an item up off the ground(delay == 0)
		global_particle_bool = true;
		__pickup_bucket_entity(player,entity)
	);
);

//used when a player fills a bucket with a liquid
__on_player_uses_item(player, item_tuple, hand) ->
(
	l(item,count,nbt) = item_tuple;
	if(item == 'bucket',
		bucket_item = query(player,'trace',4.5,'liquids') + '_bucket';
		if(inventory_find(player,null) > 35,
			schedule(0,'__get_bucket_entity',player,bucket_item),
			empty_slot = inventory_find(player,null);
			//call this here and not on_player_picks_up because we won't know where the item went
			schedule(0,'__move_bucket_item',player,empty_slot)
		)
	)
);

//used when a player right-clicks on an entity for a milk bucket or water mob bucket
__on_player_interacts_with_entity(player, entity, hand) ->
(
	//figure out what the filled bucket is
	if(hand == 'mainhand',
		slot = player~'selected_slot',
		//-1 is offhand
		slot = -1
	);
	item = inventory_get(player,slot):0;
	//lower because entity names are capitalized, bucket names are lowercase
	//replace for tropical fish
	bucket_prefix = lower(replace(entity,' ','_'));
	if(item == 'bucket' && (entity == 'Cow' || entity == 'Goat'),
		product_item = 'milk_bucket',
		item == 'water_bucket' || keys(load_app_data()) ~ bucket_prefix,
		product_item = bucket_prefix + '_bucket'
	);
	if(product_item,
		if(inventory_find(player,null) > 35,
			//use schedule() because item doesn't exist until end of tick
			schedule(0,'__get_bucket_entity',player,product_item),
			empty_slot = inventory_find(player,null);
			//call this here and not on_player_picks_up because we won't know where the item went
			schedule(0,'__move_bucket_item',player,empty_slot)
		)
	)
);

//sets up app data with default stack sizes of 1
__initialize() ->
(
	buckets = load_app_data();
	if(!buckets,
		buckets = m();
	);
	for(filter(item_list(),_ ~ '_bucket') - '_bucket',
		if(buckets:_ == null,
			buckets:_ = 1;
		);
	);
	store_app_data(buckets);
);

//prints bucket stack size(s) to the player
__print(bucket) ->
(
	data = parse_nbt(load_app_data());
	if(bucket != 'all',
		buckets = l(bucket),
		buckets = sort(keys(data));
	);
	for(buckets,
		title = title(replace(_,'_',' '));
		stack = data:_;
		print(player(),title + ' has a max stack size of ' + stack + ' bucket' + bool(stack - 1) * 's')
	)
);

//changes max stack size of particular bucket
__change(bucket,stack) ->
(
	if(player() ~ 'permission_level' > 1,
		buckets = parse_nbt(load_app_data());
		if(bucket != 'all',
			buckets:bucket = stack,
			for(keys(buckets),
				buckets:_ = stack;
			);
		);
		store_app_data(buckets),
		print(player(),format('r You do not have permission to change bucket stack sizes.'));
	)
);

//stacks buckets when player generates them
//stacks buckets when player generates them
__move_bucket_item(player,empty_slot) ->
(
	empty_tuple = inventory_get(player,empty_slot);
	//make sure the bucket actually went to a different slot
	if(empty_tuple,
		l(item,count,nbt) = empty_tuple;
		player_list = l(range(inventory_size(player) - 6));
		max_stack = load_app_data():(item - '_bucket') || 1;
		//loop until we find a spot for the bucket
		//unless you picked up a stack of buckets, this should only run once
		while(count > 0,inventory_size(player) - 6,
			//find where the bucket should have been stacked
			//don't check slot 35 since it's the last slot
			slot_index = first(player_list,
				slot_tuple = inventory_get(player,_);
				//we're expecting a particular item with less items than the max stack size and identical nbt
				slot_tuple:0 == item && slot_tuple:1 < max_stack && slot_tuple:2 == nbt
			);
			//check > -1 because it could stack in slot 0
			if(slot_index > -1 && slot_index != empty_slot,
				//move as many items as possible
				move = min(max_stack - slot_tuple:1,count);
				//reduce value to reflect how many buckets got moved
				count = count - move;
				//always remove before adding to prevent duping
				inventory_set(player,empty_slot,count,item,nbt);
				inventory_set(player,slot_index,slot_tuple:1 + move,item,nbt)
			)
		)
	)
);

//gets the bucket item entity that was created when a player with a full inventory filled a bucket
__get_bucket_entity(player,item) ->
(
	//get item entity with low age and matching name that is around 0.3 blocks below player's eyes
	entity_list = filter(entity_area('item',player~'pos' + l(0,player~'eye_height' - 0.3,0),l(0.25,0.25,0.25)),_ ~ 'age' < 3 && _ ~ 'item':0 == item);
	if(entity_list,
		//no need for fireworks if we are just injecting the item into the inventory
		global_particle_bool = false;
		__pickup_bucket_entity(player,entity_list:0)
	)
);

//determines if entity was actually a bucket and can potentially stack in the player's inventory
__pickup_bucket_entity(player,entity) ->
(
	l(item,count,nbt) = entity~'item';
	if(item~'_bucket',
		//if it's an item we're picking up that is a non-empty bucket
		//first determine the max stack size
		max_stack = load_app_data():(item - '_bucket') || 1;
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

//attempts to stack item in a player's inventory at slot
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

//used when a player places a liquid
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

//tests that the bucket that placed the liquid actually got used
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

//attempts to initialize app data on every load
__initialize();
