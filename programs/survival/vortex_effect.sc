// Sample tool return value with /script run p~\'holds\':
// [golden_pickaxe, 1, {id:"minecraft:golden_pickaxe",Count:1b,tag:{Enchantments:
//   [{lvl:3s,id:"minecraft:fortune"},{lvl:1s,id:"minecraft:unbreaking"}],Damage:0}}]
// call with \'_axe\' to match any pickaxe
// Handy function to check enchantment level on a tool

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__check_held_enchantment_level(entity, item_regex, enchantment) -> 
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

// spawns random cloud particles and plays random sounds around tornado players
__tornado_ambiance(entity, level) ->
(
  if ( !(rand(5)),
      l(x,y,z) = pos(entity);
      particle('cloud', x+rand(10)-rand(10),y+rand(10)-rand(10),z+rand(10)-rand(10),level*level,2,level/3);
      if ( !(rand(5)),
          sound('item.elytra.flying', x+rand(10)-rand(10),y+rand(10)-rand(10),z+rand(10)-rand(10),rand(0.05*level),0.3+rand(level/2))
      )
  )
);

// moves entities around player, depending on the level of the enchantment
__move_entities(entity, level) ->
(
  eid = entity ~ 'id';
  l(x, y, z) = entity ~ 'pos';
  intensity = (level-1)/2;
  for( filter( entity_area('*',x,y+9,z,20,12,20), eid != _~'id' && _~'gamemode_id' != 3 ),
      l(dx,dy,dz) = pos(_)-l(x,y,z);
      distsq = dx^2+dz^2;
      modify(_,'accelerate', 
          (-intensity*(dz) - (0.5+0.5*intensity)*(dx))/(distsq+1), 
          intensity/sqrt(distsq+1) + 0.1*(1-intensity)*(-dy + 0.1), 
          ( intensity*(dx) - (0.5+0.5*intensity)*(dz))/(distsq+1)   
      )
  )
);

// adjusts level down from 3,4,5 to 1,2,3 and causes tornado
__create_tornado(entity, level) ->
(
  level = min(level-2, 3);
  __tornado_ambiance(entity, level);
  __move_entities(entity, level)
);

// tick routine that works with players holding axes only
_tornado_tick_players_only() -> 
(
  for( player('*'),
      if ((lvl = __check_held_enchantment_level(_, '_axe','sharpness')) > 2,
          __create_tornado(_, lvl)
      )
  )
);

// tick routine that works with any entity holding such an axe. Evaluates all entities each tick
_tornado_tick_entities_precise() -> 
(
  for( entity_list('living'),
      if ((lvl = __check_held_enchantment_level(_, '_axe','sharpness')) > 2,
          __create_tornado(_, lvl)
      )
  )
);

// tick routine that works with any entity holding such an axe. 
// May delay detection by up to 2 seconds, but only searches all entities every 40 ticks
_tornado_tick_entities_robust() -> 
(
  tornado_entities = 'global_tornado_'+current_dimension();
  if(!var(tornado_entities), var(tornado_entities) = l() );
  ids = l();
  for( if ( tick_time() % 40 == 0,
               entity_list('living'),
               filter(map(var(tornado_entities),entity_id(_)), _ )
      ),
      if ( _ && (lvl = __check_held_enchantment_level(_, '_axe','sharpness')) > 2,
          __create_tornado(_, lvl);
          ids += _ ~ 'id'
      )  
  );
  var(tornado_entities) = ids;
  length(ids)
)
