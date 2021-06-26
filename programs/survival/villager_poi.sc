// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

//to keep track of last seen entity id per player
global_last_entity_id = m();

global_poi_settings = m(
	l( 'home',
		m(
			l('matching_block', '_bed$'),
			l('default_block', 'red_bed'),
			l('ground_offset', -1.6)
		)
	),
	l( 'meeting_point',
		m(
			l('matching_block', '^bell$'),
			l('default_block', 'bell'),
			l('ground_offset', -1.7)
		)
	),
	l( 'job_site',
		m(
			l('matching_block', '.'),
			l('default_block', 'crafting_table'),
			l('ground_offset', -1.4)
		)
	),
);

__on_tick() ->
(
	if (tick_time()%20, return());
	// we are using here 'all' player meaning server-wide, cross dimension list
	// therefore we need to take care of the dimension of the execution later
	for (filter(player('all'), _~'holds':0 == 'map'), 
		player = _;
		in_dimension(player,
			// going through all the players and localizing execution
			// on player dimension
			entity = query(player, 'trace', 4.5, 'entities');
			player_name = str(player);
			if (type(entity)!='entity', 
				// player is not looking at an entity
				global_last_entity_id:player_name = null; 
				return()
			);
			// player is not looking at a villager, skipping
			if (entity~'type'!='villager', return());
			eid = entity~'id';
			// player is looking at a villager they haven't seen
			// last second
			if (global_last_entity_id:player_name != eid, 
				spawn_trackers_for_villager(entity)
			);
			global_last_entity_id:player_name = eid
		)
	)
);

spawn_trackers_for_villager(entity) ->
(
	enbt = entity ~ 'nbt';
	for(global_poi_settings, poi = _;
		if (bed_pos = get(enbt, 'Brain.memories.minecraft:'+poi+'.value.pos[]'),
			spawn_tracker(entity, bed_pos, get(enbt,'Brain.memories.minecraft:'+poi+'.value.dimension'), global_poi_settings:poi)
		, //else
			spawn_tracker(entity, pos(entity), null, global_poi_settings:poi)
		)
	)
);

// unused, doesn't mean unusable
__yaw_from_look(x,y,z) -> -atan2(x, z);
// ditto
__pitch_from_look(x,y,z) -> -asin(y);

__barrier(pos) -> particle('barrier', pos, 1, 0, 0);

spawn_tracker(entity, pos, dimension, settings) ->
(
	target_yaw = 0;
	target_pos = pos+l(0.5, settings:'ground_offset', 0.5);
	// default block to display in case the villager doesn't know they don't have it.
	target_block = settings:'default_block';
	
	if (
	// no entry, no spin
	dimension == null,
		// missing entry
		target_pos = target_pos + l(rand(2)-1,2+rand(1),rand(2)-1);
		target_yaw = entity~'yaw';
		schedule(40, '__barrier', target_pos+l(0,0.2-settings:'ground_offset',0)),
		
	// else if its a wrong dimension, give it a spin
	'minecraft:'+entity~'dimension' != dimension, 
		//then
		target_pos = pos(entity)+l(rand(3),rand(2)+8,rand(3));
		target_yaw = 2000,
	//else if villager block is valid, grab display block from there
	block(pos) ~ (settings:'matching_block') && !air(pos), 
		//then
		to_block = block(pos);
		target_block = str(to_block);
		if(facing = block_state(to_block, 'facing'),
			target_yaw += if( 
				facing == 'north', 180,
				facing == 'south', 0,
				facing == 'east', 270,
				facing == 'west', 90,
				0 
			)
		)
	);
	location = entity ~ 'location';
	location:4 = 0;
	tracker = create_marker(null, location, target_block);
	entity_event(tracker, 'on_tick', '__tracker_tick', pos(entity), target_pos, entity~'yaw', target_yaw)
);

__tracker_tick(entity, start_pos, end_pos, start_yaw, end_yaw) ->
(
	if (entity~ 'age' >= 100, 
		// after 5 seconds, perish
		modify(entity, 'remove');
		particle('end_rod', end_pos+l(0, 2, 0), 20)
	);
	// tanh interpolate for smooth ends
	stage = __find_stage(entity~'age', 100);
	final_pos = start_pos*(1-stage)+end_pos*stage;
	final_yaw = start_yaw*(1-stage)+end_yaw*stage;
	final_pos:null = final_yaw;
	final_pos:null = 0;
	modify(entity, 'location', final_pos)
);

__find_stage(age, maxage) ->
(
	//center
	spread = maxage/2;
	(tanh(4*(age-spread)/spread)+1)/2
)



