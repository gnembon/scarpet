// requires carpet 1.2.1 (towards scarpet v1.6)

// scripts that spreads torches in caves up to 128 block in a sphere around the player
// currently it works 'creatively', so does not consume torches from the player inventory_set
// also - works only with one player in the game - multiplayer support could be added if `schedule`
// is used instead of __on_tick, or if player is passed in various functions.

///right click on a torch while looking at nothing to toggle.

global_spread_love;
__config() -> m(l('scope', 'global'));

__on_player_uses_item(p, it, hand) ->
(
	if (hand != 'mainhand', return());
	if (it:0 == 'torch',
		ench = it:2:'Enchantments[]';
		global_spread_love = 0;
		delete(it:2:'Enchantments');
		if (!ench,
			global_spread_love = 1;
			if (ench==null, it:2 = nbt('{}'));
			put(it:2:'Enchantments','[]');
			put(it:2:'Enchantments', '{lvl:1s,id:"minecraft:protection"}', 0);
		);
		inventory_set(p, p~'selected_slot', it:1, it:0, it:2);
	) 
);

__distance_sq(vec1, vec2) -> reduce(vec1 - vec2, _a + _*_, 0);

global_effect_radius = 128;

__on_tick() ->
(
	
	if (global_spread_love && (p = player())~'holds':0 == 'torch',
		cpos = pos(p);
		d = global_effect_radius*2;
		loop(4000,
			lpos = cpos+l(rand(d), rand(d), rand(d)) - d/2;
			if (__distance_sq(cpos, lpos) <= 16384  
					&& air(lpos) && light(lpos) < 8 && sky_light(lpos) < 8
					&& solid(pos_offset(lpos, 'down')),
				__send_torch(p, lpos);
				success += 1;
				if (success > 2, return());
			)
		)
	)
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
	scan(x,y,z,128,128,128,if(_=='torch', set(_, 'air')));
)


