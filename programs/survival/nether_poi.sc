__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

__command() -> (
	p = player();
	//toggle render for that player
	global_active:p = !global_active:p ;
	print(p, if(global_active:p, 'Nether portal POIs around you will now render, hold an eye of ender.', 'Nether portal POIs won\'t render.') );
	return('');
);

global_debug = false;
global_range = 40;
global_refresh_rate = 5;
global_markers = m( l('the_nether', m()), l('overworld', m()) );

__debug_print(str) -> if(global_debug, print(str) );

__put_marker(pos, dim) ->(
	// Create marker at position and give it useful properties and a tag
	e = create_marker('', pos + l(0.5, 0.5, 0.5), 'purple_stained_glass');
	run(str('data merge entity %s {Glowing:1b, Fire:32767s, Marker:1b}', query(e, 'uuid')));
	modify(e, 'tag', 'nether_poi_marker');
	// Add the marker to the global marker list
	global_markers:dim:pos = e;
);

__update_markers(player_list, dim) -> (
	// I'll use the keys of the map as a set
	
	poi_map = m();
	// For each player that, check nether portal pois areound them and put it's coords into the set
	for(player_list, map(filter(poi(pos(_), global_range), (_~ 'nether_portal') == 0), poi_map:(_:2) = null) );
	pois = keys(poi_map);
	// Remove existing markers that were not found when refreshing list
	map( filter( keys(global_markers:dim), pois~_ ==null) , 
			modify(global_markers:dim:_, 'remove');
			delete(global_markers:dim, _)
	);
	// Add marker for new pois
	map( filter(pois, !has(global_markers:dim, _)) , __put_marker(_, dim) );
);

__remove_markers(dim) -> (
	// look for all markers that are nether portal pois
	marker_list = filter(entity_selector('@e[tag=nether_poi_marker]'), _~'dimension' == dim);
	//and remove them
	map(marker_list, modify(_, 'remove'));
	global_markers:dim = m();
);

__do_on_tick(dim) -> (
	if(!(tick_time()%global_refresh_rate) && player('*'), 
		// check for players with ender eyes
		__debug_print(str('Players in %s: %s', dim, player('*') ) );
		player_list = filter(player('*'), 
			(query(_, 'holds', 'mainhand'):0 == 'ender_eye' || query(_, 'holds', 'offhand'):0 == 'ender_eye')
			&& global_active:_			
		);
		__debug_print( str('%s: %s', tick_time(), player_list) );
		// if any players found
		if(player_list, 
			// then update markers around them
			__update_markers(player_list, dim),
			// else, delete all remaining markers, if there are any
			__debug_print('No players found in ' + dim);
			if(global_markers, __remove_markers(dim) )
		)
	)
);

__on_tick() -> (
	__do_on_tick('overworld');
);

__on_tick_nether() -> (
	__do_on_tick('the_nether')
);

// turn on for players that connect for the first time
__on_player_connects(player) -> (
	if(global_active:player == null, global_active:player = true);
);

global_active = {};
for(player('all'), global_active:_ = true);
