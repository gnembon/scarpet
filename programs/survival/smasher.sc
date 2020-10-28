//scarpet v1.5

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

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

__on_player_clicks_block(player, block, face)->
(
	power_level = __holds(player, '_pickaxe', 'power');
	if (power_level == 0, return());
	__cascade_smash( pos(block), power_level, pos(block) )
);

__is_invalid_for_smashing(block) -> 
	air(block) || block == 'lava' || block == 'water' || 
	block == 'bedrock' || block == 'barrier' || 
	block ~ 'command_block' || block == 'structure_block' || block == 'jigsaw';

__cascade_smash(center_pos, ttl, position) ->
(
	block = block(position);
	if (ttl <= 0 || __is_invalid_for_smashing(block), return());
	block_data = block_data(block);
	properties_tag = if ( property_list = filter(block_properties(block),_!='waterlogged'),
		properties = nbt('{}');
		for(property_list,
			value = property(block, _);
			if (block == 'chest' && _ == 'type', value = 'single');
			put(properties, _, '"'+value+'"')
		);
		properties
	);
	block_name = str(block);
	set(position, if(property(block,'waterlogged')=='true','water','air'));
	l(x,y,z) = position;
	l(cx, cy, cz) = center_pos;
	nbttag = nbt('{BlockState:{Name:"minecraft:'+block_name+'"},Time:1}');
	if (block_data, put(nbttag, 'TileEntityData', block_data));
	if (properties_tag, put(nbttag, 'BlockState.Properties', properties_tag));
	falling_block = spawn('falling_block', x+0.5, y+0.5, z+0.5, nbttag);
	modify(falling_block,'motion',
		(x-cx)/10+rand(0.05),
		2*ttl/10+0.5+rand(0.2)+(y-cy)/10,
		(z-cz)/10+rand(0.05)
	);
	if (!rand(3), particle('explosion',position+rand(1)));
	if (!rand(3), sound('entity.dragon_fireball.explode',position, 0.2+rand(0.1), 0.4+rand(0.3)));
	penalty = 0.5+rand(1);
	for (rect(x,y,z),
		if(!__is_invalid_for_smashing(_) && (dist = __distance(pos(_), position)) > 0,
			schedule(
				2+ceil(rand(4*dist*dist)), 
				'__cascade_smash', 
				center_pos, 
				ttl-penalty*dist, 
				pos(_) )
		)
	)
)


