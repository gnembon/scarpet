// tests various methods of interpolating player movement
// runs a player around 0,0,0 in a circle
// use
// /circle points for static teleports each tick_time
// /circle lerp to use static teleports with extra velocity adjustments for smoother panning
// /circle ride to mount player on server side entity and ride it instead
// add '_rotation' for also setting player rotation for the turn of events.

__config() -> m( l('scope', 'global') );

__on_tick() -> __circle();

__circle() -> __noop();

__noop() -> ( global_mount = null );

__command() -> __circle() -> __noop();
points() -> __circle() -> __points(false);
points_rotation() -> __circle() -> __points(true);
lerp() -> __circle() -> __lerp(false);
lerp_rotation() -> __circle() -> __lerp(true);
ride() -> __circle() -> __ride(false);
ride_rotation() -> __circle() -> __ride(true);


__points(rotate) -> (
	p = player();
	angle = tick_time() % 360;
	curpos = l( 10*sin(angle), p~'y', 10*cos(angle));
	modify(p, 'pos', curpos);
	if (rotate, 
		modify(p, 'yaw', 180-angle)
	)
);

__lerp(rotate) -> (
	p = player();
	angle = tick_time() % 360;
	curpos = l( 10*sin(angle), p~'y', 10*cos(angle));
	modify(p, 'pos', curpos);
	if (rotate, 
		modify(p, 'yaw', 180-angle-1)
	);
	nextpos = l( 10*sin(angle+1), p~'y', 10*cos(angle+1));
	velocity = 1.0111*(nextpos - curpos);
	modify(p, 'motion', velocity)
);
 

__ride(rotate) -> 
(
	if( global_mount == null,
		p = player();	
		global_mount = create_marker(null, p~'location', 'air');
		modify(p, 'mount', global_mount);
		entity_event(global_mount, 'on_tick', __must_ride(e) -> 
			if(!e~'is_ridden', modify(e, 'remove'); __command())
		)
	);
	angle = tick_time() % 360;
	curpos = l( 10*sin(angle), global_mount~'y', 10*cos(angle));
	modify(global_mount, 'pos', curpos);
	if (rotate, 
		modify(player(), 'yaw', 180-angle)
	)
)
