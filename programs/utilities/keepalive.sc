// re-logs fake players upon server restart
// must need to place it directly in the world scripts folder

global_cached_players = {};

__config() -> {
	'scope' -> 'global'
};

__on_player_connects(player) -> (
	player_name = player()~'name';

	if (has(global_cached_players, player_name),
		e = global_cached_players:player_name;
		if(e:'gm' == 'creative' && e:'fly' == 1,
			// scheduling the modification at the end to ensure the entity was created properly and setting it to the intended state
			schedule(0, _(e) -> (modify(player(e:'name'), 'flying', e:'fly')), e);
		);
	)
);

__on_player_disconnects(player, reason) -> (
	player_name = player()~'name';

	if (has(global_cached_players, player_name),
		global_cached_players:str(player_name) = null;
	);
);

__on_server_starts() -> (
	data = load_app_data();
	
	if (data && data:'players',
		data = parse_nbt(data:'players');
		for (data,
			global_cached_players:str(_:'name') = _;
			run(str('player %s spawn at %f %f %f facing %f %f in %s in %s', _:'name', _:'x', _:'y', _:'z', _:'yaw', _:'pitch', _:'dim', _:'gm'))
		);
	);
);

__on_server_shuts_down() -> (
	data = nbt('{players:[]}');
	saved = [];

	for (filter(player('all'), _~'player_type' == 'fake'),
		pdata = nbt('{}');
		pdata:'name' = _~'name';
		pdata:'dim' = _~'dimension';
		pdata:'x' = _~'x';
		pdata:'y' = _~'y';
		pdata:'z' = _~'z';
		pdata:'yaw' = _~'yaw';
		pdata:'pitch' = _~'pitch';
		pdata:'gm' = _~'gamemode';
		pdata:'fly' = _~'flying';
		put(data, 'players', pdata, -1);
		saved += _~'name';
	);

	store_app_data(data);
	if (saved, logger('warn', 'saved '+saved+' for next startup'));
);