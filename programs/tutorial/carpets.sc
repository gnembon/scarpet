global_markers = l();
activate() ->
(
	deactivate();
	p = player();
	colors = l(
		'white','yellow','orange','red',
		'pink','magenta','purple','blue',
		'light_blue', 'cyan','green','gray',
		'light_gray'
	);
	global_markers = map( colors,
		marker = create_marker(null, pos(p), _+'_carpet');
		entity_event(
			marker, 'on_tick', '__marker_tick', 
			str(p), _i, length(colors));
		marker
	)
);

 __on_player_starts_sneaking(player)->
(
	if (global_markers, deactivate(), activate())
);

__marker_tick(carpet_entity, player_name, i, N) ->
(
	p = player(player_name);
	if (!p,
		modify(p, 'remove');
		return()
	);
	l(cx, cy, cz) = pos(p);
	cy += (p ~ 'eye_height') - (carpet_entity ~ 'eye_height');
	time = tick_time();
	angle = (360*i/N+tick_time()) % 360;
	modify(carpet_entity,'pos',
		cx+(2.1+sin(2*time)/5)*sin(angle),
		cy+sin(16*angle)/10,
		cz+(2.1+sin(2*time)/5)*cos(angle)
	);
	modify(carpet_entity,'yaw',-angle)
); 

deactivate() -> 
(
	if (!global_markers, return());
	current_time = tick_time();
	for (global_markers,
		entity_event( _, 'on_tick', '__marker_death_tick', current_time)
	);
	global_markers = l()
);

__marker_death_tick(carpet_entity, death_start) ->
(
	if (tick_time() > death_start+20,
		l(x, y, z) = pos(carpet_entity);
		y += carpet_entity~'eye_height';
		particle('smoke',x,y,z);
		modify(carpet_entity, 'remove')
	, //else
		modify(carpet_entity, 'move', rand(0.3)-rand(0.3), rand(0.4)-rand(0.2), rand(0.3)-rand(0.3))
	)
)
