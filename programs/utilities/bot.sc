global_spawning_limit = 3;
global_saving_limit = 5;
global_json_name_1 = 'bot_summoned_num_dict';
global_json_name_2 = 'summoned_by';
global_json_name_3 = 'saved_bots';
global_json_name_4 = 'saved_bots_num';
global_summon_prefix = 'bot_';

__config() -> {
	'stay_loaded' -> 'true',
	'commands' -> {
		'summon <bot_name>' -> 'summon',
		'kick <bot_summoned_by_me>' -> 'kick',
		'savebot <saved_bot_name>' -> 'savebot',
		'load <saved_bot_name>' -> 'load',
		'del <saved_bot_name>' -> 'del',
		'attack <bot_summoned_by_me> <interval>' -> 'attack',
		'use <bot_summoned_by_me> <interval>' -> 'use',
	},
	'arguments' -> {
		'bot_name' -> {'type' -> 'term','suggest' -> []},
		'interval' -> {'type' -> 'int','suggest' -> [-1,0,11]},
		'saved_bot_name' -> {'type' -> 'term','suggester' -> _(args) -> get_saved_list(),},
		'bot_summoned_by_me' -> {'type' -> 'term','suggester' -> _(args) -> get_summoned_list(),},
	},
	'scope' -> 'global',
};


__on_start() -> {
	if
	(
		read_file(global_json_name_3,'shared_json') == null,
		(
			write_file(global_json_name_3,'shared_json',{});
		),
	);
	if
	(
		read_file(global_json_name_4,'shared_json') == null,
		(
			write_file(global_json_name_4,'shared_json',{});
		),
	);
	global_saved_dict = decode_json(read_file(global_json_name_3,'shared_json'));
	global_saved_num_dict = decode_json(read_file(global_json_name_4,'shared_json'));
	global_summoned_by_dict = {};
	global_summoned_num_dict = {};
};
__on_close() -> {
	write_file(global_json_name_3,'shared_json',global_saved_dict);
	write_file(global_json_name_4,'shared_json',global_saved_num_dict);
};

__command() -> {
	
};

summon(originalbotname) -> (
	botname = lower(global_summon_prefix + originalbotname);
	p = player();
	player_name = p ~ 'name';
	handle_summon_request(player_name, botname);
);

kick(originalbotname) -> (
	botname = lower(global_summon_prefix + originalbotname);
	p = player();
	player_name = p ~ 'name';
	schedule(0, 'handle_kick_request', player_name, botname);
);

attack(originalbotname,interval) -> (
	p = player();
	player_name = p ~ 'name';
	botname = lower(global_summon_prefix + originalbotname);
	schedule(0, 'handle_attack_request', player_name, botname, interval);
);

use(originalbotname,interval) -> (
	p = player();
	player_name = p ~ 'name';
	botname = lower(global_summon_prefix + originalbotname);
	schedule(0, 'handle_use_request', player_name, botname, interval);
);

savebot(originalbotname) -> (
	p = player();
	botname = lower(global_summon_prefix + originalbotname);
	handle_save_request(p, botname);
);

load(originalbotname) -> (
	p = player();
	botname = lower(global_summon_prefix + originalbotname);
	handle_load_request(p, botname);
);

del(originalbotname) -> (
	p = player();
	botname = lower(global_summon_prefix + originalbotname);
	handle_del_request(p, botname);
);

__on_player_dies(player) -> {
	botname = lower(player ~ 'name');
	schedule(0, 'handle_bot_dies_event', botname);
};

handle_summon_request(player_name,botname) -> (
	saved_by = get(global_saved_dict,botname,'saved_by');
	if
	(
		saved_by == null || saved_by == player_name,
		(
			summoned_by = get(global_summoned_by_dict,botname);
			if
			(
				//如果该bot没有被任何人生成
				summoned_by == null,
				(
					spawned_player_num = get(global_summoned_num_dict,player_name);
					if
					(
						//该玩家未生成任何bot
						spawned_player_num == null,
						(
							put(global_summoned_num_dict,player_name,1);
							put(global_summoned_by_dict,botname,player_name);
							run(str('player %s spawn in survival',botname));
							run(str('tp %s %s',botname,player_name));
							display_title(player_name, 'clear');
							display_title(player_name, 'actionbar', format('d You have summoned 1 Bots now'), 5,5,5);
						),
						//该玩家生成的bot量未达到上限
						spawned_player_num < global_spawning_limit,
						(
							put(global_summoned_num_dict,player_name,spawned_player_num+1);
							put(global_summoned_by_dict,botname,player_name);
							run(str('player %s spawn in survival',botname));
							run(str('tp %s %s',botname,player_name));
							display_title(player_name, 'clear');
							display_title(player_name, 'actionbar', format('d '+str('You have summoned %d Bots now',spawned_player_num+1)), 5,5,5);
						),
						spawned_player_num == global_spawning_limit,
						(
							display_title(player_name, 'clear');
							display_title(player_name, 'actionbar', format('rb [Warning!!] '+str('You have already summoned %d Bots!',global_spawning_limit)),1,40,1);
						),
					);
				),
				//如果该bot已经被调用命令的玩家生成
				summoned_by == player_name,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('lb [Warning!!] That Bot has been summoned by you!'),1,40,1);
				),
				//如果该bot已经被其他玩家生成
				summoned_by != player_name,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('vb [Warning!!] That Bot has been summoned by others!'),1,40,1);
				),
				
			);
		),
		saved_by != player_name,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('vb [Warning!!] That Bot has been saved by others!'),1,40,1);
		),
	),
);

handle_kick_request(player_name,botname) -> (
	summoned_by = get(global_summoned_by_dict,botname);
	if
	(
		summoned_by == null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('lb [Warning!!] The Bot has not been summoned!'),1,40,1);
		),
		summoned_by == player_name,
		(
			spawned_player_num = get(global_summoned_num_dict,player_name);
			put(global_summoned_num_dict,player_name,spawned_player_num-1);
			run(str('player %s kill',botname));
			delete(global_summoned_by_dict,botname);
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('d '+str('Kicked %s',botname)),1,40,1);
		),
		summoned_by != player_name,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] None of your business!'),1,40,1);
		),
	),
);

handle_attack_request(player_name,botname,interval) -> (
	summoned_by = get(global_summoned_by_dict,botname);
	if
	(
		summoned_by == null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('lb [Warning!!] The Bot has not been summoned!'),1,40,1);
		),
		player_name == summoned_by,
		(
			if
			(
				interval == -1,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s is attacking continuously!',botname)),1,40,1);
					run(str('player %s attack continuous',botname));
				),
				interval == 0,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s attacked once!',botname)),1,40,1);
					run(str('player %s attack once',botname));
				),
				interval > 0,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s will attack every %d tick!',botname,interval)),1,40,1);
					run(str('player %s attack interval %d',botname,interval));
				),
				
			),
		),
		player_name != summoned_by,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] None of your business!'),1,40,1);
		),
	),
);

handle_use_request(player_name,botname,interval) -> (
	summoned_by = get(global_summoned_by_dict,botname);
	if
	(
		summoned_by == null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('lb [Warning!!] The Bot has not been summoned!'),1,40,1);
		),
		player_name == summoned_by,
		(
			if
			(
				interval == -1,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s is using continuously!',botname)),1,40,1);
					run(str('player %s use continuous',botname));
				),
				interval == 0,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s used once!',botname)),1,40,1);
					run(str('player %s use once',botname));
				),
				interval > 0,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s will use every %d tick!',botname,interval)),1,40,1);
					run(str('player %s use interval %d',botname,interval));
				),
				
			),
		),
		player_name != summoned_by,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] None of your business!'),1,40,1);
		),
	),
);

handle_bot_dies_event(botname) -> (
	summoned_by = get(global_summoned_by_dict,botname);
	if
	(
		summoned_by != null,
		(
			spawned_player_num = get(global_summoned_num_dict,summoned_by);
			put(global_summoned_num_dict,summoned_by,spawned_player_num-1);
			delete(global_summoned_by_dict,botname);
		),	
	),
);

handle_save_request(p, botname) -> (
	player_name = p ~ 'name';
	saved_by = get(global_saved_dict,botname,'saved_by');
	summoned_by = get(global_summoned_by_dict,botname);
	if
	(
		summoned_by != null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] You can only save a bot when it\'s offline!'),1,40,1);
			return();
		),
	);
	if
	(
		saved_by == null,
		(
			saved_num = number(get(global_saved_num_dict,player_name));
			if
			(
				saved_num < global_saving_limit,
				(
					bot_info = m(l('saved_by',player_name),l('pos',query(p,'location')),l('dim',p ~ 'dimension'));
					put(global_saved_dict,botname,bot_info);
					put(global_saved_num_dict,player_name,saved_num+1);
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('d '+str('%s has been saved! Now you saved %d bots',botname,saved_num+1)); );
				),
				saved_num == global_saving_limit,
				(
					display_title(player_name, 'clear');
					display_title(player_name, 'actionbar', format('rb [Warning!!] '+str('You have already saved %d Bots!',global_saving_limit)),1,40,1);
				),
			);
			
		),
		saved_by == player_name,
		(
			bot_info = m(l('saved_by',player_name),l('pos',query(p,'location')),l('dim',p ~ 'dimension'));
			put(global_saved_dict,botname,bot_info);
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('d '+str('%s has been modified',botname)) );
		),
		saved_by != player_name,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] None of your business!'),1,40,1);
		),
	);
);

handle_load_request(p, botname) -> (
	player_name = p ~ 'name';
	bot_info = get(global_saved_dict,botname);
	saved_by = get(bot_info,'saved_by');
	x = get(bot_info,'pos',0);y = get(bot_info,'pos',1);z = get(bot_info,'pos',2);yaw = get(bot_info,'pos',3);pitch = get(bot_info,'pos',4);
	dim = get(bot_info,'dim');
	if
	(
		saved_by == null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('lb [Warning!!] The Bot has not been saved!'),1,40,1);
		),
		saved_by == player_name,
		(
			summoned_by = get(global_summoned_by_dict,botname);
			if
			(
				summoned_by == null,
				(
					spawned_player_num = get(global_summoned_num_dict,player_name);
					if
					(
						spawned_player_num == null,
						(
							run(str('player %s spawn at %.4f %.4f %.4f facing %.4f %.4f in %s in survival',botname,x,y,z,yaw,pitch,dim));
							put(global_summoned_num_dict,player_name,1);
							put(global_summoned_by_dict,botname,player_name);
							display_title(player_name, 'clear');
							display_title(player_name, 'actionbar', format('d '+str('%s has been summoned',botname)) );
						),
						spawned_player_num < global_spawning_limit,
						(
							run(str('player %s spawn at %.4f %.4f %.4f facing %.4f %.4f in %s in survival',botname,x,y,z,yaw,pitch,dim));
							put(global_summoned_num_dict,player_name,spawned_player_num+1);
							put(global_summoned_by_dict,botname,player_name);
							display_title(player_name, 'clear');
							display_title(player_name, 'actionbar', format('d '+str('%s has been summoned',botname)) );
						),
						spawned_player_num == global_spawning_limit,
						(
							display_title(player_name, 'clear');
							display_title(player_name, 'actionbar', format('rb [Warning!!] '+str('You have already summoned %d Bots!',global_spawning_limit)),1,40,1);
						)
					);
					summoned_by == player_name,
					(
						display_title(player_name, 'clear');
						display_title(player_name, 'actionbar', format('lb [Warning!!] That Bot has been summoned by you!'),1,40,1);
					);
				);
			);
		),
		saved_by != player_name,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] None of your business!'),1,40,1);
		),
	);
);

handle_del_request(p, botname) -> (
	player_name = p ~ 'name';
	saved_by = get(global_saved_dict,botname,'saved_by');
	summoned_by = get(global_summoned_by_dict,botname);
	if
	(
		summoned_by != null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] You can only delete a bot when it\'s offline!'),1,40,1);
			return();
		),
	);
	if
	(
		saved_by == null,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('lb [Warning!!] The Bot has not been saved!'),1,40,1);
		),
		saved_by == player_name,
		(
			saved_num = get(global_saved_num_dict,player_name);
			delete(global_saved_dict,botname);
			put(global_saved_num_dict,player_name,saved_num-1);
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('d '+str('%s has been deleted! Now you have saved %d bots',botname,saved_num-1)); );
		),
		saved_by != player_name,
		(
			display_title(player_name, 'clear');
			display_title(player_name, 'actionbar', format('rb [Warning!!] None of your business!'),1,40,1);
		),
	);
);

get_saved_list() -> {
	p = player();
	player_name = p ~ 'name';
	nameset = [];
	for(keys(global_saved_dict),if(player_name == get(global_saved_dict,_,'saved_by'),nameset += slice(_,4)));
	return(nameset);
};

get_summoned_list() -> {
	p = player();
	player_name = p ~ 'name';
	nameset = [];
	for(keys(global_summoned_by_dict),if(player_name == get(global_summoned_by_dict,_),nameset += slice(_,4)));
	return(nameset);
};