// requires carpet 1.2.1 (towards scarpet v1.6)

// scripts that spreads torches in caves up to 128 block in a sphere around the player
// in survival gamemodes consumes torches from the player inventory

///right click on a torch while looking at nothing to toggle.

//Here you can customize the app to your needs
__on_start() -> (
	global_effect_radius = 128; //How far away you want to send torches

	global_min_light_level = __light_level_for_version(); //1 for 1.18+, 8 for 1.17 and earlier

	global_light_ground = true; //Whether or not we want to light the surface
);


// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_uses_item(player, item, hand) ->
(
	if (hand != 'mainhand', return());
	if (item:0 == 'torch',
		ench = item:2:'Enchantments[]';
		global_spread_love = 0;
		delete(item:2:'Enchantments');
		if (!ench && player~'gamemode_id'!=3,
			global_spread_love = 1;
			if (ench==null, item:2 = nbt('{}'));
			put(item:2:'Enchantments','[]');
			put(item:2:'Enchantments', '{lvl:1s,id:"minecraft:protection"}', 0);
		    	global_survival=!(player~'gamemode_id' % 2);
			schedule(0, 'spread_torches', player, player~'gamemode_id');
		);
		inventory_set(player, player~'selected_slot', item:1, item:0, item:2);
	) 
);

__distance_sq(vec1, vec2) -> reduce(vec1 - vec2, _a + _*_, 0);

__light_level_for_version()-> if(system_info('game_major_target')>=18, 1, 8); //1 for 1.18+, 8 for 1.17 and earlier

global_survival=false;

spread_torches(player, initial_gamemode) ->
(
	if (global_spread_love && player~'holds':0 == 'torch' && player ~'gamemode_id' == initial_gamemode,
		is_survival = global_survival;
		cpos = pos(player);
		d = global_effect_radius*2;
		dd = global_effect_radius*global_effect_radius;
		loop(4000,
			lpos = cpos+l(rand(d), rand(d), rand(d)) - d/2;
			if (__distance_sq(cpos, lpos) <= dd  
					&& air(lpos) && block_light(lpos) < global_min_light_level && (global_light_ground || light(lpos) < global_min_light_level)
					&& solid(pos_offset(lpos, 'down')),
				if (is_survival && not_able_loose_torch(player),
					//running out of torches as survival
					return();
				);
				__send_torch(player, lpos);
				success += 1;
				if (success > 2, 
					//spread the limit already, continue next tick
					schedule(1, 'spread_torches', player, initial_gamemode);
					return()
				);
			)
		);
		// failed to find a spot, but still have space
		schedule(1, 'spread_torches', player, initial_gamemode)
	)
);

not_able_loose_torch(p) ->
(
	// we are not using inventory_remove, because we would like to skip the holding slot if possible
	slot = inventory_find(p, 'torch', p~'selected_slot'+1);
	if (slot == null,
		slot = inventory_find(p, 'torch');
		if (slot == null, return(true));
	);
	item = inventory_get(p, slot);
	inventory_set(p, slot, item:1-1, item:0, item:2);
	false
);

torch_test() ->
(
	// can a torch ride an empty AOE cloud
	cpos = pos(player());
	aoc = spawn('area_effect_cloud',cpos, '{Radius:0,Duration:200}');
	falling_block = spawn('falling_block',cpos, '{BlockState:{Name:"minecraft:torch"},Time:500,NoGravity:1,DropItem:0}');
	modify(falling_block,'mount',aoc);
	// answer is yes.
);

__torch_tick(entity, start, end) ->
(
	if (entity~'age' >= 20,
		// after 2.5 seconds, perish,and fix block
		modify(entity, 'remove');
		if (air(end) && solid(pos_offset(end,'down')),
			set(end, 'torch');
		);
		return()
	);
	stage = __find_stage(entity~'age', 20);
	final_pos = start*(1-stage)+(end+l(0.5,0,0.5))*stage;
	stage = __find_stage(entity~'age'+1, 20);
	next_pos = start*(1-stage)+(end+l(0.5,0,0.5))*stage;
	modify(entity, 'pos', final_pos);
	modify(entity, 'motion', 1.0111*(next_pos-final_pos));
);

__send_torch(p, destination) -> // __send_torch(p, l(3035,68,1138))
(
	start_position = pos(p)+l(0,p~'eye_height',0)+p~'look';
	// using falling blocks instead of armorstands, since it looks better
	// giving them 50 ticks of life just in case
	marker = spawn('falling_block',start_position, '{BlockState:{Name:"minecraft:torch"},Time:'+(600-50)+',NoGravity:1,DropItem:0}');
	// scarpet trick to have them never set as block (be on ground)
	modify(marker,'no_clip');
	entity_event(marker, 'on_tick', '__torch_tick', pos(marker), destination);
);

__find_stage(age, maxage) ->
(
	//center
	// tanh spread - very smooth beginning and ending
	//spread = maxage/2;
	//(tanh(4*(age-spread)/spread)+1)/2
	
	//circular spread
	// smooth start, fast ending
	1-sqrt(maxage*maxage-age*age)/maxage
	
	// parabolic spread - less hesitation at the beginning
	//(age/maxage)^2
	
	//linear, simplest
	//age/maxage
);


clear_all_torches() ->
(
	l(x,y,z) = pos(player());
	d = global_effect_radius;
	scan(x,y,z,d,d,d,if(_=='torch', set(_, 'air')));
)
