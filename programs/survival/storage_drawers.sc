import('math', '_euclidean_sq');

global_controllers = null;
global_banned_blocks = m('hopper', 'dropper', 'dispenser');
global_dims = l('overworld', 'the_nether', 'the_end');

__config() ->  {
	'scope' -> 'global'
};

//displays all controller positions and detailed content of the closest storage
__command() ->
(	
	print('');
	for(filter(global_dims, length(global_controllers:_) > 0), dim = _;
		in_dimension(dim,
			print('Controllers in '+dim+': '+
				join(', ', map(global_controllers:dim, str(_)+if(loaded(_),'',' (inactive)'))))
		)
	);
	current_player = player();
	player_pos = pos(current_player);
	in_dimension( current_player,
		loaded_controllers = sort_key(
			filter(global_controllers:current_dimension(), loaded(_)), 
			_euclidean_sq(_,player_pos)
		);
		if (loaded_controllers,
			closest_controller = loaded_controllers:0;
			stats = __controller_capacity(closest_controller);
			print('Closest Controller at '+closest_controller);
			print(' - Total containers: ' + stats:'__total_containers');
			print(' - Total slots: ' + stats:'__total_slots');
			print(' - Free slots: ' + stats:'__free_slots');
			delete(stats:'__total_containers');
			delete(stats:'__total_slots');
			delete(stats:'__free_slots');
			if (stats,
				print(' - Content:');
				items = sort_key(keys(stats), - stats:_:0);
				top_items = slice(items, 0, 10);
				for (top_items,
					print('   * '+_+': '+stats:_:0+'/'+stats:_:1 )
				)
			)
		)	
	);
	''
);

// counts all items in the storage complex
__controller_capacity(position) ->
(
	// recalculate storage
	__calculate_storage(position);
	dim = current_dimension();
	stats = m();
	stats:'__total_containers' = length(global_controllers:dim:position);
	stats:'__total_slots' = 0;
	stats:'__free_slots' = 0;
	for (global_controllers:dim:position, inv_pos = _;
		if (inv_size = inventory_size(inv_pos),
			stats:'__total_slots' +=  inv_size;
			for ( range(inv_size),
				item = inventory_get(inv_pos, _);
				if (item,
					//is an item
					item_name = item:0;
					count = item:1;
					if (!has(stats:item_name), stats:item_name = l(0, 0));
					
					stats:item_name:0 += count; // actual
					// equivalent of functional: 
					// put(get(stats, item_name), 0, get(get(stats, item_name), 0) + count);
					
					stats:item_name:1 += stack_limit(item_name) // total
				, //else (free slot)
					stats:'__free_slots' += 1
				)
			)
		)
	);
	stats
);

__on_start() ->
(
	stored_controllers = load_app_data();
	if (!stored_controllers,
		global_controllers = m();
		for(global_dims, global_controllers:_ = m());
		__store_controllers();
		return()
	);
	__load_controllers(stored_controllers)
);

// stores controller positions in app nbt storage
__store_controllers() ->
(
	tag = nbt('{}');
	for ( global_dims, dimension = _;
		tag:dimension = nbt('[]');
		for (map( global_controllers:dimension, str(_)),
			put(tag:dimension, _, -1)
		)
	);
	store_app_data(tag)
);

__load_controllers(tag) -> 
(
	global_controllers = m();
	for (global_dims, dimension = _;
		global_controllers:dimension = m();
		positions = tag:(dimension+'.[]');
		if (positions,
			positions = if(type(positions)=='list', positions, l(positions));
			for (positions,
				pos = nbt(_):'[]';
				print('Adding controller at '+pos+' in '+dimension);
				global_controllers:dimension:pos = null
			)
		)
	)
);

__add_controller(dimension, position) ->
(
	if (!has(global_controllers:dimension), exit());
	put(global_controllers:dimension:(map(position, floor(_))), null);
	in_dimension( dimension,
		__calculate_storage(position)
	);
	__store_controllers()
);

__remove_controller(dimension, position) -> 
(
	if (!has(global_controllers:dimension), exit());
	print('Removing controller from '+dimension+' at '+position);
	delete(global_controllers:dimension:(map(position, floor(_))));
	__store_controllers()
);

__on_tick() -> (
	for(global_dims,
		in_dimension(_, __tick_drawers());
	);
);

__tick_drawers() ->
(
	if (!(tick_time()%100), schedule(rand(100), '__check_drawers'));
	if (!rand(10),
		for (filter(global_controllers:current_dimension(), loaded(_)), cpos = _;
			ppos = cpos+l(rand(1),1,rand(1));
			particle(if(power(cpos), 'dust 1 0 0 1', 'dust 0 1 0 1'), ppos, 1, 0, 0)
		)
	)
);

__check_drawers() -> 
(
	dimension = current_dimension();
	// no controllers
	if (!global_controllers:dimension, return());
	loaded_controllers = l();
	for (global_controllers:dimension, cpos = _;
		if (loaded(cpos),
			if (__is_valid_controller(cpos),
				loaded_controllers += cpos
			, //else
				__remove_controller(dimension, cpos)
			)
		)
	);
	if (!loaded_controllers, return());
	random_controller = (tick_time()/100) % length(loaded_controllers);
	__calculate_storage(loaded_controllers:random_controller);
	for (loaded_controllers,
		schedule(rand(50), '__transfer_items_from_controller', _)
	)
);

__is_valid_controller(position) ->
(
	block = block(position);
	if (block~'shulker_box',
		data = block_data(position);
		has(data:'CustomName') && nbt(data:'CustomName'):'text' == 'Controller'
	)
);

__calculate_storage(position) ->
(
	storage_nodes = __find_storage(position, 24, 5000);
	delete(storage_nodes:position);
	global_controllers:current_dimension():position = keys(storage_nodes)
);

__transfer_items_from_controller(position) ->
(
	if (!inventory_has_items(position), return());
	if (!__is_valid_controller(position), return());
	storage_nodes = global_controllers:current_dimension():position;
	if (storage_nodes == null,
		__calculate_storage(position);
		storage_nodes = global_controllers:current_dimension():position
	);
	if (!storage_nodes, return());
	storage_nodes_singleton = l(storage_nodes);
	loop( inventory_size(position),
		if (power(position),
			__retrieve_from_storage(position, _, storage_nodes_singleton)
		, // else
			__insert_into_storage(position, _, storage_nodes_singleton)
		)
	);
	null
);

//debug function to spawn information around the nodes
_debug_storage(x, y, z) ->
(
	storage_nodes = __find_storage(l(x,y,z), 36, 1000);
	__ttl(entity, maxage) -> if(entity~'age'>maxage, modify(entity, 'remove'));
	for (storage_nodes,
			marker = create_marker(storage_nodes:_, _+l(0.5, 0.3, 0.5));
			entity_event(marker, 'on_tick', '__ttl', 200);
			particle_rect('dust 1 0 0 2', _, _+1, 1.2)
	)
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	start_pos = pos(block);
	// player may be placing a shulkerbox
	if (item_tuple && item_tuple:0 ~ 'shulker_box',
		offset_pos = pos_offset(block,face);
		schedule(0, '__attempt_add_controller', offset_pos)
	);
	
	if (!(player ~ 'sneaking' && !item_tuple && hand == 'mainhand' && __is_valid_controller(start_pos)), 
		return()
	);
	// player is sneak right-clicking on the controller
	storage_nodes = l(__find_storage(start_pos, 32, 10000));
	for(range(inventory_size(player)),
		// Achtung: trick
		// passing l(x), rather than x, makes so that its all passed as a reference, not copy
		if (power(start_pos),
			__retrieve_from_storage(player, _, storage_nodes)
		, // else
			__insert_into_storage(player, _, storage_nodes)
		)
	)
);

__on_player_clicks_block(player, block, face) ->
(
	start_pos = pos(block);
	if (!(player ~ 'sneaking' && __is_valid_controller(start_pos) && !player~'holds'), 
		return()
	);
	storage_nodes = l(__find_storage(start_pos, 32, 10000));
	for(range(inventory_size(player)),
		if (power(start_pos),
			__retrieve_from_storage(player, _, storage_nodes)
		, // else
			__insert_into_storage(player, _, storage_nodes)
		)
	)
);

__attempt_add_controller(position) ->
(
	if (__is_valid_controller(position) && !(has(global_controllers:current_dimension():position)),
		print('Added controller at: '+position);
		__add_controller(current_dimension(), position)
	)
);

__find_storage(position, limit, hard_limit) -> 
(
	// stack based approach, bfs, way faster than recursion, but less cooler
	visited_nodes = l(position);
	visited_node_set = m(l(position, limit));
	storage_nodes = m(l(position, limit));
	loop(hard_limit,
		index = _;
		if (index >= length(visited_nodes),
			return(storage_nodes)
		);
		current_pos = visited_nodes:index;
		distance = visited_node_set:current_pos;
		if (inventory_size(current_pos) && !has(global_banned_blocks:str(block(current_pos))), 
			storage_nodes:current_pos = distance;
			if (distance > 0,
				for( neighbours(current_pos),
					npos = pos(_);
					if (!has(visited_node_set:npos),
						visited_nodes+=npos;
						visited_node_set:npos = distance-1
					)
				)
			)
		)
	);
	storage_nodes
);

__insert_into_storage(container, container_slot, storage_nodes_singleton) ->
(
	controller_stack = inventory_get(container, container_slot);
	if (!controller_stack, return());
	item_stack_limit = stack_limit(controller_stack:0);
	// unstackables
	if (item_stack_limit == 1, return()); 
	l(item, controller_count, tag) = controller_stack;
	for( storage_nodes_singleton:0,
		storage_pos = _;
		if (inventory_has_items(storage_pos),
			inv_slot = -1;
			while( (inv_slot = inventory_find(storage_pos, item, inv_slot+1)) != null, inventory_size(storage_pos),
				inv_stack = inventory_get(storage_pos, inv_slot);
				l(inv_item, inv_count, inv_tag) = inv_stack;
				if (inv_count < item_stack_limit && tag == inv_tag,
					add_limit = item_stack_limit - inv_count;
					particle('end_rod',storage_pos+0.5,10, 0.5, 0);
					if (controller_count > add_limit,
						controller_count += -add_limit;
						inventory_set(container, container_slot, controller_count);
						inventory_set(storage_pos, inv_slot, item_stack_limit)
					,
						inventory_set(container, container_slot, 0);
						inventory_set(storage_pos, inv_slot, inv_count+controller_count);
						return()
					)
				)
			)
		)
	)
);

__retrieve_from_storage(container, container_slot, storage_nodes_singleton) ->
(
	controller_stack = inventory_get(container, container_slot);
	if (!controller_stack, return());
	item_stack_limit = stack_limit(controller_stack:0);
	// unstackables
	if (item_stack_limit == 1 || controller_stack:1 == item_stack_limit, return()); 
	l(item, controller_count, tag) = controller_stack;
	for( storage_nodes_singleton:0,
		storage_pos = _;
		if (inventory_has_items(storage_pos),
			inv_slot = -1;
			while( (inv_slot = inventory_find(storage_pos, item, inv_slot+1)) != null, inventory_size(storage_pos),
				inv_stack = inventory_get(storage_pos, inv_slot);
				l(inv_item, inv_count, inv_tag) = inv_stack;
				if (inv_count > 1 && tag == inv_tag,
					add_limit = item_stack_limit - controller_count;
					particle('end_rod',storage_pos+0.5,10, 0.5, 0);
					if ((inv_count-1) < add_limit, // inventory has less then player needs
						controller_count += inv_count-1;
						inventory_set(container, container_slot, controller_count);
						inventory_set(storage_pos, inv_slot, 1)
					,
						inventory_set(container, container_slot, item_stack_limit);
						inventory_set(storage_pos, inv_slot, inv_count-add_limit);
						return()
					)
				)
			)
		)
	)
)
