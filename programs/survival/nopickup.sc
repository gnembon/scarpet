// prevents unwanted items from filling up player inventories by allowing the
// player to disable picking up specific items from the ground

// requires carpet fabric-carpet-1.16.4-1.4.16+v201105 or above
// works on both singleplayer and multiplayer
// init is triggered on player logon, so if anything doesn't work, try relogging

// written by KonaeAkira

__config() -> {
	'stay_loaded' -> true,
	'legacy_command_type_support' -> true
};

__command() -> null;

global_uuid = null;
global_forbidden = {};

__on_player_connects(player) -> (
	global_uuid = player~'uuid';
	global_forbidden = parse_nbt(read_file(global_uuid, 'nbt'));
	if (global_forbidden == 'null',
		global_forbidden = {};
	);
);

__on_player_collides_with_entity(player, entity) -> (
	if (entity~'pickup_delay' == 0,
		if (has(global_forbidden, entity~'item':0),
			modify(entity, 'pickup_delay', 20);
		);
	);
);

_save_to_file() -> (
	if (global_uuid == null, global_uuid = player~'uuid');
	delete_file(global_uuid, 'nbt');
	write_file(global_uuid, 'nbt', encode_nbt(global_forbidden, true));
);

list() -> (
	// print(global_forbidden);
	if (global_forbidden != {},
		print('List of forbidden items:');
		print(keys(global_forbidden));
	,
		print('There are no forbidden items');
	);
	return(null);
);

add(item) -> (
	if (has(global_forbidden, str(item:0)),
		print('[' + item:0 + '] is already forbidden');
	,
		put(global_forbidden, str(item:0), 0);
		_save_to_file();
		print('You can no longer pick up [' + item:0 + ']');
	);
	return(null);
);

remove(item) -> (
	item_str = str(item:0);
	if (has(global_forbidden, str(item:0)),
		delete(global_forbidden, str(item:0));
		_save_to_file();
		print('You can now pick up [' + item:0 + ']');
	,
		print('[' + item:0 + '] is not forbidden');
	);
	return(null);
);

clear() -> (
	global_forbidden = {};
	print('Cleared the list of forbidden items');
	return(null);
);
