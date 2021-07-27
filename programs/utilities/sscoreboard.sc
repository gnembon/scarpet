//statistic scoreboard interface by Lokdora
//dynamically create scoreboard based on statistics and assigned it with correct value (when player is online)
//options to distinguish bots from other players
//due to the scoreboard value cannot be assigned when player is offline, it is better to use this script to create scoreboard objectives for mid/long-term use (or singleplayer world).


__config() -> {
    'stay_loaded' -> 'true',
	'scope' -> 'global',
	'commands' -> {
		'' -> 'help',
		'clear' -> 'clear',
		'<stat> <displayName>' -> ['create','sidebar',false],
		'<stat> <displayName> <displaySlot>' -> ['create',false],
		'<stat> <displayName> <displaySlot> <includeBots>' -> 'create'
	},
	'arguments' -> {
		'stat' -> {'type' -> 'term'},
		'displayName' ->{'type' -> 'string'},
		'displaySlot' ->{'type' -> 'scoreboardslot'},
		'includeBots' -> {'type' -> 'bool'}
	}
};

__on_player_connects(p)->(
	if(!global_filter_bot||p~'player_type'!=fake,
		scoreboard('_ss_core',p~'name',statistic(p~'name',global_category,global_entry)) //player needs to login to update scoreboard values
	)
);

help() -> (
	print('/sscoreboard <stat> [<bot included>]');
	print('/sscoreboard clear');
	print('Supported stat format: \'<global_category>.<global_entry>\' or \'<global_entry>\', in which case the global_category is considerded as custom.');
	print('global_category includes:');
	print(' - mined(m);');
	print(' - crafted(c);');
	print(' - used(u);');
	print(' - broken(b);');
	print(' - picked_up(p);');
	print(' - dropped(d);');
	print(' - killed(k);');
	print(' - killed_by(kb);');
	print(' - custom(cu).');
);

create(stat,name,slot,includeBots) ->(
	scoreboard_remove('_ss_core');
	global_filter_bot=!includeBots;
	if(	stat~'\\.', [global_category,tmp,global_entry] = stat~'(.*)(\\.)(.*)',
		[global_category,global_entry] = ['custom',stat]
	);
	
	category_map = {
		'm'->'mined', 'mined'->'mined',
		'c'->'crafted', 'crafted'->'crafted',
		'u'->'used', 'used'->'used',
		'b'->'broken', 'broken'->'broken',
		'p'->'picked_up', 'picked_up'->'picked_up',
		'd'->'dropped', 'dropped'->'dropped',
		'k'->'killed', 'killed'->'killed',
		'kb'->'killed_by', 'killed_by'->'killed_by',
		'cu'->'custom', 'custom'->'custom'
	};
	if(category_map~global_category,
		[global_category,criterion]=[category_map:global_category,'minecraft.'+category_map:global_category+':'+global_entry],
		exit(print(format('rbu Error:','r Unknown global_category. Please check again.')))
	);
	try(
		scoreboard_add('_ss_core',criterion);
		'exception',
		exit(print(format('rbu Error:','r Unknown criterion. Please check again.')))
	);
	scoreboard_property('_ss_core','display_name',name);
	scoreboard_property('_ss_core','display_slot',slot);
	
	//cannot access to offline players' statistics since there is no map between uuids and usernames
	for(player('all'), scoreboard('_ss_core',_,statistic(_,global_category,global_entry))) 
);

clear() -> (
	scoreboard_remove('_ss_core');
	global_filter_bot = false;
);

__on_tick() -> (
	if(global_filter_bot,
		for(player('all'),if (_~'player_type'=='fake',scoreboard('_ss_core',_~'name',null)))
	);
)
