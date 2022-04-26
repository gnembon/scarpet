// re-logs fake players upon server restart
// must need to place it directly in the world scripts folder

__config() -> {'scope' -> 'global'};

__on_server_starts() -> (
   data = load_app_data();
   if (data && data:'players',
      data = parse_nbt(data:'players');
      for (data,
         for([str('player %s spawn at %f %f %f facing %f %f in %s',
                  _:'name', _:'x', _:'y', _:'z', _:'yaw', _:'pitch', _:'dim'),
              str('gamemode %s %s', _:'gm', _:'name')],
            logger('warn', _);
            run(_);
         );
         modify(player(_:'name'), 'flying', _:'fly')
      )
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
