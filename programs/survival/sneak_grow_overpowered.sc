// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_tick() -> _sneak_grow();
__on_tick_nether() -> _sneak_grow();
__on_tick_ender() -> _sneak_grow();

_sneak_grow() -> 
(
	for(filter(player('*'), _ ~ 'sneaking'),
		player_pos = pos(_);
		loop ( 500,
			target = player_pos
					+l(rand(12),rand(8),rand(12))
					-l(rand(12),rand(8),rand(12));
			if (material(target) == 'plant' && ticks_randomly(target), 
				particle('happy_villager', target, 2, 0.4)
			);
            random_tick(target)
		)
    )
)
