__holds(entity, item_regex, enchantment) -> 
(
	if (entity~'gamemode_id'==3, return(0));
	for(l('mainhand','offhand'),
		holds = query(entity, 'holds', _);
		if( holds,
			l(what, count, nbt) = holds;
			if ((what ~ item_regex) && (enchs = get(nbt,'Enchantments[]')),
				if (type(enchs)!='list', enchs = l(enchs));
				for (enchs, 
					if ( get(_,'id') == 'minecraft:'+enchantment,
						lvl = max(lvl, get(_,'lvl'))
					)
				)	
			)
		)
	);
	lvl
);

__distance(v1, v2) -> sqrt(reduce(v1-v2,_*_+_a,0));
//( its faster than the equivalent:
//	l(x,y,z) = v1;
//	l(a,b,c) = v2;
//	sqrt((x-a)^2+(y-b)^2+(z-c)^2)
//);

__on_player_breaks_block(player, block) ->
(
	if (!(player ~ 'sneaking'), return());
	eff_level = __holds(player, '_pickaxe', 'sharpness');
	if (eff_level == 0, return());
	block_name = str(block);
	l(x,y,z) = pos(block);
	__cascade_pick(player, 2+eff_level, block_name, pos(block), false)
);

__cascade_pick(player, ttl, block_name, position, break_block) ->
(
	if (ttl <= 0, return());
	if (break_block,
		block = block(position);
		if (block != block_name, return());
		//set(block, 'air')
		harvest(player, block);
	);
	l(x,y,z) = position;
	penalty = 0.5+rand(1);
	for (rect(x,y,z),
		if((_ == block_name) && (distance = __distance(pos(_), position)) > 0,
			schedule(
				2+ceil(rand(4*distance*distance)),
				'__cascade_pick',
				player, ttl-penalty*distance,
				block_name, pos(_), true
			)
		)
	)
)
