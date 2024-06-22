
// safe and fair spectator camera app by gnembon
// saves player's position, motion, angles, effects and restores them on landing
// saves player configs between saves
// places spectating players without config in a safe places
// and adds timeout for the player so they cannot switch instantly
// change setting below to change that behaviour (0 to disable timeout)
global_survival_timeout = 3;

// additional player checks are here. comment lines that you think are not needed
__assert_player_can_cam_out(player) ->
(
   if(!query(player, 'nbt', 'OnGround'), exit('You must be on firm ground.'));
   if(query(player, 'nbt', 'Air') < 300, exit('You must be in air, not suffocating nor in liquids.'));
   if(player ~ 'is_burning', exit('You must not be on fire.'));
   null
);

// none of your business below




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
   , current_gamemode == 'creative',
      display_title(p, 'actionbar', format('r You must be in survival mode to use camera mode'));
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
   [x, y, z] = config:'pos';
   run('execute in minecraft:'+config:'dimension'+' run tp @s '+x+' '+y+' '+z);
   modify(player, 'gamemode', 'survival');
   for(l('motion', 'yaw', 'pitch'), modify(player, _, config:_));
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

// This fixes the case where the server has force-gamemode on, in which case the player would be put
// into survival in the location they logged out in camera mode, bypassing the sign-off actions of the app.
// __get_player_stored_takeoff_params evaluates true-y when player was in cam mode before logging out
__on_player_connects(player) -> if( __get_player_stored_takeoff_params(player~'name'), modify(player, 'gamemode' , 'spectator' ));
