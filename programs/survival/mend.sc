//Place the item in off hand and right click to mend. 

__config() -> 
(
	m(
		l('stay_loaded', true)
	)
);

__on_player_uses_item(player, item_tuple, hand) ->
(
	holds_offhand = query(player, 'holds', 'offhand');
	l(off_item,off_count,off_nbt) = holds_offhand || l('None', 0, null);
	
	if(off_item != 'None' && __has_mending(off_nbt) &&  off_nbt ~ 'Damage',
		damage = get(off_nbt,'Damage');
		if(damage == 1,
			put(off_nbt,'Damage',0);
			inventory_set(player(), 40, 1, off_item,off_nbt);
		);
		if(damage > 1,
			__try_repair(player,off_item,off_nbt,damage);
		);
	);
);
//checks to see if item has mending
__has_mending(nbt) ->
(
ench = get(nbt, 'Enchantments[]');
		if (!ench, return(false));
		if (type(ench) != 'list', ench = l(ench));
		for (ench, if (get(_, 'id') == 'minecraft:mending', return(true)));
		false;
);

__try_repair(player,item_name,item_nbt,damage) ->
(
	
	cost = floor(damage/2);//it costs 1 xp to repair 2 damage.
	p_level = query(player,'xp_level');
	p_xp_progress = query(player,'xp_progress');
	//get total experience for level and progress
	total_xp = __xp_from_level(p_level)+ __xp_from_progress(p_level,p_xp_progress);
	
	
	
	if(total_xp != 0, 
		if(cost > total_xp,
			cost = cost - total_xp;
			damage = cost * 2;
			total_xp = 0;
		);
		if(cost <= total_xp,
			total_xp = total_xp - cost;
			damage = 0;
		);
	
		p_level = __get_level_from_xp(total_xp);
		p_xp_progress = __get_progress_from_xp(p_level,total_xp);
	
		
		put(item_nbt,'Damage',damage);
		inventory_set(player, 40, 1, item_name,item_nbt);
		modify(player, 'xp_level', p_level);
		modify(player, 'xp_progress', p_xp_progress);
	
		particle('happy_villager', pos(player)+l(0,1,0));
		pitch = rand(1)+0.7;
		sound('entity.player.levelup',pos(player), 0.5, pitch);
	);
);

__xp_from_level(level) ->
(
	if(level <= 16,
		return(level^2+6*level);
	);
	if(level >= 17 && level <= 31,
		return(2.5*level^2-40.5*level+360);
	);
	if(level >= 32,
		return(4.5*level^2-162.5*level+2220);
	);
);

__xp_from_progress(level,progress) ->
(
//We have to round the numbers becaue we are dealing witha a fraction
//for example If you have 5 xp into level 32 your xp will be 5.00000018626 xp
	if(level <= 15,
		return(round((2*level+7)*progress));
	);
	if(level >= 16 && level <= 30,
		return(round((5*level-38)*progress));
	);
	if(level >= 31,
		return(round((9*level-158)*progress));
	);
);

__get_level_from_xp(xp) ->
(
	if(xp <= 1005,
		return(floor(sqrt(xp+9)-3));
	);
	if(xp >= 1006 && xp <= 1627,
		return(floor((sqrt(40*xp-14319)+9)/10));
	);
	if(xp >= 1628,
		return(floor((sqrt(72*xp-54215)+325)/18));
	);
);

__get_progress_from_xp(level,xp) ->
(
	if(xp <= 1005,
		return((xp-(level^2+6*level))/(2*level+7));
	);
	if(xp >= 1006 && xp <= 1627,
		return((xp-(2.5*level^2-40.5*level+360))/(5*level-38));
	);
	if(xp >= 1628,
		return((xp-(4.5*level^2-162.5*level+2220))/(9*level-158));
	);
);





