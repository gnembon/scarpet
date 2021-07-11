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

entity_load_handler('player',_assign(p,new) -> (
	if(!global_filter_bot||p~'player_type'!=fake,
		scoreboard('_ss_core',p~'name',statistic(p~'name',global_category,global_entry)) //player needs to login to update scoreboard values
	)
););

help() -> (
	print('/sscoreboard <stat> <[<bot included>]');
	print('/sscoreboard clear');
	print('Supported stat format: \'<global_category>.<global_entry>\' or \'<global_entry>\', in which case the global_category is cosidered as custom.');
	print('global_category included:');
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
	if(	
		global_category=='m'||global_category=='mined',	[global_category,criterion]=['mined','minecraft.mined:'+global_entry],
		global_category=='c'||global_category=='crafted',	[global_category,criterion]=['crafted','minecraft.crafted:'+global_entry],
		global_category=='u'||global_category=='used',	[global_category,criterion]=['used','minecraft.used:'+global_entry],
		global_category=='b'||global_category=='broken',	[global_category,criterion]=['broken','minecraft.broken:'+global_entry],
		global_category=='p'||global_category=='picked_up',	[global_category,criterion]=['picked_up','minecraft.picked_up:'+global_entry],
		global_category=='d'||global_category=='dropped',	[global_category,criterion]=['dropped','minecraft.dropped:'+global_entry],
		global_category=='k'||global_category=='killed',	[global_category,criterion]=['killed','minecraft.killed:'+global_entry],
		global_category=='kb'||global_category=='killed_by',	[global_category,criterion]=['killed_by','minecraft.killed_by:'+global_entry],
		global_category=='cu'||global_category=='custom',	[global_category,criterion]=['custom','minecraft.custom:'+global_entry],
		(print(format('rbu Error:','r Unknown global_category. Please check again.')),return())
	);
	try(
		scoreboard_add('_ss_core',criterion);
		'exception',
		(print(format('rbu Error:','r Unknown criterion. Please check again.')),return())
	);
	scoreboard_property('_ss_core','display_name',name);
	scoreboard_property('_ss_core','display_slot',slot);
	
	//cannot access to offline players' statistics since there is no map bwtween uuids and usernames
	for(player('*'), scoreboard('_ss_core',_,statistic(_,global_category,global_entry))) 
);

clear() -> (
	scoreboard_remove('_ss_core');
	global_filter_bot = false;
);

__on_tick() -> (
	if(global_filter_bot,
		for(player('*'),if (_~'player_type'=='fake',scoreboard('_ss_core',_~'name',null)))
	);
)