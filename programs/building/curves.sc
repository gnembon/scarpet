__command() -> null;

// to store marker positions and object handles
global_settings = {
						 'show_pos' -> true ,
						 'paste_with_air' -> false ,
						 'circle_axis' -> 'y' ,
						 'wave_axis' -> 'xy' ,
						 'replace_block' -> false ,
						 'rotate' -> false , //TODO
						 'slope_mode' -> false ,
						 'max_template_size' -> 100 ,
						 'preview_enabled' -> false , //TODO
						 'undo_history_size' -> 100 ,
						 'max_operations_per_tick' -> 10000 ,
					};
global_block_alias = {
						'water_bucket' -> 'water' ,
						'lava_bucket' -> 'lava' ,
						'feather' -> 'air' ,
						'ender_eye' -> 'end_portal' ,
						'flint_and_steel' -> 'nether_portal',
					};
global_history = {
					'overworld' -> [] ,
					'the_nether' -> [] ,
					'the_end' -> [] ,
				};

// keeps track of how many blocks have been set this tick
global_set_count = 0;

////// Utilities ///////

__get_replace_block() -> (
	block = query(player(), 'holds', 'offhand'):0;
	alias = global_block_alias:block;
	if(alias==null, block, alias)
);

__set_and_save(pos, material) -> ( //defaults to no replace
	global_this_story:length(global_this_story) = [pos, block(pos)];
	set(pos , material);
);

__set_block(pos, material, replace_block) -> (
	__set_and_save(pos, material);
	if(global_set_count > global_settings:'max_operations_per_tick',
		global_set_count = 0;
		game_tick(20);
	);
);

__get_center() -> (
	dim = player() ~ 'dimension';
	if(global_positions:dim:2 == null,
		pos(player()),
		global_positions:dim:2
	)
);

__extend(list, extension) -> (
	len = length(list);
	for(extension, list:(len+_i) = _);
	return(list)
);

__reflect(list) ->  map(range(length(list)-1, -1, -1), list:_ );

// saves selected area, minus air
__make_template() -> (

	dim = player() ~ 'dimension';
	if(!global_all_set:dim,
		print(format('rb Error: ', 'y You need to make a selection first' )); 
		return(true) // dont paste anything 
	);
	
	pos0 = global_positions:dim:0;
	pos1 = global_positions:dim:1;
	
	global_template = l();
	origin = map(range(3), min(pos0:_, pos1:_)); //negative-most corner in all dimensions
	volume(
		pos0:0, pos0:1, pos0:2,
		pos1:0, pos1:1, pos1:2,
		if(global_settings:'paste_with_air',
			global_template:length(global_template) = [pos(_)-origin, _],
			if(!air(_), global_template:length(global_template) = [pos(_)-origin, _] ) //save non-air blocks and positions
		);
	);
	// Handle template size warning
	if(length(global_template) > global_settings:'max_template_size',
		print( format(
			'buy Warning',
			'y : ',
			'w Template is too big. Your tried to paste ', 
			str('by %d ', length(global_template)),
			str('w blocks %s, but max size is ', if(global_settings:'paste_with_air', '(counting air)', '(not counting air)') ),
			str('by %d', global_settings:'max_template_size' ),
			'w .\nTry increasing it with ',
			'b [this] ', '^t Click here!', '?/curves set_max_template_size 200',
			'w command.'
		) );
		true, // tempalte too big, don't paste it
		false // paste it
	)
);

// clone template at given position
__clone_template(pos, replace_block) -> (
	for(global_template, __set_block(pos + _:0, _:1, replace_block) );
);


__make_set_function(dim, material, replace_block) -> (
	if( material == 'template',
		// make with tempalte
		if(__make_template(), return() ); // retrn if tempalte was too big
		offset = map(global_positions:dim:0 - global_positions:dim:1, abs(_)) / 2; //offsets the selection so that it clones it in the center of the block

		__set_function(position, outer(replace_block), outer(offset)) -> __clone_template( position - offset, replace_block);
		,
	// else, make from block material
		__set_function(position, outer(material), outer(replace_block)) -> __set_block( position, material, replace_block);
	);
);



////// Helixes ///////

// ---- Utils ----

// mainly for debug porpuses
_circle(radius, material) -> (
	circ = __make_circle(radius);
	c = pos(player());
	for(circ, 
		set(c + l(_:0, 0, _:1), material); 
		create_marker(str(_i), c + l(_:0, 0, _:1))
	);	
);

__rotated90(list_to_rotate) -> ( //rotates 90 degrees
	map(list_to_rotate, l(_:1, -_:0))
);

__helix_get_step(circle, perimeter, advance_step, i) ->( //defaults to axis y
	circle_pos = circle:(i%perimeter);
	step = l(circle_pos:0, i * advance_step, circle_pos:1) ;
);

__make_circle(radius) -> (
	z_function(x, outer(radius)) -> round(sqrt(radius * radius - x*x));
	range_val = radius * cos(45); //this spans a quarter circle
	x_range = range(-range_val, range_val);
	
	quarter1 = map(x_range, l(_, z_function(_)) ); // starts with quarter circle
	half = __extend(quarter1, __rotated90(quarter1)); // add a quarter by rotating it 90 degrees
	__extend(half,__rotated90(__rotated90(half))); // rotate the half circle 180 degrees and add it
);

__assert_pitch(pitch) -> (
	if(pitch == 0, 
		print(format('rb Error: ', str('y %s must not be zero', if(global_settings:'slope_mode', 'Slope', 'Pitch')) ) );
		true,
		false
	);
	
);

// ---- Draw funtions ----

__preview_helix(circle, center, pitch, size, iterations_left) -> (
	
	perimeter = length(circle); // ammount of blocks in one revolution
	advance_step = if(global_settings:'slope_mode', pitch, pitch/perimeter); //pitch encodes slope if slope_mode == true
	
	loop(floor( size / advance_step) - 1 , //loop over the total ammount of helixes
		this_step = __helix_get_step(circle, perimeter, advance_step, _);
		next_step = __helix_get_step(circle, perimeter, advance_step, _+1);
		draw_shape('line', 15, 'from', center + this_step, 'to', center + next_step);
	);
	
	if(iterations_left > 0, 
		schedule(10, '__preview_helix', circle, center, pitch, size, iterations_left-1)
	);
);

preview_helix(radius, pitch, size, time) -> (
	if(__assert_pitch(pitch), return('') );
	
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	
	iterations = time * 2; // time is a value in seconds, will render every 10 gt
	__preview_helix(circle, center, pitch, size, iterations);	
);

//main funtion to draw helix from material
__draw_helix(circle, center, pitch, size, material) -> (
	
	dim = player() ~ 'dimension';
	
	if(__assert_pitch(pitch), return('') );

	perimeter = length(circle); // ammount of blocks in one revolution
	
	replace_block = __get_replace_block();
	advance_step = if(global_settings:'slope_mode', pitch, pitch/perimeter); //pitch encodes slope if slope_mode == true
	global_this_story = [];
	
	__make_set_function(dim, material, replace_block);

	//else, make out of material
	loop(floor( size / advance_step), //loop over the total ammount of helixes
		this_step =  __helix_get_step(circle, perimeter, advance_step, _);
		__set_function(center + this_step);
	);
		
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
);


helix(radius, pitch, size, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	__draw_helix(circle, center, pitch, size, material);
);

antihelix(radius, pitch, size, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = __reflect(circle); // to spin the other way around
	__draw_helix(circle, center, pitch, size, material);

);

multihelix(radius, pitch, size, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = __extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_helix(this_circ, center, pitch, size, material);
	);
);

antimultihelix(radius, pitch, size, ammount, material) -> (
	center = __get_center(); // center coordiantes
	circle = __make_circle(radius);
	circle = __reflect(circle); // to spin the other way around
	perimeter = length(circle); // ammount of blocks in one revolution
	loop(ammount,
		jump = floor(_ * perimeter/ammount); // by how many places to advance to get to the next circle
		this_circ = __extend(slice(circle, jump), slice(circle, 0, jump) ); // redefine the circle last for this iteration
		__draw_helix(this_circ, center, pitch, size, material);
	);
);

////// Spirals ///////

// ---- Utils ----

__arclength_poly(exponent, turns, radius) -> (
	radius * (360 * turns) ^ (exponent +1) / (exponent + 1 );
);

__arclength_exp(base, turns, radius) -> (
	radius * sqrt(1+ln(base)^2)/ln(base) * base^(360*turns - 1)
); 


// ---- Draw funtions ----

__draw_spiral(arclength, material) -> (

	dim = player() ~ 'dimension';
	center = __get_center();

	replace_block = __get_replace_block();
	global_this_story = [];
	position_list = [];
	
	// define function to set with
	__make_set_function(dim, material, replace_block);
	parameter = l( range(arclength) ) / arclength;

	for( parameter,
		position = map(center + __circular_parametric_curve_get_step( u(_) , v(_) , 0), floor(_) );
		// check for repeated spots
		if( position_list ~ position, 
			continue(),
			position_list:length(position_list) = position
		);
		__set_function(position);
	);

	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
	return('');

);

spiral(turns, exponent, radius, phase, material) -> (
	u(t, outer(exponent), outer(radius), outer(phase)) -> radius * t^exponent * cos(t + phase);
	v(t, outer(exponent), outer(radius), outer(phase)) -> radius * t^exponent * sin(t + phase);
	//__draw_spiral( __arclength_poly(exponent, turns, radius), material) 
	__arclength_poly(exponent, turns, radius)
);

logspiral(turns, base, radius, phase, material) -> (
	u(t, outer(base), outer(radius), outer(phase)) -> radius * base^t * cos(t + phase);
	v(t, outer(base), outer(radius), outer(phase)) -> radius * base^t * sin(t + phase);
);

antispiral(turns, exponent, radius, phase, material) -> (
	u(t, outer(exponent), outer(radius), outer(phase)) -> radius * t^exponent * cos(-t + phase);
	v(t, outer(exponent), outer(radius), outer(phase)) -> radius * t^exponent * sin(-t + phase);
);

antilogspiral(turns, base, radius, phase, material) -> (
	u(t, outer(base), outer(radius), outer(phase)) -> radius * base^t * cos(-t + phase);
	v(t, outer(base), outer(radius), outer(phase)) -> radius * base^t * sin(-t + phase);
);

////// Waves ///////

// ---- Utils ----

__wave_get_step(wave, len, current_offset, i) -> (
	wave_pos = wave:(i%len);
	this_step = l(wave_pos:0 + current_offset , wave_pos:1, 0) ;
);

__make_curve(L, A) -> (

	// make first quarter
	start_pos = pos( block(pos(player())) ); // to get integer coords
	x = range(L/4 + 1);
	curve = map(x, l(_, floor(A * sin(360 *_ / L)) ));
	curve = __fill_in(curve);
	
	//add second quarter
	quarter_size = curve:(length(curve)-1):0;
	reflected_quarter = map(curve, l(quarter_size * 2 + 1- _:0, _:1) ); 
	reflected_quarter = __reflect(reflected_quarter);
	curve = __extend(curve, reflected_quarter);
	
	//add last half
	half_size = curve:(length(curve)-1):0;
	reflected_half = map(curve, l(half_size * 2 - _:0, - _:1) );
	reflected_half = __reflect(reflected_half);
	delete(reflected_half, 0); // to avoid overlap where they meet
	delete(reflected_half, length(reflected_half)-1); // to avoid overlap with the next bit
	curve = __extend(curve, reflected_half);
);

__fill_in(in_list) -> (
	out_list = l();
	len = length(in_list);
	loop( len ,
	
		out_list:length(out_list) = in_list:_;
		
		if(_ < len, // dont run the last one
			d = in_list:(_+1):1 - in_list:_:1;
			i = _;
			loop( d -1 , 
				out_list:length(out_list) = in_list:i + l(0,_+1);
			);
		);
	);
	out_list;
);


// ---- Draw funtions ----

wave(wavelength, amplitude, size, material) -> (

	dim = player() ~ 'dimension';
	start = __get_center();
	
	wave = __make_curve(wavelength, amplitude);
	lenx = wave:(length(wave) - 1):0; //how long the wave is in one dimension
	len = length(wave);
	fractional_fit = size/lenx; //ammount of times the curve fits in target size
	
	replace_block = __get_replace_block();
	global_this_story = [];
	
	__make_set_function(dim, material, replace_block);

	loop( floor( fractional_fit * len ), 
	
		current_period = floor(_ / len);
		this_step = __wave_get_step(wave, len, current_period * lenx, _);
		
		__set_function(start + this_step);
	);

	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
	return('');
	
);


////// Circle wave ///////


// ---- Utils ----

__circular_parametric_curve_get_step(u, v, w) -> [u, w, v]; // defaults to y axis

__get_arclength_planar(R, A, k, arc) -> (
	// Arclength should be int(sqrt( (A * k * cos(k*t))^2 + (A * sin(k*t) + d)^2 ))
	// Doing this approximation means heavily oversampling the curve
	arclength = sqrt(A*A * k*k + A*A + R*R + 2*A*R) * arc; 
);

__get_arclength_tarsverse(R, A, k, arc) -> (
	// Arclength should be int(sqrt( R^2 + (A * k * cos(k*t))^2 ))
	// Doing this approximation means heavily oversampling the curve
	arclength = sqrt(R*R + A*A * k*k) * arc;
);

__get_param(arclength, arc) -> (
	parameter = arc * l( range(arclength) ) / arclength;
);

__create_planar_functions(R, A, k, from) -> (
	u(t, outer(A), outer(R), outer(k), outer(from) ) -> (R + A * sin(t * k) ) * cos(t + from);
	v(t, outer(A), outer(R), outer(k), outer(from) ) -> (R + A * sin(t * k) ) * sin(t + from);
	w(t) -> 0;
);

__create_transverse_functions(R, A, k, from) -> (
	u(t, outer(R), outer(from) ) -> R * cos(t + from);
	v(t, outer(R), outer(from) ) -> R * sin(t + from);
	w(t, outer(A), outer(k) ) -> A * sin( k * t );
);


// ---- Draw functions ----

__draw_circle_wave(parameter, material) -> (

	dim = player() ~ 'dimension';
	center = __get_center();

	replace_block = __get_replace_block();
	global_this_story = [];
	position_list = [];
	
	// define function to set with
	__make_set_function(dim, material, replace_block);

	for( parameter,
		position = map(center + __circular_parametric_curve_get_step( u(_) , v(_) , w(_)), floor(_) );
		// check for repeated spots
		if( position_list ~ position, 
			continue(),
			position_list:length(position_list) = position
		);
		__set_function(position);
	);

	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
	return('');

);

cwave_planar(radius, amplitude, cycles, material) -> (
	cwave_planar_partial(radius, amplitude, cycles, 0, 360, material);	
);

cwave_planar_partial(radius, amplitude, cycles, from, to, material) -> (
	
	arc = abs(to-from);
	arclength = __get_arclength_planar(radius, amplitude, cycles, arc);
	parameter = __get_param(arclength, arc);
	__create_planar_functions(radius, amplitude, cycles, from);
	__draw_circle_wave(parameter, material);

);

cwave_transverse(radius, amplitude, cycles, material) -> (
	cwave_transverse_partial(radius, amplitude, cycles, 0, 360, material);
);

cwave_transverse_partial(radius, amplitude, cycles, from, to, material) -> (
	
	arc = abs(to-from);
	arclength = __get_arclength_tarsverse(radius, amplitude, cycles, arc);
	parameter = __get_param(arclength, arc);
	__create_transverse_functions(radius, amplitude, cycles, from);
	__draw_circle_wave(parameter, material);

);


////// Stars ///////


// ---- Utils ----

__draw_line(p1, p2) -> (
 	m = p2-p1;
	max_size = max(map(m, abs(_)));
	t = l(range(max_size+1))/max_size;
	for(t, 
 		b = m * _ + p1;
 		__set_function(b);
 	);
);

// gets n equidistant points (pairs of coors) along a circle of radius R
__get_points(R, n, phase) -> (
	angle_step = 360/n;
	get_step(i, outer(R), outer(angle_step), outer(phase)) -> R * [ cos(i*angle_step + phase), sin(i*angle_step + phase)];
	map(range(n), get_step(_) );
);

__interlace_lists(l1, l2) -> (
	// asumes l1 and l2 of same length
	out = [];
	for(l1, 
		out:length(out) = _;
		out:length(out) =  l2:_i;
	);
	return(out);
);


// ---- Draw functions ----

star(outer_radius, inner_radius, n_points, phase, material) -> (

	dim = player() ~ 'dimension';
	center = __get_center();

	replace_block = __get_replace_block();
	global_this_story = [];
	position_list = [];

	// define function to set with
	__make_set_function(dim, material, replace_block);

	// get points in inner and pouter radius + interlace them
	inner_points = __get_points(inner_radius, n_points, phase);
	outer_points = __get_points(outer_radius, n_points, phase + 360/n_points/2);
	interlaced_list = __interlace_lists(inner_points, outer_points);
	interlaced_list:length(interlaced_list) = inner_points:0; // add first point at the end to close curve
	
	// get points and draw the connecting lines
	loop(length(interlaced_list)-1,
		p1 = center + __circular_parametric_curve_get_step(interlaced_list:_:0, interlaced_list:_:1, 0) ;
		p2 = center + __circular_parametric_curve_get_step(interlaced_list:(_+1):0, interlaced_list:(_+1):1, 0) ;
		__draw_line(p1, p2);		
	);
	
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
	return('');
);


////// Polygon ///////
// builds on star utils

polygon(radius, n_points, rotation, material) -> (

	dim = player() ~ 'dimension';
	center = __get_center();

	replace_block = __get_replace_block();
	global_this_story = [];
	position_list = [];

	// define function to set with
	__make_set_function(dim, material, replace_block);

	// get points in inner and pouter radius + interlace them
	points = __get_points(radius, n_points, rotation);
	points:length(points) = points:0; // add first point at the end to close curve
	
	// get points and draw the connecting lines
	loop(length(points)-1,
		p1 = center + __circular_parametric_curve_get_step(points:_:0, points:_:1, 0) ;
		p2 = center + __circular_parametric_curve_get_step(points:(_+1):0, points:(_+1):1, 0) ;
		__draw_line(p1, p2);		
	);
	
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Set %d blocks', length(global_this_story) ));
	return('');
);


////// Soft replace ///////

__get_states(b) -> (
	properties = block_properties(pos(b));
	pairs = map(properties, l(_, property(pos(b), _)) );
);

__make_properies_string(pairs) -> (
	if( pairs,
		props_str = join(',', map(pairs, str('%s="%s"', _:0, _:1)) ),
		props_str = '',
	);
);

__set_with_state(b, replace) -> (
	pairs = __get_states(b);
	properties_string = __make_properies_string(pairs);
	__set_and_save(b, block(str('%s[%s]', replace, properties_string)) );
);

__replace_one_block(b, to_replace, replace_with) -> (
	if(b == to_replace, 
		__set_with_state(b, replace_with)
	);
);

__replace_one_block_filt(b, to_replace, replace_with, property, value) -> (
	if(b == to_replace && property(b, property)==value, 
		__set_with_state(b, replace_with)
	);
);

soft_replace() -> (
	to_replace = __get_replace_block();
	replace_with = query(player(), 'holds', 'mainhand'):0;

	dim = player() ~ 'dimension';
	if(!global_all_set:dim,
		print(format('rb Error: ', 'y You need to make a selection first' )); 
		return('') // dont paste anything 
	);
	
	global_this_story = [];

	pos0 = global_positions:dim:0;
	pos1 = global_positions:dim:1;

	volume(
		pos0:0, pos0:1, pos0:2,
		pos1:0, pos1:1, pos1:2,
		(
			__replace_one_block(_, to_replace, replace_with)
		);
	);	
	print(global_this_story);
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Replaced %d %s blocks with %s', length(global_this_story), to_replace, replace_with ));
);

soft_replace_filt(property, value) -> (
	to_replace = __get_replace_block();
	replace_with = query(player(), 'holds', 'mainhand'):0;

	dim = player() ~ 'dimension';
	if(!global_all_set:dim,
		print(format('rb Error: ', 'y You need to make a selection first' )); 
		return('') // dont paste anything 
	);
	
	global_this_story = l();
	
	print(str('Replacing %s with %s', to_replace, replace_with));
	volume(global_positions:0:0, global_positions:0:1, global_positions:0:2,
		 global_positions:1:0, global_positions:1:1, global_positions:1:2,
		(
			__replace_one_block_filt(_, to_replace, replace_with, property, value)
		);
	);
	
	__put_into_history(global_this_story, dim); //ensure max history size is not exceeded
	print(str('Replaced %d blocks', length(global_this_story) ));
);


////// Brush ///////

__paste_brush() -> (
	dim = player() ~ 'dimension';
	looking_at = query(player(), 'trace', 200, 'blocks');
	if(looking_at == null, return(''), block_pos = pos(looking_at) );
	replace_block = __get_replace_block();
	if(__make_template(), return() ); //tempalte was too big
	offset = map(global_positions:dim:0 - global_positions:dim:1, abs(_)) / 2; //offsets the selection so that it clones it in the center of the block
	global_this_story = l();
	__clone_template( block_pos - offset, replace_block);
);


////// Settings'n stuff ///////

set_max_template_size(value) -> (
	if( type(value) == 'number' && value > 0, 
		global_settings:'max_template_size' = value;
		print(format(str('b Max tempalte size value set to %s', value) ) ),
		print(format('rb Error: ', 'y Max template size should be a positive number') )
	);
	return('')
);

set_max_operations_per_tick(value) -> (
	if( type(value) == 'number' && value > 0, 
		global_settings:'max_template_size' = value;
		print(format(str('b max operations per tick set to %s', value) ) ),
		print(format('rb Error: ', 'y Max operations per tick should be a positive number') )
	);
	return('')
);

set_circle_axis(axis) -> (
	if( ( l('x','y','z')~axis ) == null, 
		print(format('rb Error: ', 'y Axis must be one of ', 'yb x, y ', 'y or ', 'yb z'));
		return('')
	);
	global_settings:'circle_axis' = axis;
	if( axis == 'x',
		__helix_get_step(circle, perimeter, advance_step, i) ->(
			circle_pos = circle:(i%perimeter);
			step = l(i * advance_step, circle_pos:0, circle_pos:1) ;
		);
		__circular_parametric_curve_get_step( u , v , w) -> l(w, u, v);
		,
		axis == 'y',
		__helix_get_step(circle, perimeter, advance_step, i) ->( //defaults to axis y
			circle_pos = circle:(i%perimeter);
			step = l(circle_pos:0, i * advance_step, circle_pos:1)
		);
		__circular_parametric_curve_get_step( u , v , w) -> l(u, w, v);
		,
		axis == 'z',
		__helix_get_step(circle, perimeter, advance_step, i) ->(
			circle_pos = circle:(i%perimeter);
			step = l(circle_pos:0, circle_pos:1, i * advance_step) ;
		);
		__circular_parametric_curve_get_step( u , v , w) -> l(u, v, w);
	);
	print(format(str('b Circular curves will now generate perpendicular to the %s axis', axis) ) );
	return('')
);

set_wave_axis(axis) -> (
	if( ( l('xy', 'xz', 'yx' ,'yz','zx', 'zy')~axis ) == null, 
		print(format('rb Error: ', 'y Axis must be one of ', 'yb xy, xz, yx, yz, zx ', 'y or ', 'yb zy'));
		return('')
	);
	global_settings:'wave_axis' = axis;
	if( axis == 'xy',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l(wave_pos:0 + current_offset , wave_pos:1, 0) ;
		),
		axis == 'xz',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( wave_pos:0 + current_offset , 0, wave_pos:1) ;
		),
		axis == 'yx',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( wave_pos:1, wave_pos:0 + current_offset , 0) ;
		),
		axis == 'yz',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( 0, wave_pos:0 + current_offset , wave_pos:1) ;
		),
		axis == 'zx',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( wave_pos:1, 0, wave_pos:0 + current_offset ) ;
		),
		axis == 'zy',
		__wave_get_step(wave, len, current_offset, i) -> (
			wave_pos = wave:(i%len);
			this_step = l( 0, wave_pos:1, wave_pos:0 + current_offset) ;
		),

	);
	print(format(str('b Waves will now generate along the %s axis and into the %s axis', slice(axis, 0, 1), slice(axis, 1)) ) );
	return('')
);


set_undo_histoy_size(value) -> (
	if( type(value) == 'number' && value > 0, 
		global_settings:'undo_history_size' = value;
		print(format(str('b Max undo value set to %s', value) ) ),
		print(format('rb Error: ', 'y Undo history size should be a positive number') )
	);
	index = length(global_history) - global_settings:'undo_history_size';
	if( index>0 , global_history = slice(global_history, index) );
	return('')
);

toggle_paste_with_air() -> (
	global_settings:'paste_with_air' = !global_settings:'paste_with_air';
	if(global_settings:'paste_with_air',
		print('Template will now be pasted with air'),
		print('Template will now be pasted without air')
	);
	return('')
);

toggle_replace_block() -> (
	global_settings:'replace_block' = !global_settings:'replace_block';
	if(global_settings:'replace_block',
		//if replace blocks
		print( format('b Curves will now only replce block in your offhand.\n',
			'g Hold bucket for liquids, feather for air, ender eye for end portal, and flint and steel for nether portal.') );
		__set_block(pos, material, replace_block) -> if(block(pos) == replace_block, __set_and_save(pos, material) ),
		//else
		print(format( 'b Curve will paste completly, replacing whatever is there.') );
		__set_block(pos, material, replace_block) -> __set_and_save(pos, material)
	);
	return('')
);

toggle_slope_mode() -> (
	global_settings:'slope_mode' = !global_settings:'slope_mode';
	if(global_settings:'slope_mode',
		print(format('b Second argument of helix commands is now slope (in blocks)') ),
		print(format('b Second argument of helix commands is now pitch (separation between revolutions)') )
	);
	return('')
);

// generate interactive string for togglable parameter
__make_toggle_setting(parameter, hover) -> (
	str_list = l(
		str('w * %s: ', parameter), 
		str('^y %s', hover),
	);
	str_list = __extend(str_list, __get_button('true', parameter) );
	str_list = __extend(str_list, __get_button('false', parameter) );
	print(player(), format(str_list))
);

__get_active_button(value) -> (
	l( str('yb [%s] ', value) )
);

__get_inactive_button(value, parameter) -> (
	l( 
		str('g [%s] ', value),
		str('^gb Click to toggle'),
		str('!/curves toggle_%s', parameter)
	)
);

__get_button(value, parameter) -> (
	bool_val = if(bool(value), global_settings:parameter, !global_settings:parameter);
	if( bool_val, __get_active_button(value), __get_inactive_button(value, parameter) )
);

// generate interactive string for parameter with options
__make_value_setting(parameter, hover, options, has_arbitrary_values) -> (
	str_list = l(
		str('w * %s: ', parameter), 
		str('^y %s', hover),
	);
	options_list = [];
	map( options, 
			len = length(options_list);
			options_list:len = str('%sb [%s]', if(global_settings:parameter == _, 'y', 'g',), _);
			options_list:(len+1) = '^bg Click to set this value';
			options_list:(len+2) = str('?/curves set_%s %s', parameter, _) 
	);
	str_list = __extend(str_list, options_list);
	if( has_arbitrary_values, 
		current_val = ['w \ | ', str('e %s', global_settings:parameter), '^e Current value'];
		str_list = __extend(str_list, current_val) 
	);
	print(player(), format( str_list ))
);

// print all settings
settings() -> (
	print(player(), '======================');
	print(player(), format( 'b General settings:' ));
	__make_toggle_setting('show_pos', 'Shows markers and outlines selection');
	__make_toggle_setting('paste_with_air', 'Includes air when pasting template');
	__make_toggle_setting('replace_block', 'Shapes will only be generated replacing block in offhand');
	__make_value_setting('max_template_size', 'Limits template size to avoid freezing the game if you mess up the selection', [20, 100, 1200] , true);
	__make_value_setting('undo_history_size', 'Sets the maximum ammount of actions to undo', [10, 100, 500] , true);
	__make_value_setting('max_operations_per_tick', 'Sets the maximum ammount of operations per gametick', [2000, 10000, 50000] , true);
	print(player(), format( 'b Shapes settings:' ));
	__make_toggle_setting('slope_mode', 'Defines behaviour of second argument in helix definitions: slope or pitch');
	__make_value_setting('circle_axis', 'Axis along which circular stuff is generated. Affects stars, helixes and cwaves', ['x', 'y', 'z'] , false);
	__make_value_setting('wave_axis', 'Axis along which and into which waves are generated', ['xy', 'xz', 'yx' ,'yz','zx', 'zy'] , false);
	print(player(), '');
	return('')
);


////// Undo stuff ///////

__put_into_history(story, dim) -> (
	global_set_count = 0; // reset global count
	global_history:dim:length(global_history:dim) = story;
	if(length(global_history:dim) > global_settings:'undo_history_size',
		delete(global_history:dim, 0)
	);
);

__undo(index, dim) -> (
	// iterate over the story backwards
	for(range(length(global_history:dim:index)-1, -1, -1),
		set(global_history:dim:index:_:0, global_history:dim:index:_:1); // (position, block) pairs
	);
	// remove used story
	delete(global_history:dim, index);
);

go_back_stories(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0, 
		print(format('rb Error: ', 'y Need a positive number of steps to go to'));
		return('')
	);
	
	dim = player() ~ 'dimension';

	index = length(global_history:dim)-num;
	if(index<0, 
		print(format('rb Error: ', str('y You only have %d actions available to undo', length(global_history:dim) ) ));
		return('')
	);
	
	__undo(index, dim);
	print(str('Undid what you did %s actions ago', num ));	
);

undo(num) -> (
	//check for valid input
	if( type(num) != 'number' || num <= 0, 
		print(format('rb Error: ', 'y Need a positive number of steps to undo'));
		return('')
	);

	dim = player() ~ 'dimension';
	
	index = length(global_history:dim)-num;
	if(index<0, 
		print(format('rb Error: ', str('y You only have %d actions to undo available', length(global_history:dim) ) ));
		return('')
	);
	
	loop(num, __undo(length(global_history:dim)-1, dim) );
	print(str('Undid the last %d actions', num) );
);


////// Handle Markers //////

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
set_pos(i) -> (
	dim = player() ~ 'dimension';
	
	try( // position index must be 1, 2 or 3 
 		if( !reduce(range(1,4), _a + (_==i), 0),
			throw();
		),
		print(format('rb Error: ', 'y Input must be either 1, 2 or 3 for position to set. You input ' + i) );
		return()
	);
	// position to be set at the block the player is aiming at, or player position, if there is none
	tha_block = query(player(), 'trace');
	if(tha_block!=null,
		tha_pos = pos(tha_block),
		tha_pos = map(pos(player()), round(_))
	);
	global_positions:dim:(i-1) = tha_pos; // save to global positions
	__all_set(dim); 
	
	print(str('Set your position %d in %s to ',i, dim) + tha_pos);

	if(global_settings:'show_pos', // remove previous marker for set positi, if aplicable
		__remove_mark(i-1, dim); //-1 because stupid indexes
		__mark(i, tha_pos, dim);
	);

);

// print list of positions
get_pos() -> (
	dim = player() ~ 'dimension';
	for(global_positions:dim, 
 		print(str('Position %d is %s', 
				_i+1, if(_==null, 'not set', _)));
 	)
);

// toggle markers and bounding box visibility
toggle_show_pos() ->(
	dim = player() ~ 'dimension'; 
	global_settings:'show_pos' = !global_settings:'show_pos'; 
	if(global_settings:'show_pos',
		( // summon the markers
			for(global_positions:dim, 
				if(_!=null, __mark( (_i+1) , _, dim) );
			);
			print('Positions are now shown');
		),
		// else
		( //remove the markers
			for(global_armor_stands:dim, 
				__remove_mark(_i, dim);
			);
			print('Positions are now hidden');
		);
	);
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
	if(query(player(), 'holds'):0 == 'golden_sword',
		set_pos(1);
	);
);

// set position 2 if player right clicks with a golden sword
__on_player_uses_item(player, item_tuple, hand) -> (
	if(item_tuple:0 == 'golden_sword' && hand == 'mainhand',
		if(query(player(), 'sneaking'),
			set_pos(3),
			set_pos(2)
		),
	item_tuple:0 == 'blaze_rod' && hand == 'mainhand',
		__paste_brush();
	);
);

__all_set(dim) -> (
	if(all(slice(global_positions:dim, 0, 2), _!=null), global_all_set:dim = true);
	__render_box();
);

__render_box() -> (
	dim = current_dimension();
	if(global_all_set:dim && global_settings:'show_pos',
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


////// Handle Markers //////

help() -> (
	print(player(), '======================');
	print(player(), format( 'b Help:' ));
	print(player(), 'Click the following options to get category specific help. All curves commands will tell you the arguments they require if you run them without arguments.');
	__make_help_categories_buttons(['Positions', 'Undo', 'Brush', 'Replace', 'Curves']);
	print(player(), '');
	print(player(), format('g Curves app by Firigion'));
	print(player(), format('') );
);

__make_help_categories_buttons(categories) -> (
	cat_list = [];
	for( categories, 
		len = length(cat_list);
		cat_list:len = str('qb [%s] ', _);
		cat_list:(len+1) = '^bg Click to see this category';
		cat_list:(len+2) = str('!/curves help_%s', lower(_)) 
	);
	print(player(), format(cat_list) );
);

help_positions() -> (
	print(player(), '======================');
	print(player(), format( 'b Positions help:' ));
	description1 = ['w You have three positions to set. Positions 1 and 2 define a selection, while position 3 defines the center or origin of the curves. ' ,
					'w To set them you can use ',
					'yb /curves set_pos i ',
					'^bg Click to run!',
					'?/curves set_pos ',
					'w where i should be 1, 2 or 3. ',
					];
	description2 = ['w Alternatively, you can also grab a ',
					'db golden sword ',
					'^bg Click to get one!',
					str('!/give %s golden_sword{display:{Name:\'{"text":"Marker","color":"gold"}\',Lore:[\'{"text":"Click to set positions","color":"gray"}\']},Enchantments:[{}]} 1', player() ),
					'w and use left and right click to set positions 1 and 2, and shift right click to set position 3. ',
					'w Note that you can left click in mid air, but not right click. ',
					];
					
	description3 = ['w To delete selections and positions, use ',
					'yb /curves reset_positions',
					'^bg Click to run!',
					'?/curves reset_positions',
					'w . You can toggle positions and selection rendering on and off in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w .'
					];

	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), format(description3) );
	print(player(), '');
);

help_undo() -> (
	print(player(), '======================');
	print(player(), format( 'b Undo help:' ));
	description1 = ['w All actions done by this app are saved into history. You can undo any number of actions with ' ,
					'yb /curves set_pos n ',
					'^bg Click to run!',
					'?/curves undo ',
					'w where n should be the ammount of actions to undo, of course. ',
					];					
	description2 = ['w You can also use  ',
					'yb /curves go_back_stories n ',
					'^bg Click to run!',
					'?/curves go_back_stories ',
					'w to skip over a bunch of actions and undo a specific one. For example, n=3 will undo the action you did three things ago without touching the last two you did. ',
					'w Bare in mind that the undo history will be lost after you close the world, so be careful.'
					];
	description3 = ['w Undo histoy has a maximum size of 100 actions, which you can change in  ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w .'
					];

	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), format(description3) );
	print(player(), '');
);

help_brush() -> (
	print(player(), '======================');
	print(player(), format( 'b Brush help:' ));
	description1 = ['w A very simple functionality where right clicking with a blaze rod will plop wahtever selection you have created. ' ,
					'w The app will ray trace and place in on the first block it encounters, so you can paste your selection at a distance.'
					];
	print(player(), format(description1) );
	print(player(), '');
);

help_replace() -> (
	print(player(), '======================');
	print(player(), format( 'b Soft Replace help:' ));
	description1 = ['w This functionality allows you to replace a block with another one, but keeping its block properties. ' ,
					'w This means that if you replace cobble stairs with oak stairs, all the new stairs will have the orientation the old ones had.'
					];
	description2 = ['w To use it, put the block you want to replace in the off hand, the block you want to replace with in the main hand and select the area in which to replace stuff. ' ,
					'w Then, you can use ',
					'yb /curves soft_replace ',
					'^bg Click to run!',
					'?/curves soft_replace',
					'w to simply excecute the command or ',
					'yb /curves soft_replace_filt <property> <value> ',
					'^bg Click to run!',
					'?/curves soft_replace_filt ',
					'w to only replace blocks that have a given property set to that value. ',
					'w For example, setting property to ',
					'b half ',
					'w and value to ',
					'b top ',
					'w will only replace top stairs.'
					];
	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), '');
);

help_curves() -> (
	print(player(), '======================');
	print(player(), format( 'b Curves help:' ));
	description1 = ['w The app comes with a bunch of curves options, some of them have themselves a few variations. ',
					'w All curves have a few parameters that define them and a last parameter that sets the material the curve is made of. ',
					'w That material can be any minecraft block, or can be ',
					'b template',
					'w . If you choose template as material, the app will use your selection as a template and paste it along the curve. ',
					'w You can choose to have the template include or exclude air in the selection, which you can toggle in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w .'
					];					
	description2 = ['w There you can also toggle the option to have the curves only be generated replacing certain blocks. ',
					'w Place whatever block you want to replace in your offhand and run the curve command. ',
					'w Some blocks have aliases: bucket for liquids, feather for air, ender eye for end portal, and flint and steel for nether portal.'
					];
	description3 = ['w For more detailed info on how to use each curve, click on the list below. ',
					'w You can also jsut run the curve commands without parameters and the game will tell you what aprameters it needs. ',
					];
	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), format(description3) );
	__make_help_categories_buttons(['Helix', 'Wave', 'CWave', 'Star', 'Polygon']);
	print(player(), '');
);

help_helix() -> (
		print(player(), '======================');
	print(player(), format( 'b Helixes help:' ));
	description1 = ['w Helixes are defined by their radius, how fast they grow and how tall they are. ',
					'w The second argument can be either ',
					'b pitch ',
					'w or ',
					'b slope',
					'w , which you can set in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w . In the former case, you you set how many blocks the helix advances each time it completes a full revolution. ',
					'w In the latter, you define how many blocks it advances each block. '
					];
	description2 = ['w You can set the diretion along which the helix forms with the ',
					'b circle_axis ',
					'w setting.'
					];
	description3 = ['w Helixes come in four flavours: regular helix, antihelix, which is just the helix rotating in the opposite direction, and multihelix variations of those. ',
					'w Multihelixes take a fourth argument that set how many helixes to generate. They will evenly spaced along the circle. '
					];
	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), format(description3) );
	print(player(), '');
);

help_wave() -> (
		print(player(), '======================');
	print(player(), format( 'b Waves help:' ));
	description1 = ['w Waves are defined by their wavelength (how many blocks it takes to complete an oscilation), ',
					'w their amplitude (how big a half oscilation is), and their size (how long or tall the whole shape is). ',
					];
	description2 = ['w Waves generate along an axis and into another axis, both of which you can choose in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w . For example, setting ',
					'b wave_axis ',
					'w to ',
					'b xy ',
					'w means you will get a vertical wave that extends into the (positive) x axis, while ',
					'b xz ',
					'w makes a horizontal wave along the x axis.'
					];
	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), format(description3) );
	print(player(), '');
);


help_cwave() -> (
		print(player(), '======================');
	print(player(), format( 'b Circular waves help:' ));
	description1 = ['w CWaves, or circular waves are just waves that go around in a circle. ',
					'w They are defined by the radius of the circle, the amplitude of the wave and the amount of oscilations to have in a full circle. ',
					];
	description2 = ['w You can set the direction perpendicular to the circle with the ',
					'b circle_axis ',
					'w setting in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w .'
					];
	description3 = ['w Circular waves come in two flavours: ',
					'yb planar ',
					'^bg Click to run!',
					'?/curves cwave_planar ',
					'w and ',
					'yb transverse ',
					'^bg Click to run!',
					'?/curves cwave_transverse ',
					'w . The former will create the wave in the plane defined by the circle, while the latter will create it perpendicular to it. ',
					'w Both flavours come with a ',
					'b partial ',
					'w option. Partial cwaves take two extra arguments defining the starting and finishing angle to the circle, ',
					'w so you can make just a half circle, or two full cycles, if you choose.'
					];
	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), format(description3) );
	print(player(), '');
);



help_star() -> (
		print(player(), '======================');
	print(player(), format( 'b Stars help:' ));
	description1 = ['w Stars are defined by the outer radius, the inner radius, the number of points N and the phase or rotation. ',
					'w The way they are constructed is by selecting N points along the circle with outer radius, anther N points along the inner circle and connecting them with straight lines. ',
					'w This means that setting inner radius equal to outer radius will generate polygons with 2N sides, and setting N to 2 will generate a rhombus.'
					];
	description2 = ['w You can set the direction perpendicular to the star with the ',
					'b circle_axis ',
					'w setting in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w .'
					];

	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), '');
);


help_polygon() -> (
		print(player(), '======================');
	print(player(), format( 'b Polygon help:' ));
	description1 = ['w Polygons are defined the radius, the number of points N and the phase or rotation. ',
					'w They way they are constructed is simply by placing equidistant points in a circle and connecting them with straight lines, so all polygons will be regular.'
					];
	description2 = ['w You can set the direction perpendicular to the star with the ',
					'b circle_axis ',
					'w setting in ',
					'tb settings',
					'^bg Click to run!',
					'!/curves settings',
					'w .'
					];

	print(player(), format(description1) );
	print(player(), format(description2) );
	print(player(), '');
);
