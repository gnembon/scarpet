// re-logs fake players upon server restart
// must need to place it directly in the world scripts folder

__config() -> {
	requires -> {
		'minecraft' -> '>=1.8', // spectator was not a thing prior to 1.8
	},
	'scope' -> 'global'
};

__on_connect(e) -> (
	// forcing the fake-players that were spawned in spectator back into creative to retain flight per their data
	if(e:'gm' == 'creative' && e:'fly' == 1,
		sleep(50); // the direct delay is needed to force it back into the proper gamemode. it cannot for the life of it do so at the exact same moment and requires the delay.
		for([str('gamemode %s %s', e:'gm', e:'name')],
			logger('warn', _);
			run(_);
		);
	);
);

__on_player_connects(player) -> (
	data = load_app_data();
	if (data && data:'players',
		data = parse_nbt(data:'players');

		player_name = player()~'name';

		for (data,
			// we're detecting if the player connecting is a fake-player, thus guaranteeing that the player entity object exists and can be used
			if (_:'name' == player_name,
				task('__on_connect', _)
			);
		);
	);
);

	
__spawn_players() -> (
   	data = load_app_data();
   	if (data && data:'players',
    	data = parse_nbt(data:'players');
	   	for (data,
			if (_:'gm' == 'creative' && _:'fly' == 1,
				// spawning creative fake-players in spectator to force the flying state as using modify throws an error due to potential race conditions
				for([str('player %s spawn at %f %f %f facing %f %f in %s in spectator', _:'name', _:'x', _:'y', _:'z', _:'yaw', _:'pitch', _:'dim')],
					logger('warn', _);
					run(_);
				);
			, _:'gm' != 'creative',
				for([str('player %s spawn at %f %f %f facing %f %f in %s in %s',_:'name', _:'x', _:'y', _:'z', _:'yaw', _:'pitch', _:'dim', _:'gm')],
					logger('warn', _);
					run(_);
				);
			);
	   );
   );
);

__on_server_starts() -> (
  task('__spawn_players');
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
