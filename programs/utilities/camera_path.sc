script run 
$ start_path() -> 
$(
$	p = player();
$	global_points = l(l(l(p~'x',p~'y',p~'z',p~'pitch',p~'yaw'),0,'sharp'));
$	str('Started path at %.1f %.1f %.1f', p~'x', p~'y', p~'z')
$);
$
$ add_point(delay) -> 
$( 
$	p = player(); 
$	mode = 'sharp';
$	'mode is currently unused, run_path does always sharp, gauss interpolator is always smooth';
$	' but this option could be used could be used at some point by more robust interpolators';
$	vector = l(p~'x',p~'y',p~'z', p~'pitch', p~'yaw');
$	__add_path_segment(vector, delay, mode);
$	str('Added point %d: %.1f %.1f %.1f', length(global_points), p~'x', p~'y', p~'z')
$);
$
$ __add_path_segment(vector, duration, mode) -> 
$(
$	if ( (l('sharp','smooth') ~ mode) == null, exit('use smooth or sharp point'));	
$	if(!global_points, exit('Cannot add point to path that didn\'t started yet!'));
$	l(v, end_time, m) = element(global_points, -1);
$   put(vector,-1, __adjusted_rot(element(v, -1), element(vector, -1)));
$	global_points += l(vector, end_time+duration, mode)
$);
$
$'adjusts current rotation so we don\'t spin around like crazy';
$ __adjusted_rot(previous_rot, current_rot) -> 
$(
$	while( abs(previous_rot-current_rot) > 180, 1000,
$		current_rot += if(previous_rot < current_rot, -360, 360)
$	);
$	current_rot
$);
$
$
$ loop_path(times, last_section_duration) -> 
$(
$	positions = map(global_points, k = element(_, 0));
$	modes = map(global_points, element(_, -1));
$	durations = map(global_points, element(element(global_points, _i+1), 1)-element(_, 1));
$	put(durations, -1, last_section_duration);
$	loop(times,
$		loop( length(positions),
$			__add_path_segment(element(positions, _), element(durations, _), element(modes, _))
$		)
$	);
$	str('Add %d points %d times', length(positions), times)
$);
$
$ adjust_speed_percentage(percentage) ->
$(
$	if (percentage < 25 || percentage > 400, 
$		exit('path speed can only be speed, or slowed down 4 times. Recall command for larger changes')
$	);
$	ratio = percentage/100;
$	for(global_points, put(_, 1, element(_, 1)*ratio ) )
$);
$
$ select_interpolation(method) ->
$(
$	'each supported method needs to specify its run_path to track the player, accepting fps(speed), as parameter';
$	'and _show_path_tick, which is one tick of diplay of complete path give the particle as a parameter';
$	if (
$		method == 'linear',
$		(
$			run_path(fps) -> __run_path_linear(fps); 
$			_show_path_tick(particle) -> __show_path_tick_linear(particle)
$		),
$		method ~ '^gauss_',
$		(
$			type = method - 'gauss_';
$			global_gauss_type = if(type=='auto',0,number(type));
$			run_path(fps) -> __run_path_gauss(fps);
$			_show_path_tick(particle) -> __show_path_tick_gauss(particle)
$		),
$		'Choose one of the following methods: linear, gauss:auto, gauss:<deviation>'
$	)
$);
$ select_interpolation('linear');
$
$ show_path() -> 
$(
$	loop(100,
$		_show_path_tick('dust 0.9 0.1 0.1 1');
$		game_tick(50)
$	);
$	game_tick(1000);
$	'Done!'
$);
$
$ __show_path_tick_linear(particle) -> 
$(
$	if(!global_points, exit('Cannot show path that doesn\'t exist'));
$	if(length(global_points)<2, exit('Path not complete - add more points'));
$	loop(length(global_points)-1,
$		l(x1, y1, z1) = slice(element(element(global_points,_),0),0,3);
$		l(x2, y2, z2) = slice(element(element(global_points,_+1),0),0,3);
$		particle_line(particle, x1, y1, z1, x2, y2, z2, 10.0)
$	);
$	'ok'
$);
$
$ __run_path_linear(fps) -> 
$(
$	p = player();
$	if(!global_points, exit('Cannot show path that doesn\'t exist'));
$	if(length(global_points)<2, exit('Path not complete - add more points'));
$	loop(length(global_points)-1,
$		l(va, start, mode_a) = element(global_points,_);
$		l(vb, end, mode_b)   = element(global_points,_+1);
$		section = end-start;
$       loop(section,
$			dt = _/section;
$			v = dt*vb+(1-dt)*va;
$			modify(p, 'pos', slice(v,0,3));
$			modify(p, 'pitch', element(v,3));
$			modify(p, 'yaw', element(v,4));
$			game_tick(1000/fps)
$		)
$	);
$	game_tick(1000);
$	'Done!'
$);
$
$
$'(1/sqrt(2*pi*d*d))*euler^(-((x-miu)^2)/(2*d*d))   but we want one peak';
$ __norm_prob(x, miu, d) -> euler^(-((x-miu)^2)/(2*d*d));
$
$ __find_position(from_index, point) -> 
$(
$	dev = global_gauss_type;
$	components = l();
$	try(
$		for(range(from_index+1, length(global_points)),
$			l(v,ptime,mode) = element(global_points, _);
$			dev = if (global_gauss_type > 0, global_gauss_type, 
$				devs = l();
$				if (_+1 < length(global_points), devs += element(element(global_points, _+1),1)-ptime);
$				if (_-1 >= 0, devs += ptime-element(element(global_points, _-1),1));
$				0.6*reduce(devs, _a+_, 0)/length(devs)
$			);
$			impact = __norm_prob(point, ptime, dev);
$			if(rtotal && impact < 0.000001*rtotal, throw(null));
$			components += l(v, impact);
$			rtotal += impact
$		)
$		,null
$	);
$	try(
$		for(range(from_index, -1, -1),
$			l(v,ptime,mode) = element(global_points, _);
$			dev = if (global_gauss_type > 0, global_gauss_type, 
$				devs = l();
$				if (_+1 < length(global_points), devs += element(element(global_points, _+1),1)-ptime);
$				if (_-1 >= 0, devs += ptime-element(element(global_points, _-1),1));
$				0.6*reduce(devs, _a+_, 0)/length(devs)
$			);
$			impact = __norm_prob(point, ptime, dev);
$			if(ltotal && impact < 0.000001*ltotal, throw(null));
$			components += l(v, impact);
$			ltotal += impact
$		)
$		,null
$	);
$	total = rtotal+ltotal;
$	reduce(components, _a+element(_,0)*(element(_,1)/total), l(0,0,0,0,0))
$);
$
$ __run_path_gauss(fps) -> 
$(  
$	p = player();
$	if(!global_points, exit('Cannot show path that doesn\'t exist'));
$	if(length(global_points)<2, exit('Path not complete - add more points'));
$	loop(length(global_points)-1, i = _;
$		start = element(element(global_points,i),-2);
$		end = element(element(global_points,i+1),-2);
$		section = end-start;
$		loop(section,
$			v = __find_position(i, start+_);
$			modify(p, 'pos', slice(v,0,3));
$			modify(p, 'pitch', element(v,3));
$			modify(p, 'yaw', element(v,4));
$			game_tick(1000/fps)
$		)
$	);
$	game_tick(1000);
$	'Done!'
$);
$
$ __show_path_tick_gauss(particle) ->
$(
$	if(!global_points, exit('Cannot show path that doesn\'t exist'));
$	if(length(global_points)<2, exit('Path not complete - add more points'));
$	particle_chance = element(element(global_points,-1),-2)/50;
$	'50 particles per tick';
$	loop(length(global_points)-1, i = _;
$		start = element(element(global_points,i),-2);
$		end = element(element(global_points,i+1),-2);
$		section = end-start;
$		loop(section, point = _;
$			if( !rand(particle_chance),
$				l(x,y,z) = slice(__find_position(i, start+point),0,3);
$				particle(particle, x, y, z, 1, 0, 0)
$			)	
$		)
$	);
$	'ok'
$)
$
