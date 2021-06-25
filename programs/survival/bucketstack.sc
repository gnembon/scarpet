__config() ->
(
	//change these number to whatever you want the maximum stack size to be
	//note that the max scarpet can inventory_set() is 2^31 - 1
	global_buckets = m(
		l('water',20),
		l('lava',1),
		l('milk',1),
		l('pufferfish',1),
		l('salmon',1),
		l('cod',1),
		l('tropical_fish',1),
		l('axolotl',1)
	);
	m(
		l('scope','player'),
		l('stay_loaded',true)
	)
);

__on_player_collides_with_entity(player,entity) ->
(
	if(entity~'pickup_delay' == 0,
		l(item,count,nbt) = entity~'item';
		if(item~'_bucket',
			//if it's an item we're picking up that is a non-empty bucket
			//first determine the max stack size
			type = null;
			while(type == null,length(global_buckets),
				type = item~(keys(global_buckets):_);
			);
			max_stack = global_buckets:type;
			//then find a non-full slot to put it in
			//modified lines 103 and 104 of gnembon's shulkerboxes.sc to find a non-full slot
			slot = -1;
			while(count && (slot = inventory_find(player,item,slot+1)) != null && slot < 36,inventory_size(player)-5,
				//using copy() because script kept throwing errors here
				l(slot_item,slot_count,slot_nbt) = copy(inventory_get(player,slot));
				if(slot_count < max_stack,
					//attempt to pick up items
					count = __pickup(player,entity,item,slot,count,slot_count,max_stack);
				);
			);
			//if all possible stacks are full, find as many empty slots as necessary
			//slot < 36 and inventory_size(player)-5 unless you want to wear the buckets
			while(count && (slot = inventory_find(player,null)) != null && slot < 36,inventory_size(player)-5,
				count = __pickup(player,entity,item,slot,count,0,max_stack);
			);
		);
	);
);

__pickup(player,entity,item,slot,count,slot_count,max_stack) ->
(
	//figure out how many items to move
	//and move them
	move = min(max_stack - slot_count,count);
	//if all possible slots are full, leave whatever is left on the ground
	count = count - move;
	
	if(move,
		//lines 85,95, and 68 of gnembon's shulkerboxes.sc for particles, item pickup animation, and item pickup sound
		particle('portal', pos(entity)-[0,0.3,0], 10, 0.1, 0);
		modify(entity, 'pickup_delay', 1);
		sound('entity.item.pickup',pos(player),0.2,(rand(1)-rand(1))*1.4+2.0, 'player');
	);
	
	//modified line 90 of gnembon's shulkerboxes.sc to change the number of items in the item entity
	modify(entity,'nbt_merge','{Item:{Count:' + count + 'b}}');
	//always set player inventory after removing items from world to prevent duping
	inventory_set(player,slot,slot_count + move,item);
	return(count);
);

//WRITTEN BY FIRIGION

__on_player_right_clicks_block(player,item_tuple,hand,block,face,hitvec) ->
(
	if(hand == 'mainhand',
		slot = player~'selected_slot',
		//-1 is offhand
		slot = -1;
	);
	if(item_tuple:0~'_bucket' && !item_tuple:0~'milk',
		schedule(0,'test_bucket_used',player,hand,item_tuple,slot);
	);
);

//only for milk
__on_player_finishes_using_item(player,item_tuple,hand) ->
(
	if(hand == 'mainhand',
		slot = player~'selected_slot',
		//-1 is offhand
		slot = -1;
	);
  	if(item_tuple:0 == 'milk_bucket',
		schedule(0,'test_bucket_used',player,hand,item_tuple,slot);
	);
);

test_bucket_used(player, hand, item_tuple, slot) ->
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
				bucket_slot = -1;
				//bucket_slot < 36 and inventory_size(player)-5 unless you want to wear the buckets
				while((bucket_slot = inventory_find(player,_,bucket_slot+1)) != null && bucket_slot < 36,inventory_size(player)-5,
					slot_count = inventory_get(player,bucket_slot):1;
					//dont want to overfill a slot or overwrite the original slot
					if(slot_count < stack_limit('bucket') && bucket_slot != slot,
						inventory_set(player,bucket_slot,slot_count + 1,'bucket');
						return();
					);
				);
			);
			//if there were absolutely no places to put the bucket, just spawn a bucket item
			spawn('item',pos(player)+l(0,((player~'eye_height')/2),0),nbt(m(l('Item',m(l('id','"minecraft:bucket"'),l('Count','1b'))),l('PickupDelay',0))));
		);
	);
);
