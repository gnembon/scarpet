// |----------------------------------------------------------------|
// |								    |
// |             SCRIPT CONFIGURATION, ONLY MODIFY THIS             |
// |								    |
// |----------------------------------------------------------------|


// Set to true for bot respawn on reconnect, false for no respawn
global_respawn_on_reconnect = true;

// Delay to check for reconnection to kill bots, in ticks (recommended min. 20 ticks for proper disconnection handling)
global_delay = 400;

// Players bypassing restrictions (should look like ['player_1', 'player_2', 'player_3'] USE SINGLE QUOTATION MARKS)
global_bypass = [];

// Player bypass rules
global_bypass_disconnect_kill = true;
global_bypass_limited_spawns = true;
global_bypass_act_on_other_player_bots = true;
global_bypass_disabled_commands = true;

// Allow players to kill other player's bots (not with commands, with force)
global_allow_griefing = false;

// If there should be a limit to how many bots a player can spawn
global_limited_bot_spawns = true;

// Amount of bots that a single player can spawn
global_max_bots_per_player = 3;

// Set this to the name of the .sc file (do NOT include the .sc)
global_file_name = 'fakeplayer';

// Which sub /player commands are allowed
global_allowed_commands = [
			   'attack', 
			   'dismount', 
			   'drop', 
			   'dropStack', 
			   'hotbar', 
			   'jump', 
			   'kill', 
			   'look', 
			   'mount', 
			   'move', 
			   'shadow', 
			   'sneak', 
			   'spawn', 
			   'sprint', 
			   'stop', 
			   'swapHands', 
			   'turn', 
			   'unsneak', 
			   'unsprint', 
			   'use' 
			  ];


// |----------------------------------------------------------------|
// |								    |
// |             SCRIPT CONFIGURATION, ONLY MODIFY THIS             |
// |								    |
// |----------------------------------------------------------------|



global_command_list = [
		       'attack', 
		       'dismount', 
		       'drop', 
		       'dropStack', 
		       'hotbar', 
		       'jump', 
		       'kill', 
		       'look', 
		       'mount', 
		       'move', 
		       'shadow', 
		       'sneak', 
		       'spawn', 
		       'sprint', 
		       'stop', 
		       'swapHands', 
		       'turn', 
		       'unsneak', 
		       'unsprint', 
		       'use' 
		      ];



// Declare global variable types
global_player_bots = {};
global_disconnect_tick = {};
global_reconnect_tick = {};
global_bot_data = [];
global_spawnable_bots = {};



global_basic_subactions = ['continuous', 'interval', 'once'];
global_spawn_subactions = ['facing', 'at', 'in', 'gamemode'];
global_drop_subactions = ['continuous', 'interval', 'once', 'all', 'mainhand', 'offhand'];
global_hotbar_subactions = [1, 2, 3, 4, 5, 6, 7, 8, 9];
global_move_subactions = ['backward', 'forward', 'left', 'right'];
global_turn_subactions = ['back', 'left', 'right'];
global_mount_subactions = ['anything'];
global_look_subactions = ['down', 'up', 'north', 'west', 'east', 'south', 'at'];
global_subactionless_actions = ['dismount', 'shadow', 'sneak', 'sprint', 'stop', 'unsneak', 'unsprint'];
global_subactionful_actions = ['attack', 'jump', 'swaphands', 'use', 'drop', 'dropstack', 'hotbar', 'move', 'turn', 'mount'];



// Set app scope to global & define custom commands
__config() -> {
    'scope' -> 'global',
    'commands' -> {
        '' -> _() -> print(),
// -------------------------------------------------------------------------------------------------------------------------------------------
	'<name> <action> <subaction>' -> 					'_on_bot_command',
// -------------------------------------------------------------------------------------------------------------------------------------------
	'<name> <action> interval <ticks>' -> 	_(name, action, tk) -> 		_on_interval_command(name + ' ' + action + ' interval ' + tk),
// -------------------------------------------------------------------------------------------------------------------------------------------
	'<name> look at <pos>' -> 						'_on_look_command',
	'<name> look <look_subs>' -> 						'_on_basic_look_command',
// -------------------------------------------------------------------------------------------------------------------------------------------
	'<name> unsprint' -> 			_(name) -> 			_on_subactionless_command(name + ' unsprint'),
	'<name> unsneak' -> 			_(name) -> 			_on_subactionless_command(name + ' unsneak'),
	'<name> stop' -> 			_(name) -> 			_on_subactionless_command(name + ' stop'),
	'<name> sprint' -> 			_(name) -> 			_on_subactionless_command(name + ' sprint'),
	'<name> sneak' -> 			_(name) -> 			_on_subactionless_command(name + ' sneak'),
	'<name> shadow' -> 			_(name) -> 			_on_subactionless_command(name + ' shadow'),
	'<name> dismount' -> 			_(name) -> 			_on_subactionless_command(name + ' dismount'),
// -------------------------------------------------------------------------------------------------------------------------------------------
	'<name> spawn at <pos>' -> 						'_on_spawn_command',
	'<name> kill' -> 							'_on_kill_command'
// -------------------------------------------------------------------------------------------------------------------------------------------
    },

    'arguments' -> {
        'name' -> { 'type' -> 'term', 'suggester' -> '_get_bot_suggestions' },
        'action' -> { 'type' -> 'term', 'suggester' -> '_get_action_suggestions' },
        'subactionless_action' -> { 'type' -> 'term', 'suggest' -> global_subactionless_actions },
        'subaction' -> { 'type' -> 'term', 'suggester' -> '_get_subaction_suggestions' },
        'ticks' -> { 'type' -> 'int', 'min' -> 1, 'max' -> 12000, 'suggest' -> [1, 2, 5, 10, 15, 20] },
        'pos' -> { 'type' -> 'pos' },
	'look_subs' -> { 'type' -> 'term', 'suggest' -> global_look_subactions },
    }
};




// Return player's valid bot list to the <name> argument when custom spawn/kill commands are called
_get_bot_suggestions(arg) -> (

    player_name = lower(str(player()));
    logger('--[PLAYER TYPING COMMAND: ' + upper(player_name) + ']--');

    return(global_spawnable_bots:player_name);
);



// Get the subaction suggestions for player commands having them
_get_subaction_suggestions(arg) -> (

    action = arg:'action';
    bot = arg:'name';
    player = lower(str(player()));

    // Return subactions based on the action
    if (action == 'attack' || action == 'jump' || action == 'swapHands' || action == 'use',

	return(global_basic_subactions);
    ,
	if (action == 'drop' || action == 'dropStack',

	    return(global_drop_subactions);
	,
	    if (action == 'hotbar',

		return(global_hotbar_subactions);
	    ,
		if (action == 'move',

		    return(global_move_subactions);
		,
		    if (action == 'turn',

			return(global_turn_subactions);
		    ,
			if (action == 'mount',

			    return(global_mount_subactions);
			);
		    );
		);
	    );
	);
    );
);



_get_action_suggestions(arg) -> (

    player_name = lower(str(player()));

    if (global_bypass_disabled_commands == false,

	return(global_allowed_commands);
    ,
	if (global_bypass_disabled_commands == true && global_bypass~player_name == null,

	    return(global_allowed_commands);
	,
	    if (global_bypass_disabled_commands == true && global_bypass~player_name != null,

		return(global_command_list);
	    );
	);
    );
);



_command_execution_checks(player_command, player_name, bot_name, action) -> (

    if (global_player_bots:player_name~bot_name != null && global_allowed_commands~action != null,

	logger(str(player_command));
	run(str(player_command));
    ,
	if (global_player_bots:player_name~bot_name == null && global_bypass~player_name != null && global_allowed_commands~action != null,

	    logger(str('--[%s BYPASSED PREVENTING ACTING ON %s]--', upper(player_name), upper(bot_name)));
	    logger(str(player_command));
	    run(str(player_command));
	,
	    if (global_player_bots:player_name~bot_name != null && global_bypass~player_name != null && global_allowed_commands~action == null && global_bypass_disabled_commands == true,

		logger(str('--[%s BYPASSED DISABLED COMMAND ACTION %s]--', upper(player_name), upper(action)));
		logger(str(player_command));
		run(str(player_command));
	    ,
		if (global_player_bots:player_name~bot_name == null && global_bypass~player_name != null && global_allowed_commands~action == null && global_bypass_disabled_commands == true,

		    logger(str('--[%s BYPASSED PREVENTING ACTING ON %s AND BYPASSED DISABLED COMMAND ACTION %s]--', upper(player_name), upper(bot_name), upper(action)));
		    logger(str(player_command));
		    run(str(player_command));
		,
		    if (global_player_bots:player_name~bot_name == null && global_bypass~player_name == null && global_allowed_commands~action == null,

			logger(str('--[%s TRIED ACTING ON %s AND EXECUTED %s DISABLED COMMAND AND FAILED]--', upper(player_name), upper(bot_name), upper(action)));
		    ,
			if (global_player_bots:player_name~bot_name != null && global_bypass~player_name == null && global_allowed_commands~action == null,

			    logger(str('--[%s TRIED EXECUTING %s DISABLED COMMAND AND FAILED]--', upper(player_name), upper(action)));
			,
			    if (global_player_bots:player_name~bot_name == null && global_bypass~player_name == null && global_allowed_commands~action != null,

				logger(str('--[%s TRIED ACTING ON BOT %s AND FAILED]--', upper(player_name), upper(bot_name)));
			    );
			);
		    );
		);
	    );
	);
    );
);



// Logic for actions having the interval subaction
_on_interval_command(arg) -> (

    logger(arg);
    command = str(arg);
    split = split(' ', command);
    bot_name = lower(str(split:0));
    player_name = lower(str(player()));
    action = split:1;
    player_command = str('player %s', command);

    _command_execution_checks(player_command, player_name, bot_name, action);
);



_on_basic_look_command(name, subaction) -> (

    player_name = lower(str(player()));
    bot_name = lower(str(name));
    action = 'look';
    player_command = str('player %s look %s', bot_name, subaction);

    _command_execution_checks(player_command, player_name, bot_name, action);
);



_on_look_command(name, pos) -> (

    player_name = lower(str(player()));
    bot_name = lower(str(name));
    action = 'look';
    player_command = str('player %s look at %f %f %f', bot_name, pos:0, pos:1, pos:2);

    _command_execution_checks(player_command, player_name, bot_name, action);
);



_on_subactionless_command(arg) -> (

    args = split(' ', arg);
    player_name = lower(str(player()));
    bot_name = lower(str(args:0));
    action = str(args:1);
    player_command = str('player %s', arg);
    logger(str('-----[PLAYER COMMAND: %s]-----', player_command));

    _command_execution_checks(player_command, player_name, bot_name, action);
);



_on_bot_command(name, action, subaction) -> (

    player_name = lower(str(player()));
    bot_name = lower(str(name));
    player_command = str('player %s %s %s', bot_name, action, subaction);

    _command_execution_checks(player_command, player_name, bot_name, action);
);



_on_spawn_command(name, pos) -> (

    player_name = lower(str(player()));
    bot_name = lower(str(name));
    action = 'spawn';

    if (global_limited_bot_spawns == true && global_allowed_commands~action != null,

	if (global_spawnable_bots:player_name~bot_name != null,

	    run(str('player %s spawn at %f %f %f', bot_name, pos:0, pos:1, pos:2));
	);
    ,
	if (global_limited_bot_spawns == false && global_allowed_commands~action != null,

	    run(str('player %s spawn at %f %f %f', bot_name, pos:0, pos:1, pos:2));
	,
	    if (global_allowed_commands~action == null,

		logger('--[%s TRIED EXECUTING %s DISABLED COMMAND AND FAILED]--', upper(player_name), upper(action));
	    );
	);
    );
);



_on_kill_command(name) -> (

    if (global_allow_griefing == true,

	run(str('player %s kill', name));
    ,
	if (global_player_bots:player_name~global_bot_name != null,

	    run(str('player %s kill', name));
	);
    );
);



// |----------------------------------------------|
// |                                              |
// | SEPARATE CONFIG AND COMMANDS FROM OTHER CODE |
// |                                              |
// |----------------------------------------------|



// Logger function for schedule
_logger(log) -> (
    logger(str(log))
);



// Make global_bypass usernames lowercase
global_bypass = map(global_bypass, lower(_));



// Check for player bots after disconnection, and kill them all, after the delay is over
_check_and_kill_bots(player) -> (

    current_tick = tick_time();

    // Check if the player has indeed been disconnected for more than the delay
    if ((current_tick - global_reconnect_tick:lower(str(player))) >= global_delay,

	logger('--[PLAYER DISCONNECTED FOR LONGER THAN DELAY, STARTING BOT KILLING]--');

        disconnected_player = lower(str(player));

	// Go through the player's bots
        for (global_player_bots:disconnected_player,

            current_bot = entity_selector(_):0;

	    // Kill the bot
            run(str('player %s kill', current_bot));
	    
	    logger('--[KILLED ' + upper(str(current_bot)) + ']--');

	    // If respawn on reconnect is true, prepare the bots' data to be stored in a json file
            if (global_respawn_on_reconnect == true,

		logger('--[STORING BOT DATA]--');

                global_bot_data += {
                    'bot_name' -> str(current_bot),
                    'dimension' -> query(current_bot, 'dimension'),
                    'coords' -> pos(player(str(current_bot))),
                };
            );

	    // Unassign the current bot from the disconnected player's key
            global_player_bots:disconnected_player = filter(global_player_bots:disconnected_player, _ != current_bot);

	    logger('--[UNASSIGNED ' + upper(str(current_bot)) + ']--');
        );

	// If respawn on reconnect is true, create a json file for the player including all killed bots for future respawning
        if (global_respawn_on_reconnect == true,

            write_file(disconnected_player, 'json', global_bot_data);

	    logger('--[WROTE BOT DATA TO FILE: ' + disconnected_player + '.json' + ']--');
        );

	// Delete the player's key from the map
        delete(global_player_bots, disconnected_player);

	logger('--[DELETED PLAYER KEY FROM PLAYER-BOTS MAP]--');
    );
);



// Create valid bot spawn list & respawn player's bots on reconnection if they had any, and update the player-bots map accordingly
__on_player_connects(player) -> (

    // Check if user enabled limited bot spawns
    if (global_limited_bot_spawns == true && global_bypass~lower(str(player)) == null,

        counter = 0;
        global_username = lower(str(player));
	case_respect_username = str(player);
        warning = 16 - 1 - length(global_max_bots_per_player);

	// Create a list assigned to the player's key if it doesn't have one
        if (!has(global_spawnable_bots, global_username),
            global_spawnable_bots:global_username = [];
        );

	// Check if the player's user is too long to avoid false spawns due to missing space for underscore and bot number
        if (length(global_username) >= warning,

            username = slice(global_username, 0, -(16 - warning));
        );

	// Add underscore after the username
        username = global_username + '_';

	// Create the amount of valid bots specified each ending with their number
        while (counter < global_max_bots_per_player,

            global_spawnable_bots:global_username += (username + str(counter + 1));
            counter += 1;
        );

	global_spawnable_bots:global_username += case_respect_username;
    ,
	if (global_limited_bot_spawns == true && global_bypass~lower(str(player)) != null && global_bypass_limited_spawns == true,

	    logger(str('--[%s IS A BYPASS USER, SKIPPING BOT LIMITING]--'));
	);
    );

    logger(str(global_spawnable_bots));

    global_reconnected_player_name = lower(str(player));
    global_reconnect_tick:global_reconnected_player_name = tick_time();

    // Check if respawn on reconnect is true, if the player connecting is real and if the time since disconnection is over the delay
    if (global_respawn_on_reconnect == true && query(player, 'player_type') != 'fake'
	&& global_reconnect_tick:global_reconnected_player_name - global_disconnect_tick:global_reconnected_player_name >= global_delay,

	logger('--[STARTING BOT RESPAWN PROCESS]--');	

        global_connected_player = lower(str(player));
        global_player_bots:global_connected_player = [];

	// Store the bot data from the json file into a variable and delete the player's json file
        data = read_file(global_connected_player, 'json');
        delete_file(global_connected_player, 'json');

	// Go through every bot previously stored in the json file and respawn it at the correct disconnection coordinates and dimension, in the player's gamemode
        for (data,

            command = str('execute as %s in minecraft:%s run player %s spawn at %f %f %f',
                           global_connected_player, data:_i:'dimension', data:_i:'bot_name', data:_i:'coords':0, data:_i:'coords':1, data:_i:'coords':2);
            run(command);

	    logger('--[RAN COMMAND: /' + command + ']--');

	    // Assign the bot name to the player key in the player-bots map
            global_player_bots:global_connected_player += str(lower(data:_i:'bot_name'));

	    logger('--[ASSIGNED BOT TO PLAYER: ' + upper(global_connected_player) + ']--');
        );
    );
);


// Record bot spawn and kill to update the player-bots map
__on_player_command(player, command) -> (

    // Split the command into its arguments
    args = split(' ', command);

    // Store the bot name from the command and the player name, as their lowercase to avoid future mismatches
    global_bot_name = lower(str(get(args, 1)));
    player_name = lower(str(player));

    // Check if the command is /player <name> spawn
    if (args:0 == global_file_name && args:2 == 'spawn',

	logger('--[SPAWN DETECTED]--');

	// Check if the player has a key in the player-bots map, create it if not
        if (!has(global_player_bots, player_name),

            global_player_bots:player_name = [];
        );
	
	// Check if the bot isn't already spawned, and assign it to the key if not
	if (global_player_bots:player_name~global_bot_name == null,

            global_player_bots:player_name += global_bot_name;

	    logger('--[ASSIGNED BOT TO PLAYER: ' + str(global_player_bots:player_name) + ']--');
	    logger('--[UPDATED PLAYER-BOTS MAP: ' + str(global_player_bots) + ']--');
	);
    ,

        // Check if the command is /player <name> kill
        if (args:0 == global_file_name && args:2 == 'kill',

	    logger('--[BOT KILL DETECTED]--');

	    // Check if the player has spawned a bot with that name, if so unassign it from their key
            if (global_player_bots:player_name~global_bot_name != null,

                global_player_bots:player_name = filter(global_player_bots:player_name, _ != global_bot_name);

		logger('--[UNASSIGNED BOT FROM PLAYER: ' + str(global_player_bots:player_name) + ']--');
		logger('--[UPDATED PLAYER-BOTS MAP: ' + str(global_player_bots) + ']--');
            );
        );
    );
);


// Kill bots on manual kill, and schedule the bot kill function on real player disconnection
__on_player_disconnects(player, reason) -> (

    global_bot_name = lower(str(player));

    // Check if the bot disconnected due to a player attacking it
    if (str(reason)~'death.attack.generic',

	// Go through every player's spawned bots
        for (global_player_bots,

            real_player = _;
	    logger('--[ITERATING THROUGH ' + upper(str(real_player)) + '\'S BOTS]--');


	    // If there's a match, unassign the bot from the owner's key in the player-bots map
            if (global_player_bots:real_player~global_bot_name != null,

                real_player = _;
                global_player_bots:real_player = filter(global_player_bots:real_player, _ != global_bot_name);
		logger('--[UNASSIGNED BOT FROM PLAYER: ' + str(global_player_bots:lower(str(real_player))) + ']--');
		logger('--[UPDATED PLAYER-BOTS MAP: ' + str(global_player_bots) + ']--');
            );
        );
    );


    // Check if the player disconnecting is a real player
    if (query(player, 'player_type') != 'fake',

        schedule(2, '_logger', ('--[INFO]-- ' + player + ' disconnected. Scheduling check in ' + global_delay + ' ticks...'));
	
	if (global_bypass~lower(str(player)) == null,

	    // Schedule the bot killing process for after the delay
            schedule(global_delay, '_check_and_kill_bots', player);

	    // Record the disconnect time and reset the reconnect time
            global_disconnect_tick:lower(str(player)) = tick_time();
            global_reconnect_tick:lower(str(player)) = 0;
	,
	    schedule(3, '_logger', '--[BYPASS: SKIPPING CHECKS]--');
	);
    );
);



__on_player_attacks_entity(player, entity) -> (

    player_name = lower(str(player));
    entity_name = lower(str(entity));

    if (query(entity, 'player_type') == 'fake' && global_player_bots:player_name~entity_name == null && global_allow_griefing == false,

	logger(str('--[' + upper(player_name) + ' TRIED ATTACKING ' + upper(entity_name) + ' AND FAILED]--'));
	return('cancel');
    ,
	if (query(entity, 'player_type') == 'fake' && 
	    global_player_bots:player_name~entity_name == null && 
	    global_bypass_act_on_other_player_bots == true && 
	    global_allow_griefing == false &&
	    global_bypass~player_name != null,

	    logger(str('--[' + upper(player_name) + ' BYPASSED PROTECTION OF ' + upper(entity_name) + ']--'));
	);
    );
);