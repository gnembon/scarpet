__command() -> __help();

__help() -> (
	p = player();
	print(player(), '======================');
	print(format('b Welcome to the cover app'));

	print(p, 'Cover x block with y block.');
	print(p, 'There are two modes available: continuous and region.');
	print(p, 'To select the region to affect right and left click with an iron sword to slect corners of the volume. Use /curves reset_positions to erase selection.');
	print(p, 'To define size of box around player in continuous mode, use /cover set_size and /cover set_offset. Toggle on or off with /cover continuous. ');
	print(p, 'Place block to cover in offhand and block to cover with in main hand.');
	print(p, 'To create a list of block pairs, put a shulker box in each hand. Items in corresponding slots will make cover pairs.');
	print(p, 'Undo the last n actions you did with /cover undo n. Use big numbers for continuos mode.');

	print(player(), '');
	print(player(), format('g Cover app by Firigion'));
	print(player(), format('') );
);

////// Make pairs

__make_pairs(player) -> (
	mainhand = query(player, 'holds', 'mainhand');
	offhand = query(player, 'holds', 'offhand');

	if(mainhand:0~'shulker_box' && offhand:0~'shulker_box', 
		//if both are shulkers, look at the contents
		__make_shulker_pairs(mainhand:2, offhand:2),
		//else look at the items themselves
		global_pairs = {offhand:0 -> mainhand:0}
	);
);

__make_shulker_pairs(mainhand_data, offhand_data) -> (
	mainhand_list = __get_item_list(mainhand_data);
	offhand_list = __get_item_list(offhand_data);

	global_pairs = {};
	loop( 27,
		if(mainhand_list:_ != null, global_pairs:(offhand_list:_) = mainhand_list:_ ) 
	);
);

__get_item_list(box_data) -> (
	item_tuple_list = parse_nbt(box_data:'BlockEntityTag':'Items');
	// empty list of 27 elements
	item_list = map(range(27), null);
	// fill with items in their slots
	for(item_tuple_list,
		item_list:(_:'Slot') = _:'id' - 'minecraft:'
	);
	return(item_list)
);


////// Genearl utils

__set_and_save(pos, material) -> ( //defaults to no replace
	global_this_story:length(global_this_story) = [pos, block(pos)];
	set(pos , material);
);

__place_if(block) -> (
	cover = global_pairs:str(block);
	if(cover != null,
		p = pos(block) +[0,1,0];
		if(air(p) && __check(block), __set_and_save(p, cover))
	);
);

__check(block) -> (
	if(	
		block ~ '_slab', return(property(block, 'type')=='top'),
		block ~ '_stairs', return(property(block, 'half')=='top'),
		// for any other block, true
		return(true)
	)
);

////// Continuous mode

global_box_halfsize = [10, 4, 10];
global_box_offset = 3;

// Set commands for global mox size
set_size(dx, dy, dz) -> global_box_halfsize = [dx, dy, dz]/2;
set_offset(offset) -> global_box_offset = offset;


__draw_box(player) -> (
	pos = player ~ 'pos';
	from = global_box_halfsize  + 0.5 - [0, global_box_offset + global_box_halfsize:1 -0.5 , 0];
	to = -1 * global_box_halfsize - 0.5- [0, global_box_offset + global_box_halfsize:1, 0];
	draw_shape('box', 2, 'color', 0x059915F0, 'fill', 0x05991550, 'from', from, 'to', to, 'follow', player)
);

global_continous_on = false;
continuous() -> (
	// togle state
	global_continous_on = !global_continous_on;
	if(global_continous_on, 
		p = player();
		dim = p ~ 'dimension';
		__make_pairs(p);
		__cover_player(p, dim) 
	);
	return('')
);

// Cover around the player
__cover_player(player, dim) -> (
	if(global_continous_on,
		global_this_story = [];
		result = scan( pos(player) - [0, global_box_offset + global_box_halfsize:1, 0], global_box_halfsize, __place_if(_));
		if(result, __put_into_history(global_this_story, dim) );

		__draw_box(player);
		schedule(1, '__cover_player', player, dim);
	);
);

// Turn off in a bunch of cases
__on_player_switches_slot(player, from, to) -> global_continous_on = false;

__on_player_swaps_hands(player) -> global_continous_on = false;

__on_player_disconnects(player, reason) -> global_continous_on = false;

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> global_continous_on = false;

__on_player_dies(player) -> global_continous_on = false;


////// Volume mode

region() -> (
	p = player();
	__make_pairs(p);
	
	if(global_all_set,
		task('__cover_region', p),
		print(format('rb Error: ', 'y You must select a region to cover first. Use an iron sword.') );
	);
	return('');
);

__cover_region(player) -> (
	dim = player ~ 'dimension';
	global_this_story = [];

	print(player, 'Covering...');
	result = volume(global_positions:dim:0, global_positions:dim:1, __place_if(_));
	print(player, 'Covered ' + result  + ' blocks');

	__put_into_history(global_this_story, dim);
);

// Spawn a marker
__mark(i, position, dim) -> (
 	colours = l('red', 'lime', 'light_blue'); 
	e = create_marker('pos' + i, position + l(0.5, 0.5, 0.5), colours:(i-1) + '_concrete', false); // crete the marker
	run(str( //modify some stuff to make it fancier
		'data merge entity %s {Glowing:1b, Fire:32767s}', query(e, 'uuid') 
		));
	global_armor_stands:dim:(i-1) =  query(e, 'id'); //save the id for future use
);

__remove_mark(i, dim) -> (
	e = entity_id(global_armor_stands:dim:(i));
 	if(e != null, modify(e, 'remove'));
);

// set a position
__set_pos(i) -> (
	dim = player() ~ 'dimension';

	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:dim:(i-1) = tha_pos; // save to global positions
	__all_set(dim); 
	
	print(str('Set your position %d in %s to ',i, dim) + tha_pos);

	// remove previous marker for set positi
	__remove_mark(i-1, dim); //-1 because stupid indexes
	__mark(i, tha_pos, dim);
);

// remove all markers
__reset_positions(dim) -> (
	loop(3, 
		__remove_mark(_, dim);
	);
	global_positions:dim = l(null, null, null);
	global_all_set:dim = false;
	global_armor_stands:dim = l(null, null, null);
);

reset_positions() -> (
	dim = player() ~ 'dimension';
	__reset_positions(dim);
);

// set position 1 if player left clicks with a golden sword
__on_player_clicks_block(player, block, face) -> (
	if(query(player(), 'holds'):0 == 'iron_sword',
		__set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(item_tuple:0 == 'iron_sword' && hand == 'mainhand',
		__set_pos(2)
	);
);

__all_set(dim) -> (
	if(all(slice(global_positions:dim, 0, 2), _!=null), global_all_set:dim = true);
	__render_box();
);

__render_box() -> (
	dim = current_dimension();
	if(global_all_set:dim,
		min_pos = map(range(3), min(global_positions:dim:0:_, global_positions:dim:1:_));
		max_pos = map(range(3), max(global_positions:dim:0:_, global_positions:dim:1:_));
		draw_shape('box', 6, 'color', 0xFFFFFF70 , 'fill', 0xFFFFFF20, 'from', min_pos, 'to', max_pos+1 );
		schedule(5, '__render_box')
	);
);

global_positions = m();
global_all_set = m();
global_armor_stands = m();

__reset_positions('overworld');
__reset_positions('the_nether');
__reset_positions('the_end');


////// Undo

global_undo_history_size = 12000; // 10 minutes worth of ticks to undo in continuous mode
global_history = {
					'overworld' -> [] ,
					'the_nether' -> [] ,
					'the_end' -> [] ,
				};

__put_into_history(story, dim) -> (
	global_history:dim:length(global_history:dim) = story;
	if(length(global_history:dim) > global_undo_history_size,
		delete(global_history:dim, 0)
	);
);

__undo(index, dim) -> (
	// iterate over the story backwards
	print('Undoing');
	for(range(length(global_history:dim:index)-1, -1, -1),
		set(global_history:dim:index:_:0, global_history:dim:index:_:1); // (position, block) pairs
	);
	// remove used story
	delete(global_history:dim, index);
);

undo(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0, 
		print(format('rb Error: ', 'y Need a positive number of steps to undo'));
		return('')
	);

	p = player();
	dim = p ~ 'dimension';
	
	index = length(global_history:dim)-num;
	if(index<0, 
		print(format('rb Error: ', str('y You only have %d actions to undo available', length(global_history:dim) ) )),
		task('__undo_asynch', num, length(global_history:dim)-1, dim, p )
	);
	return('')	
);

__undo_asynch(ammount, index, dim, player) -> (
	print(player, str('Undoing the last %d actions or ticks', ammount));
	loop(ammount, __undo(index, dim) );
	print(player, 'Done')
);
