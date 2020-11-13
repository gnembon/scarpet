// prevents unwanted items from filling up player inventories by allowing the
// player to disable picking up specific items from the ground

// requires carpet fabric-carpet-1.16.4-1.4.16+v201105 or above
// works on both singleplayer and multiplayer
// init is triggered on player logon, so if anything doesn't work, try relogging

// written by KonaeAkira

__config() -> {
	'stay_loaded' -> true,
	'legacy_command_type_support' -> true,
	'arguments' -> {
		'count' -> {
			'type' -> 'int',
			'min' -> 0
		}
	}
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
		item = entity~'item';
		if (has(global_forbidden, item:0),
			pickup_limit = global_forbidden:(item:0);
			if (pickup_limit == 0 || _count_in_inventory(item) + item:1 > pickup_limit,
				modify(entity, 'pickup_delay', 20);
			);
		);
	);
);

_count_in_inventory(item) -> (
	num_slots = inventory_size(player());
	slot = -1;
	count = 0;
	while ((slot = inventory_find(player(), item:0, slot + 1)) != null, num_slots,
		count += inventory_get(player(), slot):1;
	);
	return(count);
);

_save_to_file() -> (
	if (global_uuid == null, global_uuid = player~'uuid');
	delete_file(global_uuid, 'nbt');
	write_file(global_uuid, 'nbt', encode_nbt(global_forbidden, true));
);

list() -> (
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

setlimit(item, count) -> (
	if (has(global_forbidden, str(item:0)),
		global_forbidden:(str(item:0)) = number(count);
		_save_to_file();
		print('Set pickup limit for [' + item:0 + '] to ' + count);
	,
		print('[' + item:0 + '] is not forbidden');
	);
);

remove(item) -> (
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
