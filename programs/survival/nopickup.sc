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
global_forbidden = {}; // item_name -> pickup_limit

__on_player_connects(player) -> (
	_init();
);

__on_player_collides_with_entity(player, entity) -> (
	if (entity~'pickup_delay' == 0, // entity is an item and is ready to be picked up
		item = entity~'item';
		if (has(global_forbidden, item:0),
			pickup_limit = global_forbidden:(item:0);
			// check for pickup_limit = 0 seperately for performance reasons
			// avoids counting items in the player's inventory most of the time
			if (pickup_limit == 0 || _count_in_inventory(item) + item:1 > pickup_limit,
				// prevent pick up by increasing the pickup delay
				modify(entity, 'pickup_delay', 20);
			);
		);
	);
);

_init() -> (
	global_uuid = player()~'uuid';
	global_forbidden = parse_nbt(read_file(global_uuid, 'nbt'));
	if (global_forbidden == 'null', // config file for this UUID doesn't exist
		global_forbidden = {};
	);
);

// returns how many of "item" is in the player's inventory
_count_in_inventory(item) -> (
	num_slots = inventory_size(player());
	slot = -1;
	count = 0;
	while ((slot = inventory_find(player(), item:0, slot + 1)) != null, num_slots,
		count += inventory_get(player(), slot):1;
	);
	count;
);

// saves global_forbidden to the UUID's own config file
_save_to_file() -> (
	delete_file(global_uuid, 'nbt');
	if (global_forbidden != {}, // avoid converting an empty map to NBT
		write_file(global_uuid, 'nbt', encode_nbt(global_forbidden));
	);
);

list() -> (
	if (global_uuid == null, _init()); // needed in case script is reloaded when players are connected
	if (global_forbidden != {},
		print('List of forbidden items:');
		for (pairs(global_forbidden),
			print('	[' + _:0 + ', ' + _:1 + ']');
		);
	,
		print('There are no forbidden items');
	);
);

add(item) -> (
	if (global_uuid == null, _init()); // needed in case script is reloaded when players are connected
	if (has(global_forbidden, str(item:0)),
		print('[' + item:0 + '] is already forbidden');
	,
		put(global_forbidden, str(item:0), 0);
		_save_to_file();
		print('You can no longer pick up [' + item:0 + ']');
	);
);

// sets the pickup limit for an item in the blacklist
setlimit(item, count) -> (
	if (global_uuid == null, _init()); // needed in case script is reloaded when players are connected
	if (has(global_forbidden, str(item:0)),
		global_forbidden:(str(item:0)) = number(count);
		_save_to_file();
		print('Set pickup limit for [' + item:0 + '] to ' + count);
	,
		print('[' + item:0 + '] is not forbidden');
	);
);

remove(item) -> (
	if (global_uuid == null, _init()); // needed in case script is reloaded when players are connected
	if (has(global_forbidden, str(item:0)),
		delete(global_forbidden, str(item:0));
		_save_to_file();
		print('You can now pick up [' + item:0 + ']');
	,
		print('[' + item:0 + '] is not forbidden');
	);
);

clear() -> (
	if (global_uuid == null, _init()); // needed in case script is reloaded when players are connected
	global_forbidden = {};
	_save_to_file();
	print('Cleared the list of forbidden items');
);
