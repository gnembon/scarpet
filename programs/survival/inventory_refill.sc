// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	start_pos = pos(block);
	if (!(player ~ 'sneaking' && !item_tuple && hand == 'mainhand' && 
			inventory_size(start_pos) ), 
		return()
	);
	storage = l(__find_storage(start_pos, 32, 10000));
	for(range(inventory_size(player)),
		__insert_into_storage(player, _, storage)
	)
);

__on_player_clicks_block(player, block, face) ->
(
	start_pos = pos(block);
	if (!(player ~ 'sneaking' && inventory_size(start_pos) && !player~'holds'), 
		return()
	);
	storage = l(__find_storage(start_pos, 32, 10000));
	for(range(inventory_size(player)),
		__retrieve_from_storage(player, _, storage )
	)
);

global_banned_blocks = m('hopper', 'dropper', 'dispenser');
__find_storage(position, distance_limit, hard_limit) -> 
(
	// stack based approach, bfs, way faster than recursion, but less cooler
	visited_nodes = l(position);
	visited_node_set = m(l(position, distance_limit));
	storage_nodes = m(l(position, distance_limit));
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
	l(controller_item, controller_count, tag) = controller_stack;
	for( storage_nodes_singleton:0,
		storage_pos = _;
		inv_slot = 0;
		while( (inv_slot = inventory_find(storage_pos, controller_item, inv_slot)) != null, inventory_size(storage_pos),
			inv_stack = inventory_get(storage_pos, inv_slot);
			l(inv_item, inv_count, inv_tag) = inv_stack;
			if (inv_count < item_stack_limit && tag == inv_tag,
				add_limit = item_stack_limit - inv_count;
				particle('end_rod',storage_pos+0.5,20, 0.5, 0);
				if (controller_count > add_limit,
					controller_count += -add_limit;
					inventory_set(container, container_slot, controller_count);
					inventory_set(storage_pos, inv_slot, item_stack_limit)
				,
					inventory_set(container, container_slot, 0);
					inventory_set(storage_pos, inv_slot, inv_count+controller_count);
					return()
				)
			);
			inv_slot += 1
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
	l(controller_item, controller_count, tag) = controller_stack;
	for( storage_nodes_singleton:0,
		storage_pos = _;
		inv_slot = 0;
		while( (inv_slot = inventory_find(storage_pos, controller_item, inv_slot)) != null, inventory_size(storage_pos),
			inv_stack = inventory_get(storage_pos, inv_slot);
			l(inv_item, inv_count, inv_tag) = inv_stack;
			if (inv_count > 1 && tag == inv_tag,
				add_limit = item_stack_limit - controller_count;
				particle('end_rod',storage_pos+0.5,20, 0.5, 0);
				if ((inv_count-1) < add_limit, // inventory has less then player needs
					controller_count += inv_count-1;
					inventory_set(container, container_slot, controller_count);
					inventory_set(storage_pos, inv_slot, 1)
				,
					inventory_set(container, container_slot, item_stack_limit);
					inventory_set(storage_pos, inv_slot, inv_count-add_limit);
					return()
				)
			);
			inv_slot += 1
		)
	)
)