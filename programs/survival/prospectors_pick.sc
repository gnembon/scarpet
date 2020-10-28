// scarpet 1.4

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);
 
__holds(entity, item_regex, enchantment) -> 
(
	if (entity~'gamemode_id'==3, return(0));
	for(l('mainhand','offhand'),
		holds = query(entity, 'holds', _);
		if( holds,
			l(what, count, nbt) = holds;
			if ((what ~ item_regex) && (enchants = get(nbt,'Enchantments[]')),
				// nbt query returns a scalar for lists of size one
				if (type(enchants)!='list', enchants = l(enchants));
				for (enchants, 
					if ( get(_,'id') == 'minecraft:'+enchantment,
						level = max(level, get(_,'lvl'))
					)
				)	
			)
		)
	);
	level
);

global_overworld_ores = l(
	l('coal_ore','dust 0.1 0.1 0.1 0.5'),
	l('iron_ore', 'dust 0.6 0.3 0.1 0.5'),
	l('redstone_ore', 'dust 0.9 0.1 0.1 0.5'),
	
	l('gold_ore','dust 0.9 0.9 0.0 0.5'),
	l('lapis_ore', 'dust 0.1 0.1 1.0 0.5'),
	
	l('diamond_ore','dust 0.3 0.8 1.0 0.5'), 
	l('emerald_ore', 'dust 0.4 1.0 0.4 0.5')
);

//l('nether_quartz_ore','dust 0.9 0.9 0.9 0.5'), 

__on_tick() ->
(
	for (player('!spectating'), player = _;
		if ( level = __holds(player, 'golden_pickaxe', 'fortune'),
			player_pos = pos(player);
			l(x, y, z) = map(player_pos, floor(_));
			player_in_caves = top('terrain',player_pos) > (y+3);
			// modify reference Y level, around diamond level for surface tracking
			base_y = if(player_in_caves, y, 8);
			loop(level*40,
				try (
					l(block_x, block_y, block_z) = l(x, base_y, z) 
							+ l(rand(16)-rand(16), rand(16)-rand(16), rand(16)-rand(16));
					block = block(block_x, block_y, block_z);
					if (block ~ '_ore',
						for(range(level-1, 1+2*level),
							l(oreblock, ore_particle) = get(global_overworld_ores, _);
							if (block == oreblock,
								if( player_in_caves,
									particle_line(ore_particle, 
										player_pos+l(0,1.2,0), 
										block_x+0.5, block_y+0.5, block_z+0.5, 
										0.8
									)
								,//else	
									particle(ore_particle, 
										block_x, top('terrain',block)+1 , block_z,
										20
									)
								);
								throw()
							)
						)
					)
				)
			)
		)
	)
);
__on_tick_nether() ->
(
	for (player('!spectating'), player = _;
		if ( lvl = __holds(player, 'golden_pickaxe', 'fortune'),
			player_pos = pos(player);
			l(x, y, z) = player_pos;
			loop(lvl*40,
				block = block(player_pos + l(rand(16)-rand(16),rand(16)-rand(16),rand(16)-rand(16)));
				if (block == 'nether_quartz_ore',
					l(block_x, block_y, block_z) = pos(block);
					particle_line('dust 0.9 0.9 0.9 0.5', 
						player_pos+l(0, 1.2, 0),
						block_x+0.5, block_y+0.5, block_z+0.5,
						0.8
					)
				)
			)
		)
	)
)
 
