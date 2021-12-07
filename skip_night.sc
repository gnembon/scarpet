//////
// night-skip by Firigion
// set bed_mode=false; to toggle night skipping via command
// set bed_mode=true; to have the night be skipped when a player places a bed with 'skip' in its name
// nights will be skipped as long as the bed exists in the overworld and no other skip bed was placed.
// This is the default mode.
//////

bed_mode = true;

global_sleep_time = 12550; // time of day to start skipping
global_refresh_rate = 100; // how often to check, in ticks

__config() -> {'scope' -> 'global'};

if( bed_mode,
  
  ( // bed mode
  __on_player_places_block(player, item_tuple, hand, block) -> (
    if( player~'dimension' == 'overworld' &&
      item_tuple:0 ~'_bed' &&  //if it's a bed
      (name_nbt = item_tuple:2:'display.Name') != null && // and has a custom name
      lower(parse_nbt(name_nbt):'text') ~ 'skip', // bed has 'skip' in the name
      
      _set_bed_block(block, player);
      
    );
  );

  __on_player_breaks_block(player, block) -> (
    if(global_bed_block && 
      player~'dimension' == 'overworld' &&
      (pos(block)==pos(global_bed_block) || 
      pos(block)==pos_offset(pos(global_bed_block), block_state(global_bed_block, 'facing'), 1)
      ),
      _reset_bed_block()
    );
  );
  ),
  //command mode

  __command() -> (
    print(format(
      'w Night skipping is ',
      str('b %s', if(global_skip_state, 'on', 'off') ),
      'w . Use ',
      'yb /skip-night toggle ',
      '^g Click here!',
      '!/skip-night toggle',
      'w to change it.'
    ));
    return('');
  );
);

_skip() -> (
  //dtime = day_time()%24000; //day time
  if((dtime = day_time()%24000)> global_sleep_time, // if it's sleepy time
    current = day_time();
    day_time(current + (24000 - dtime) );
    print('Night skipped')
  );
);

global_skip_state = false;
toggle() -> (
  global_skip_state = !global_skip_state;
  if(global_skip_state,
    print(format('e Nights will now be skipped once daytime reaches '+global_sleep_time)),
    print(format('n, Nights will no longer be skipped'))
  );
  return('')
);

_set_bed_block(block, player) -> (
  print(format(str('e Nights will be now skipped with bed at %s thanks to %s', pos(block), player)));
  if(global_bed_block, print(format('g Replaced old bed at '+pos(global_bed_block))));
  
  global_skip_state = true;
  global_bed_block = block;
  global_bed_block_particles_pos = pos_offset(pos(block), block_state(block, 'facing'), 1) + [0.5, 0, 0.5];
);

_reset_bed_block() -> (
  global_bed_block = null;
  global_skip_state = false;
  print(format('n Nights will no longer be skipped'));
);


__on_tick() -> (
  if(global_skip_state && !(system_info('world_time')%global_refresh_rate),
    _skip()
  );
  if( global_bed_block,
    particle('portal', global_bed_block_particles_pos, 3, 0.5, 0);
    
    if( !(system_info('world_time')%global_refresh_rate) && global_bed_block != block(pos(global_bed_block)),
      _reset_bed_block();
    );
  );
    
);
