global_update_rate = 5; //in ticks

__config() -> {
	'commands' -> {
		'<first_pos> <second_pos>' -> 'projection',
		'place' -> 'place',
		'stop' -> 'stop',
		'clear <first_pos> <second_pos>' -> 'clear'
	}
};

projection(pos1, pos2) -> (
	[from, to] = _get_bounds(pos1, pos2);
	
	budding = {};
	volume(from, to, if(_=='budding_amethyst', budding += _));

	buds = {};
	for(budding, 
		for(neighbours(_),
			if(!has(budding, _), buds += pos(_))
		)
	);

	//convert set of blocks to positions
	budding = map(budding, pos(_));

	global_blocks = {
		'budding' -> map(range(3), _project(budding, _, to)),
		'buds' -> map(range(3), _project(buds, _, to))
	};

	loop(3, 
		i = _;
		for(global_blocks:'buds':i,
			if(has(global_blocks:'budding':i, _), delete(global_blocks:'buds':i, _))
		)
	);

	global_draw_shapes = true;
	projection_tick();

);

global_colours = {
	'buds' -> [0x0f961855, 0x0f9618ff], //greens
	'budding' -> [0xa940e655, 0xa940e6ff], //purples
};

projection_tick() -> (
	if(global_draw_shapes,
		for(pairs(global_blocks),
			colours = global_colours:(_:0);
			for(_:1,
				cubes = map(_, //iterate over the three directions
					_make_cube(...colours, _);
				);
				draw_shape(cubes)
			);
		);
		schedule(global_update_rate, 'projection_tick')
	);
);


_get_bounds(pos1, pos2) -> (
	zipped = map(pos1, [_, pos2:_i]);
	from = map(zipped, min(_));
	to = map(zipped, max(_));

	[from, to]
);

place() -> (
	if(!global_blocks,
		print(format('r You need to select a geode first'));
		exit()
	);

	blocks = {'budding' -> 'pink_glazed_terracotta', 'buds' ->'melon'};
	for(pairs(global_blocks),
		block = blocks:(_:0);
		for(_:1, //iterate over direction
			for(_, //iterate over blocks in that plane
				set(_, block);
				total_placed += 1;
			)
		)
	);

	print(format(str('g Filled %d blocks', total_placed)));
);


_make_cube(fill_colour, edge_colour, block) -> [
	'box', global_update_rate*2,
	'from', block, 
	'to', block+1,
	'fill', fill_colour,
	'color', edge_colour
];

_project(blocks, direction, corner) -> (
	out = copy(blocks);

	for(out, _:direction = corner:direction);
	
	//remove duplciates the lazy way (make the list into a set)
	out_set = {};
	for(out, out_set += _);
	out_set;
);

stop() -> global_draw_shapes = false;

clear(pos1, pos2) -> (
	volume(pos1, pos2, if(!air(_) &&_!='budding_amethyst', set(_, 'air'); total += 1));
	print(format(str('g Removed %d blocks', total)))
);
