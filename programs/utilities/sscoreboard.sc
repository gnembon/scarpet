//statistic scoreboard interface by Lokdora
//dynamically create scoreboard based on statistics and assigned it with correct value (when player is online)
//options to distinguish bots from other players
//due to the scoreboard value cannot be assigned when player is offline, it is better to use this script to create scoreboard objectives for mid/long-term use (or singleplayer world).

  
global_custom_stats = ['animals_bred','aviate_one_cm','bell_ring','boat_one_cm','clean_armor','clean_banner','clean_shulker_box','climb_one_cm','crouch_one_cm','damage_absorbed','damage_blocked_by_shield','damage_dealt','damage_dealt_absorbed','damage_dealt_resisted','damage_resisted','damage_taken','deaths','drop','eat_cake_slice','enchant_item','fall_one_cm','fill_cauldron','fish_caught','fly_one_cm','horse_one_cm','inspect_dispenser','inspect_dropper','inspect_hopper','interact_with_anvil','interact_with_beacon','interact_with_blast_furnace','interact_with_brewingstand','interact_with_campfire','interact_with_cartography_table','interact_with_crafting_table','interact_with_furnace','interact_with_grindstone','interact_with_lectern','interact_with_loom','interact_with_smithing_table','interact_with_smoker','interact_with_stonecutter','jump','leave_game','minecart_one_cm','mob_kills','open_barrel','open_chest','open_enderchest','open_shulker_box','pig_one_cm','play_noteblock','play_record','play_time','player_kills','pot_flower','raid_trigger','raid_win','sleep_in_bed','sneak_time','sprint_one_cm','strider_one_cm','swim_one_cm','talked_to_villager','target_hit','time_since_death','time_since_rest','total_world_time','traded_with_villager','trigger_trapped_chest','tune_noteblock','use_cauldron','walk_on_water_one_cm','walk_one_cm','walk_under_water_one_cm'];
global_scoreboard_stats = ['air','armor','deathCount','food','health','level','playerKillCount','totalKillCount','xp'];
__config() -> {
    'stay_loaded' -> 'true',
	'scope' -> 'global',
	'commands' -> {
		'' -> 'help',
		'clear' -> 'clear',

        // No display name, sidebar, no bots 
        '<scoreboardStats>' ->      _(ss) -> create(ss, null, 'sidebar', false),
        'broken <itemName>' ->      _(in) -> create('minecraft.broken:minecraft.' + in,    null, 'sidebar', false),
		'crafted <itemName>' ->     _(in) -> create('minecraft.crafted:minecraft.' + in,   null, 'sidebar', false),
        'custom <customStats>' ->   _(cs) -> create('minecraft.custom:minecraft.' + cs,    null, 'sidebar', false),
		'dropped <itemName>' ->     _(in) -> create('minecraft.dropped:minecraft.' + in,   null, 'sidebar', false),
        'killed <entityName>' ->    _(en) -> create('minecraft.killed:minecraft.' + en,    null, 'sidebar', false),
		'killed_by <entityName>' -> _(en) -> create('minecraft.killed_by:minecraft.' + en, null, 'sidebar', false),
        'mined <blockName>' ->      _(bn) -> create('minecraft.mined:minecraft.' + bn,     null, 'sidebar', false),
        'picked_up <itemName>' ->   _(in) -> create('minecraft.picked_up:minecraft.' + in, null, 'sidebar', false),
        'used <itemName>' ->        _(in) -> create('minecraft.used:minecraft.' + in,      null, 'sidebar', false),
        
        // Sidebar, no bots
        '<scoreboardStats> <displayName>' ->      _(ss, displayName) -> create(ss,                                  displayName, 'sidebar', false),
        'broken <itemName> <displayName>' ->      _(in, displayName) -> create('minecraft.broken:minecraft.' + in,    displayName, 'sidebar', false),
		'crafted <itemName> <displayName>' ->     _(in, displayName) -> create('minecraft.crafted:minecraft.' + in,   displayName, 'sidebar', false),
        'custom <customStats> <displayName>' ->   _(cs, displayName) -> create('minecraft.custom:minecraft.' + cs,    displayName, 'sidebar', false),
		'dropped <itemName> <displayName>' ->     _(in, displayName) -> create('minecraft.dropped:minecraft.' + in,   displayName, 'sidebar', false),
        'killed <entityName> <displayName>' ->    _(en, displayName) -> create('minecraft.killed:minecraft.' + en,    displayName, 'sidebar', false),
		'killed_by <entityName> <displayName>' -> _(en, displayName) -> create('minecraft.killed_by:minecraft.' + en, displayName, 'sidebar', false),
        'mined <blockName> <displayName>' ->      _(bn, displayName) -> create('minecraft.mined:minecraft.' + bn,     displayName, 'sidebar', false),
        'picked_up <itemName> <displayName>' ->   _(in, displayName) -> create('minecraft.picked_up:minecraft.' + in, displayName, 'sidebar', false),
        'used <itemName> <displayName>' ->        _(in, displayName) -> create('minecraft.used:minecraft.' + in,      displayName, 'sidebar', false),
        
        // No bots
        '<scoreboardStats> <displayName> <displaySlot>' ->      _(ss, displayName, displaySlot) -> create(ss,                                    displayName, displaySlot, false),
        'broken <itemName> <displayName> <displaySlot>' ->      _(in, displayName, displaySlot) -> create('minecraft.broken:minecraft.' + in,    displayName, displaySlot, false),
		'crafted <itemName> <displayName> <displaySlot>' ->     _(in, displayName, displaySlot) -> create('minecraft.crafted:minecraft.' + in,   displayName, displaySlot, false),
        'custom <customStats> <displayName> <displaySlot>' ->   _(cs, displayName, displaySlot) -> create('minecraft.custom:minecraft.' + cs,    displayName, displaySlot, false),
		'dropped <itemName> <displayName> <displaySlot>' ->     _(in, displayName, displaySlot) -> create('minecraft.dropped:minecraft.' + in,   displayName, displaySlot, false),
        'killed <entityName> <displayName> <displaySlot>' ->    _(en, displayName, displaySlot) -> create('minecraft.killed:minecraft.' + en,    displayName, displaySlot, false),
		'killed_by <entityName> <displayName> <displaySlot>' -> _(en, displayName, displaySlot) -> create('minecraft.killed_by:minecraft.' + en, displayName, displaySlot, false),
        'mined <blockName> <displayName> <displaySlot>' ->      _(bn, displayName, displaySlot) -> create('minecraft.mined:minecraft.' + bn,     displayName, displaySlot, false),
        'picked_up <itemName> <displayName> <displaySlot>' ->   _(in, displayName, displaySlot) -> create('minecraft.picked_up:minecraft.' + in, displayName, displaySlot, false),
        'used <itemName> <displayName> <displaySlot>' ->        _(in, displayName, displaySlot) -> create('minecraft.used:minecraft.' + in,      displayName, displaySlot, false),
        
        //
        '<scoreboardStats> <displayName> <displaySlot> <includeBots>' ->      _(ss, displayName, displaySlot, includeBots) -> create(ss,                                    displayName, displaySlot, includeBots),
        'broken <itemName> <displayName> <displaySlot> <includeBots>' ->      _(in, displayName, displaySlot, includeBots) -> create('minecraft.broken:minecraft.' + in,    displayName, displaySlot, includeBots),
		'crafted <itemName> <displayName> <displaySlot> <includeBots>' ->     _(in, displayName, displaySlot, includeBots) -> create('minecraft.crafted:minecraft.' + in,   displayName, displaySlot, includeBots),
        'custom <customStats> <displayName> <displaySlot> <includeBots>' ->   _(cs, displayName, displaySlot, includeBots) -> create('minecraft.custom:minecraft.' + cs,    displayName, displaySlot, includeBots),
		'dropped <itemName> <displayName> <displaySlot> <includeBots>' ->     _(in, displayName, displaySlot, includeBots) -> create('minecraft.dropped:minecraft.' + in,   displayName, displaySlot, includeBots),
        'killed <entityName> <displayName> <displaySlot> <includeBots>' ->    _(en, displayName, displaySlot, includeBots) -> create('minecraft.killed:minecraft.' + en,    displayName, displaySlot, includeBots),
		'killed_by <entityName> <displayName> <displaySlot> <includeBots>' -> _(en, displayName, displaySlot, includeBots) -> create('minecraft.killed_by:minecraft.' + en, displayName, displaySlot, includeBots),
        'mined <blockName> <displayName> <displaySlot> <includeBots>' ->      _(bn, displayName, displaySlot, includeBots) -> create('minecraft.mined:minecraft.' + bn,     displayName, displaySlot, includeBots),
        'picked_up <itemName> <displayName> <displaySlot> <includeBots>' ->   _(in, displayName, displaySlot, includeBots) -> create('minecraft.picked_up:minecraft.' + in, displayName, displaySlot, includeBots),
        'used <itemName> <displayName> <displaySlot> <includeBots>' ->        _(in, displayName, displaySlot, includeBots) -> create('minecraft.used:minecraft.' + in,      displayName, displaySlot, includeBots)
	},
	'arguments' -> {
		'blockName' ->       {'type' -> 'term',   'suggest' -> block_list(),            'option' -> block_list()},
        'itemName' ->        {'type' -> 'term',   'suggest' -> item_list(),             'option' -> item_list()},
        'entityName' ->      {'type' -> 'term',   'suggest' -> entity_types(),          'option' -> entity_types()},
        'customStats' ->     {'type' -> 'term',   'suggest' -> global_custom_stats,     'option' -> global_custom_stats},
        'scoreboardStats' -> {'type' -> 'term',   'suggest' -> global_scoreboard_stats, 'option' -> global_scoreboard_stats},
		'displayName' ->     {'type' -> 'string', 'suggest' -> ['displayName']},
		'displaySlot' ->     {'type' -> 'scoreboardslot'},
		'includeBots' ->     {'type' -> 'bool'}
	}
};

__on_player_connects(p)->(
	if(!global_filter_bot||p~'player_type'!=fake,
		scoreboard('_ss_core',p~'name',statistic(p~'name',global_category,global_entry)) //player needs to login to update scoreboard values
	)
);

help() -> (
	print('/sscoreboard <stat> [displayName] [displaySlot] [<bot included>]');
	print('/sscoreboard clear');
);

create(stat,name,slot,includeBots) -> (
	scoreboard_remove('_ss_core');
	global_filter_bot=!includeBots;

    if((stats = stat ~ '^minecraft.(\\w+):minecraft.(\\w+)$') == null, 
        stats = ['', stat]
    );
    try(
	    scoreboard_add('_ss_core',stat);
	    'exception',
	    exit(print(format('rbu Error:','r Unknown criterion. Please check again.')))
	);
	
    name = name || if(stats:0 != 'custom', title(stats:0+' '), '') + title(replace(stats:1, '_', ' '));
	scoreboard_property('_ss_core','display_name',name);
	scoreboard_property('_ss_core','display_slot',slot);
	
	//cannot access to offline players' statistics since there is no map between uuids and usernames
	for(player('all'), scoreboard('_ss_core',_,statistic(_,stat:0,stat:1))) 
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
