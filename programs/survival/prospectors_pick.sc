
//Expose this option to the players to be able to switch between cave and surface modes with /script invoke';
select_ore_finder(option) -> 
(
  if( option == 'surface',
      (
          __scanned_elevation(player) -> 8;
          __link_player(x, y, z, p, part) -> particle(part, x, top('light',x,z)+1 , z, 10)
          //Showing ores on world surface'
      ),
      option == 'caves', 
      (
          __scanned_elevation(player) -> player ~ 'y';
          __link_player(x, y, z, p, part) -> particle_line(part, pos(p)+l(0,1.2,0), x+0.5, y+0.5, z+0.5, 0.3)
          //Showing ores in caves via beams'
      ),
      'Unsuported option: '+option+' choose \'caves\' or \'surface\''
  )
);
//The default option would be caves
select_ore_finder('caves');

//Handy function to check enchantment level on a tool
//Sample tool return value with /script run p~\'holds\':
//[golden_pickaxe, 1, {id:"minecraft:golden_pickaxe",Count:1b,tag:{Enchantments:[{lvl:3s,id:"minecraft:fortune"},{lvl:1s,id:"minecraft:unbreaking"}],Damage:0}}]
//call with \'_axe\' to match any pickaxe
__check_held_enchantment_level(entity, tool_re, enchantment) -> 
(
  if (entity~'gamemode_id'==3, return(0));
  for(l('main','offhand'),
      holds = query(entity, 'holds', _);
      if( holds,
          l(what, count, nbt) = holds;
          if( (what ~ tool_re) && ( ench = (nbt ~ 'lvl:\\d+s,id:"minecraft:'+enchantment+'"') ),
              lvl = max(lvl, number(ench ~ '(?<=lvl:)\\d') )
          )
      )
  );
  lvl
);

__check_ores_for_player(player, outer(ore_levels)) -> 
(
   lvl = __check_held_enchantment_level(player, 'golden_pickaxe','fortune');
   if(lvl && !rand(5),
       l(x, y, z) = pos(player);
       scan(x, __scanned_elevation(player), z, 8, 8, 8,
           if (!rand(5) && (_ ~ '_ore'),
               current_block = _ ;
               for(get(ore_levels, min(lvl-1, 2)),
                   l(ore, part, thres) = _ ;
                   if ( current_block == ore && for(neighbours(current_block), _ == ore) >= thres,
                       __link_player(_x, _y, _z, player, part)
                   )
               )
           )
       )
   )
);

_ore_finder_tick() -> 
(
   ore_levels = l(
       l( l('coal_ore','dust 0.1 0.1 0.1 0.5', 4), l('iron_ore', 'dust 0.6 0.3 0.1 0.5', 3) ),
       l( l('coal_ore','dust 0.1 0.1 0.1 0.5', 5), l('iron_ore', 'dust 0.6 0.3 0.1 0.5', 3),
          l('gold_ore','dust 0.9 0.9 0.0 0.5', 2), l('redstone_ore', 'dust 0.9 0.1 0.1 0.5', 3) ),
       l( l('coal_ore','dust 0.1 0.1 0.1 0.5', 6), l('iron_ore', 'dust 0.6 0.3 0.1 0.5', 3),
          l('gold_ore','dust 0.9 0.9 0.0 0.5', 2), l('redstone_ore', 'dust 0.9 0.1 0.1 0.5', 3),
          l('nether_quartz_ore','dust 0.9 0.9 0.9 0.5', 3), l('lapis_ore', 'dust 0.1 0.1 1.0 0.5', 0),
          l('diamond_ore','dust 0.3 0.8 1.0 0.5', 0), l('emerald_ore', 'dust 0.4 1.0 0.4 0.5', 0), )
   );
   for(player('*'), __check_ores_for_player(_))   
)