
// safe and fair spectator camera app by gnembon
// saves player's position, motion, angles, effects and restores them on landing
// saves player configs between saves
// places spectating players without config in a safe places
// and adds timeout for the player so they cannot switch instantly
// change setting below to change that behaviour (0 to disable timeout)
global_survival_timeout = 3;

// optionally, you can restrict a player from flying too far from their original 
// position (null or any non positive number to disable). The shape in which "too
// far" is is calculated can be set below. Use 1 for "cylindrical", 2 for "spherical"
// and 3 for "square" restriction shapes.
global_max_flight_range = null;
global_restriction_shape = 1; // 1, 2 or 3 for "cylindrical", "shperical" and "square", respectively

// additional player checks are here. comment lines that you think are not needed
__assert_player_can_cam_out(player) ->
(
   if(!query(player, 'nbt', 'OnGround'), exit('You must be on firm ground.'));
   if(query(player, 'nbt', 'Air') < 300, exit('You must be in air, not suffocating nor in liquids.'));
   if(player ~ 'is_burning', exit('You must not be on fire.'));
   null
);

// none of your business below



import('math', '_euclidean', '_vec_length');

__config() -> {
    'stay_loaded' -> 'true'
};

__command() ->
(
   p = player();
   current_gamemode = p~'gamemode';
   if ( current_gamemode == 'spectator',
      if (config = __get_player_stored_takeoff_params(p~'name'),
         __remove_camera_effects(p);
         __restore_player_params(p, config);
         __remove_player_config(p~'name');
         display_title(p, 'actionbar', format('y Exited camera mode'));
      ,
         if (__survival_defaults(p), 
            __remove_camera_effects(p);
            display_title(p, 'actionbar', format('y Exited camera mode'));
         );
      );
   , current_gamemode == 'survival' && !global_is_in_switching, // else if survival - switch to spectator
      __assert_player_can_cam_out(p);
      
      global_is_in_switching = true;
      for(range(1, global_survival_timeout+1), 
         schedule((global_survival_timeout - _) * 20, _(outer(_),outer(p)) -> display_title(p, 'actionbar', format('y Entering camera mode in '+_+'...')))
      );
      player_name = p~'name';
      player_dim = p~'dimension';
      schedule((global_survival_timeout)*20, _(outer(player_name), outer(player_dim)) -> (
         global_is_in_switching = false;
         p = player(player_name);
         if (p && p~'dimension' == player_dim,
            __store_player_takeoff_params(p);
            __turn_to_camera_mode(p);
            display_title(p, 'actionbar', format('y Entered camera mode'));
         )
      ));
   );
   null
);


__get_player_stored_takeoff_params(player_name) ->
(
   tag = load_app_data();
   if(!tag, return (null));
   player_tag = tag:player_name;
   if (!player_tag, return(null));
   config = m();
   config:'pos' = player_tag:'Position.[]';
   config:'motion' = player_tag:'Motion.[]';
   config:'yaw' = player_tag:'Yaw';
   config:'pitch' = player_tag:'Pitch';
   config:'dimension' = player_tag:'Dimension';
   config:'effects' = l();
   effects_tags = player_tag:'Effects.[]';
   if (effects_tags,
      for(effects_tags, etag = _;
         effect = m();
         effect:'name' = etag:'Name';
         effect:'amplifier' = etag:'Amplifier';
         effect:'duration' = etag:'Duration';
         config:'effects' += effect;
      );
   );
   config
);

__store_player_takeoff_params(player) ->
(
   tag = nbt('{}');
   // need to print to float string
   //otherwise mojang will interpret 0.0d as 0i and fail to insert
   for(pos(player), put(tag:'Position',str('%.6fd',_),_i)); 
   for(player~'motion', put(tag:'Motion',str('%.6fd',_),_i)); 
   tag:'Yaw' = str('%.6f', player~'yaw');
   tag:'Pitch' = str('%.6f', player~'pitch');
   tag:'Dimension' = player~'dimension';
   for (player~'effect',
      l(name, amplifier, duration) = _;
      etag = nbt('{}');
      etag:'Name' = name;
      etag:'Amplifier' = amplifier;
      etag:'Duration' = duration;
      put(tag:'Effects', etag, _i);
   );
   apptag = load_app_data();
   if (!apptag, apptag = nbt('{}'));
   apptag:(player~'name') = tag;
   store_app_data(apptag);
   null
);

__restore_player_params(player, config) ->
(
   run('execute in minecraft:'+config:'dimension'+' run tp @s ~ ~ ~');
   modify(player, 'gamemode', 'survival');
   for(l('pos', 'motion', 'yaw', 'pitch'), modify(player, _, config:_));
   for (config:'effects',
      modify(player, 'effect', _:'name', _:'duration', _:'amplifier')
   );
);

__remove_player_config(player_name) ->
(
   tag = load_app_data();
   delete(tag:player_name);
   store_app_data(tag);
);

__remove_camera_effects(player) ->
(
   modify(player, 'effect', 'night_vision', null);
   modify(player, 'effect', 'conduit_power', null);
   modify(player, 'gamemode', 'survival');
);

__turn_to_camera_mode(player) ->
(
   modify(player, 'effect');
   modify(player, 'effect', 'night_vision', 999999, 0, false, false);
   modify(player, 'effect', 'conduit_power', 999999, 0, false, false);
   modify(player, 'gamemode', 'spectator');
   __restrict_flight(player);
);

__survival_defaults(player) ->
(
   yposes = l();
   l(x,y,z) = pos(player);
   for(range(32), yposes+=y+_; yposes+=y-_);
   for( yposes,
      scan(x, _, z, 32, 0, 32,
         if( air(_) && air(pos_offset(_, 'up')) && suffocates(pos_offset(_, 'down')),
            modify(player, 'pos', pos(_)+l(0.5,0.2,0.5));
            return(true);
         )
      )
   );
   print(format('rb Cannot find a safe spot to land within 32 blocks.'));
   false;
);

__restrict_flight(player) -> 
(
   if(global_max_flight_range > 0, 
      config = __get_player_stored_takeoff_params(player~'name');
      if(!config,  // gotta restrict it somehow if no config was saved
         start_pos = player~'pos';
         start_dim = player~'dimension',

         start_pos = config:'pos'; 
         start_dim = config:'dimension'
      );
      
      __restrict_flight_tick(player, start_pos, start_dim)
   )
);

__restrict_flight_tick(player, start_pos, start_dim) -> 
(
   if(player~'gamemode' != 'spectator', exit());

   pp = player~'pos';
   distance = __get_distance(copy(pp), copy(start_pos));
   
   if(distance>global_max_flight_range,
      if(distance>global_max_flight_range*1.5,
         new_pos = __get_restriction_new_pos(pp, start_pos);
         modify( player, 'pos', new_pos), //in case they TP to another player

         new_motion = __get_restriction_new_motion(pp, start_pos, player~'motion');
         modify( player, 'motion', new_motion),
      )
      
   );

   if(player~'dimension' != start_dim,
      run('execute in minecraft:'+ start_dim +' run tp @s ~ ~ ~');
      modify( player, 'pos', start_pos);
   );

   schedule(1, '__restrict_flight_tick', player, start_pos, start_dim);
);

__get_distance(pos1, pos2)->
(
   // messure flat distance for square and cylindrical
   if (global_restriction_shape!=2,
      pos1:1 = 0;
      pos2:1 = 0;
   );
   if( global_restriction_shape<3,
         _euclidean(pos1, pos2),
      // else, square
         max(map(pos1-pos2, abs(_)) ),
   )
);

__get_restriction_new_pos(current, start) ->
(
   if (global_restriction_shape<3,
         direction = current-start;
         if (global_restriction_shape==1, direction:1 = 0); //have to do this separately for the normalization to work
         direction = direction/_vec_length(direction);
         new_pos = direction * global_max_flight_range + start;
         if (global_restriction_shape==1, new_pos:1 = current:1);
         new_pos,
      //else, square
         zeroed = current-start;
         zeroed = map(zeroed, __sign(_) * min(abs(_), global_max_flight_range));
         new_pos = start + zeroed;
         new_pos:1 = current:1;
         new_pos
   )
);


__get_restriction_new_motion(current, start, motion) ->
(
   direction = current-start;
   if (global_restriction_shape<3,
         if (global_restriction_shape==1, direction:1 = 0);
         -1 * direction / global_max_flight_range * (_vec_length(motion)+0.5),
      //else, square
         new_motion = copy(motion);
         if(abs(direction:0) > global_max_flight_range, new_motion:0 = -motion:0 - __sign(direction:0) * 0.5);
         if(abs(direction:2) > global_max_flight_range, new_motion:2 = -motion:2 - __sign(direction:2) * 0.5);
         new_motion
   )
);

__sign(num) -> if (num < 0, -1, 1);

__on_player_connects(player) -> 
(
  if( __get_player_stored_takeoff_params(player~'name'), 
     modify(player, 'gamemode' , 'spectator' );
     __restrict_flight(player)
    );
);
