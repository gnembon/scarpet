// will likely break your worlds, automatically trying to mess up configs of existing features.
global_experimental_features = false; 
// when true, outputs failed assertions for the snapshot feature content jsons, otherwise only logs it.
global_verbose = false;

// end of togglable features
__config() -> 
{
   'scope' -> 'global',
   'commands' -> {
      'list' -> _() -> for(filter(system_info('world_dimensions'), is_rift(_)),
         [x, foo, z] = name_to_center(_);
         print(format('w '+slice(_, 5)+': ', 'wb ['+x+',~,'+z+']', '!/tp '+x+' ~ '+z)); 
      ),
      'explain' -> ['explain', true, null, null],
      'explain <pos>' -> _(p) -> explain(true, p, null),
      'explain <pos> <word>' -> _(p, key) -> explain(true, p, key),
      'locate' -> 'locate',
      'chance' -> _() -> print(global_rarity),
      'chance <chance>' -> _(prob) -> (print('Rift spawn chance set to '+prob); global_rarity = prob; save_settings()),
      'create' -> 'create',
      'tpcommands <bool>' -> _(opt) -> (print('Vanilla teleport commands enabled: '+opt); global_tps = opt; save_settings()),
   },
   'arguments' -> {
      'chance' -> { 'type' -> 'float', 'min' -> 0, 'max' -> 1, 'suggest' -> [0.05, 0.15, 0.5] },
   }
};

global_version = '1.1';

global_rarity = 0.15;

global_dx = 1+0.01/euler;
global_dz = 1+0.01/pi;

global_tps = false;

settings = load_app_data();
if (settings,
   global_rarity = settings:'Chance';
   global_tps = settings:'Teleports';
);

save_settings() ->
(
   settings = nbt('{}');
   settings:'Chance' = global_rarity;
   settings:'Teleports' = global_tps;
   store_app_data(settings);
);

vanilla_data(path) -> read_file('vanilla_worldgen_'+system_info('game_major_target')+'.zip/'+path, 'shared_json');
vanilla_options(path) -> 
(
   full_path = 'vanilla_worldgen_'+system_info('game_major_target')+'.zip/'+path;
   offset = length(full_path)+1;
   sort(map(list_files(full_path, 'shared_json'), slice(_,offset)))
);

global_has_wg_data = bool(vanilla_options('dimension'));

explain(command, pos, dimkey) ->
(
   if (!pos, 
      pos = if (current_dimension()~'rift:',
         name_to_center(current_dimension())
      ,  current_dimension() == 'overworld', 
         pos(player());
      )
   );
   
   if( pos,
      portal_location = in_dimension('overworld', find_portal_around(pos, false));
      if (portal_location,
         previous_dimkey = in_dimension('overworld', get_custom_seed(portal_location));
         if (previous_dimkey, dimkey = previous_dimkey);
         [data, explain] = create_dimension_data(portal_location, dimkey);
         if (command, 
            print('');
            print('Rifts ver. '+global_version);
            if (current_dimension() == 'overworld', 
               print('Rift features around '+portal_location+':'),
               print('Current rift features:')
            );
            for (explain, if (type(_)=='list', for (_, print(' '+_)), print(' '+_)));
            particle('end_rod', portal_location+[0.5, 1.5, 0.5], 200, 0.2, 0.2);
         ,
            feature = rand(explain);
            if (type(feature)=='list', feature = rand(feature));
            emit(pos, feature);
         );
         return();
      )
   );
   if (command, print('Nothing to report in here'), emit(pos, 'Nothing'));
);

create() ->
(
   p = player();
   if (!p, exit());
   ppos = map(pos(p), floor(_));
   if ( current_is_rift(),
      if (abs(ppos:0) > 14 || abs(ppos:2) > 14,
         exit('You are too far from the the central portal location')
      )
   , current_dimension() != 'overworld',
      exit('Restoring of rifts only works in the overworld and in rifts dimensions')
   );
   set_poi(ppos, null);
   set(ppos, 'end_gateway');
   set_poi(ppos, 'unemployed');
   print(p, 'Restored rift portal at '+ppos);
);

emit(pos, message) -> 
(
   bat = create_marker(message, pos+[0.5, 1.5, 0.5]);
   rx = rand(1)/10-0.05;
   rz = rand(1)/10-0.05;   
   entity_event(bat, 'on_tick', _(e, outer(rx), outer(rz)) -> modify(e, 'move', [rx+rand(1)/5-0.1, 0.02, rz+rand(1)/5-0.1]));
   schedule(60, _(outer(bat)) -> modify(bat, 'remove'));
);

__on_chunk_generated(x, z) -> if( current_dimension() == 'overworld' && should_generate_at_coords(x, z),
   generate_portal_for_chunk(x, z);
);

should_generate_at_coords(x, z) ->
(
   chx = floor(x/16);
   chz = floor(z/16);
   !(chx%2) && !(chz%2) && (simplex(global_dx*chx, global_dz*chz, 0, system_info('world_seed'))-1) > -global_rarity
);


generate_portal_for_chunk(x, z) ->
(
   center = [x+8, top('terrain', x+8,0, z+8), z+8];
   plop_altair(center, center, !altair_is_aquatic(center), true);  
);

move_entities(position, range, intensity, radial) ->
(
  for( filter( entity_area('*',position, 8, 8, 8), _~'gamemode_id' != 3 ),
      [dx,dy,dz] = pos(_)-position;
      distsq = dx^2+dz^2;
      modify(_,'accelerate', 
          (-radial*dz - (0.5+0.5*intensity)*(dx))/(distsq+1), 
          intensity/sqrt(distsq+1),// + 0.1*(1-intensity)*(-dy + 0.1), 
          ( radial*dx - (0.5+0.5*intensity)*(dz))/(distsq+1)   
      )
  )
);

global_splashing_sounds = reduce(filter(sound(), _~'splash$'), (_a+=_)+='block.water.ambient', []);

drill_vortex(center) ->
(
   protected_blocks = [altair_protected_blocks(center)];
   assert_area_generated(center, 2);
   seed = get_world_seed(center)+69420;
   reset_seed(seed);
   start_angle = 360*rand(1, seed);
   loop(20,
      ray = center;
      start_angle += rand(40, seed);
      angle = start_angle;
      loop(20, 
         carver = ( _(outer(_), outer(ray), outer(protected_blocks)) -> (
            ices = {'ice', 'frosted_ice', 'packed_ice'};
            pb = protected_blocks:0;
            size = 20-_;
            move_entities(ray, 8, 0.086, 0.3);
            if(!rand(3), schedule(rand(6), _(outer(ray)) -> sound(rand(global_splashing_sounds), ray, 1.0, 0.5)) );
            for(diamond(ray, size/8, size/2),
               if(!has(pb, pos(_)) && (liquid(_) || has(ices, str(_))), 
                  particle('warped_spore',_, 5, 1, 0);
                  without_updates(set(_, 'air'))
               )
            );
         ));
         schedule(30+rand(8, seed)+3*_, carver);
         ray = ray + [sin(angle), 0, cos(angle)];
         angle += rand(10, seed);
      );
   );
   // stop kelp around to mess your vortex
   scan(center,20,center:1,20,20,0,20,
      if (_ == 'kelp', without_updates(set(_, 'kelp[age=25]')) )
   );
   
);

global_breaking_sounds = filter(sound(), _~'block\..+\.break');

drill_crevasse(center) ->
(
   protected_blocks = [altair_protected_blocks(center)];
   assert_area_generated(center, 1);
   seed = get_world_seed(center)+69420;
   reset_seed(seed);
   start_angle = 360*rand(1, seed);
   depth = (center:1+if(system_info('game_major_target')<17,20,20+64))/24;
   loop(2,
      ray = center;
      start_angle += 140+rand(80, seed);
      angle = start_angle;
      loop(24,
         carver = ( _(outer(_), outer(ray), outer(seed), outer(depth), outer(protected_blocks)) -> (
            pb = protected_blocks:0;
            size = 24-_;
            move_entities(ray, 8, 0.6, 0);
            loop(10, schedule(rand(4), _(outer(ray)) -> sound(rand(global_breaking_sounds), ray, 1.0, 0.5)));
            for(diamond(ray, size/4, depth*size+rand(10, seed)), 
               if(!has(pb, pos(_)), 
                  if (!rand(20), particle('block '+_, _, 2, 0.5 ));
                  without_updates(set(_, 'air'));
                  
               );
            );
         ));
         schedule(40+rand(8, seed)+2*_, carver);
         ray = ray + [sin(angle), 0, cos(angle)];
         angle += rand(10, seed)-5;
      );
   );
);

// load chunks
assert_area_generated(center, r) -> for(range(-r,r+1), cx = _; for(range(-r,r+1), cz = _; air(center+16*[cx, 0, dz]) ) );

global_altair_palette = map([
      //      roof slab               side_pedestal             side pillar                     side endcap stairs          corner slab                    pillar bottom                    pillar top
      ['smooth_quartz_slab',       'emerald_block',         'gold_block',                  'smooth_quartz_stairs',       'smooth_quartz_slab',            'end_rod',                       'end_rod'                 ],
      ['polished_andesite_slab',   'quartz_bricks',         'quartz_pillar',               'polished_diorite_stairs',    'quartz_slab',                   'diorite_wall',                  'andesite_wall'           ],
      ['dark_prismarine_slab',     'prismarine_bricks',     'prismarine',                  'dark_prismarine_stairs',     'warped_slab',                   'prismarine_wall',               'warped_fence'            ],
      ['end_stone_brick_slab',     'purpur_block',          'purpur_pillar',               'purpur_stairs',              'purpur_slab',                   'end_stone_brick_wall',          'end_stone_brick_wall'    ],
      ['mossy_cobblestone_slab',   'mossy_stone_bricks',    'mossy_cobblestone',           'mossy_stone_brick_stairs',   'mossy_stone_brick_slab',        'mossy_stone_brick_wall',        'mossy_cobblestone_wall'  ],
      ['cobblestone_slab',         'cobblestone',           'cobblestone',                 'cobblestone_stairs',         'cobblestone_slab',              'cobblestone_wall',              'cobblestone_wall'        ],
      
      ['brick_slab',               'polished_andesite',     'andesite',                    'polished_andesite_stairs',   'smooth_stone_slab',             'oak_fence',                     'oak_fence'               ],
      ['nether_brick_slab',        'red_nether_bricks',     'chiseled_nether_bricks',      'red_nether_brick_stairs',    'red_nether_brick_slab',         'red_nether_brick_wall',         'nether_brick_fence'      ],
      ['polished_blackstone_slab', 'gilded_blackstone',     'chiseled_polished_blackstone','blackstone_stairs',          'polished_blackstone_brick_slab','polished_blackstone_brick_wall','polished_blackstone_wall'],
      ['acacia_slab',              'red_sandstone',         'chiseled_red_sandstone',      'smooth_red_sandstone_stairs','cut_red_sandstone_slab',        'red_sandstone_wall',            'acacia_fence'            ],
      ['birch_slab',               'sandstone',             'chiseled_sandstone',          'smooth_sandstone_stairs',    'cut_sandstone_slab',            'sandstone_wall',                'birch_fence'             ],
      ['dark_oak_slab',            'stripped_dark_oak_wood','spruce_wood',                 'dark_oak_stairs',            'spruce_slab',                   'spruce_fence',                  'dark_oak_fence'          ],
      //['','','','','','',''],
   ], 
   {   'roof_slab' -> _:0,         'side_pedestal' -> _:1,  'side_pillar' -> _:2,          'side_endcap_stairs' -> _:3,  'corner_slab' -> _:4,            'corner_bottom_pillar' -> _:5,   'corner_top_pillar' ->_:6 }
);

altair_protected_blocks(center) -> 
(
   protected_blocks = {};
   for (range(-7, 5), protected_blocks += pos_offset(center, 'up', _));
   for(['north', 'south', 'east', 'west'], side = _;
      for([0, -1, -2, 3],
         protected_blocks += pos_offset(center, side)+[0, _, 0]
      )
   );
   for([[-1,0,-1], [1,0,-1], [-1,0,1], [1,0,1]], side = _;
      loop( 4,
         protected_blocks += pos_offset(center+side, 'up', _)
      )
   );
   protected_blocks
);

altair_is_aquatic(center) -> all([center, center-7, center+7, center-[7, 0, -7], center+[-7, 0, 7]], biome(_, 'category')=='ocean');

plop_altair(center, seed_pos, guardian, full) ->
(
   seed = get_world_seed(seed_pos)+69420;
   reset_seed(seed);
   palette = rand(global_altair_palette, seed);
   if (full,
      set(center, 'end_gateway');
      set_poi(center, 'unemployed');
      if (guardian && !rand(5, seed), spawn('villager', center+[0.9, 1.5, 0.9]));
      set(center-[0,1,0], 'bedrock');
      set(center-[0,2,0], 'polished_basalt');
      set(center-[0,3,0], 'polished_basalt');
   );
   set(center-[0,4,0], 'polished_blackstone_brick_wall');
   set(center-[0,5,0], 'chain');
   set(center-[0,6,0], 'chain');
   set(center-[0,7,0], 'lantern[hanging=true]');
   if(full,
      for([1,2,3], set(center+[0,_,0], 'air'));
      set(center+[0,4,0], palette:'roof_slab');
   );
   for(['north', 'south', 'east', 'west'],
      if(full,
         set(pos_offset(center, _)-[0, 0, 0], palette:'side_pedestal');
         set(pos_offset(center, _)-[0, 1, 0], palette:'side_pillar');
         set(pos_offset(center, _)+[0,3,0], palette:'roof_slab'+'[type=top]');
      );
      set(pos_offset(center, _,-1)-[0, 2, 0], palette:'side_endcap_stairs'+'[facing='+_+',half=top]');
   );
   if (full,
      for([[-1,0,-1], [1,0,-1], [-1,0,1], [1,0,1]],
         set(center+_+[0,0,0], palette:'corner_slab'+'[type=top]');
         set(center+_+[0,1,0], palette:'corner_bottom_pillar');
         set(center+_+[0,2,0], palette:'corner_top_pillar');
         set(center+_+[0,3,0], palette:'roof_slab');
      );
   );
);

add_vines(center) -> 
(
   seed = get_world_seed(center)+69420;
   reset_seed(seed);
   for(['north', 'south', 'east', 'west'],
      side = _;
      loop(4+rand(8, seed),
         pos = pos_offset(center, side, -2)-[0, _, 0];
         if(air(pos), set(pos, 'vine['+side+'=true]'));
      )
   )
);

add_ladders(center) -> for(['north', 'south', 'east', 'west'],
   side = _;
   loop(2,
      pos = pos_offset(center, side, 2)-[0, _, 0];
      if (air(pos), set(pos, 'ladder[facing='+side+']'));
   )
);

// debug utility
read_datapack(path) ->
(
   output = {};
   for (list_files(path, 'shared_folder'), output:slice(_, length(path)+1) = read_datapack(_));
   for (list_files(path, 'shared_json'), output:(slice(_, length(path)+1)+'.json') = read_file(_, 'shared_json'));
   for (list_files(path, 'shared_nbt'), output:(slice(_, length(path)+1)+'.nbt') = read_file(_, 'shared_nbt'));
   output;
);

locate() ->
(
   p = player();
   if (!p, exit('locate can only be triggered by players'));
   if (current_dimension() != 'overworld', 
      exit('Wrong dimension, use /rifts locate only in the overworld')
   );
   center = pos(p);
   ckcenter = map(center/16, floor(_));
   found = 0;
   print(p, 'Closest rift locations:');
   for( diamond(ckcenter, 125, 0),
      [x, yy, z] = (pos(_)*16)+8;
      if (should_generate_at_coords(x, z),
         print(p, format('wb ['+x+',~,'+z+']', '!/tp '+x+' ~ '+z));
         draw_shape('line', 100, 'from', center, 'to', [x, center:1, z], 'line', 5, 'color', 255/(found+1));
         found+=1;
         if (found > 5, break());
      );
   )
);

global_transportals = [];

global_creation_mutex = true;



__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
   if (block == 'end_gateway' && hand == 'mainhand' && poi(block):0 == 'unemployed',
      dimkey = parse_nbt(item_tuple:2:'display.Name'):'text';
      if( item_tuple:0 == 'emerald' && current_dimension() == 'overworld',
      (
         center = pos(block);
         assert_portal_not_used(center);
         first_run = dimension_not_created(center);
         if (!global_creation_mutex,
            sound('entity.villager.no', center);
            exit();
         );
         if(player~'gamemode_id'%2,
            modify(player, 'swing')
         , // else
            inventory_set(player, player~'selected_slot', item_tuple:1-1),
         );
         
         if (first_run, // this will lock it for a second so would be better to do that before we start animating
            global_creation_mutex = false;
            schedule(110, _() -> global_creation_mutex = true);
            if (dimkey, set_custom_seed(center, dimkey));
            create_dimension(center, dimkey);

            if(altair_is_aquatic(center),
               drill_vortex(center);
               schedule(100, 'add_vines', center);
            ,
               drill_crevasse(center);
               schedule(100, 'add_ladders', center);
            );
            schedule(99, 'plop_altair', center, center, false, false);
         );
         global_transportals += [center, current_dimension(), 0];
      )
      ,  item_tuple:0 == 'gold_nugget' && current_dimension() == 'overworld',
      (
         if(player~'gamemode_id'%2==0, 
            inventory_set(player, player~'selected_slot', item_tuple:1-1),
            modify(player, 'swing')
         );
         explain(false, pos(block), dimkey);
      )
      ,  current_is_rift() ,
      (
         assert_portal_not_used(pos(block));
         modify(player, 'swing');
         global_transportals += [pos(block), current_dimension(), 0];
      ));
   )
);

set_custom_seed(center, dimkey) ->
(
   seedstore = pos_offset(center,'down',2);
   set(seedstore, 'chest');
   tag = nbt('{}');
   tag:'seed' = dimkey;
   inventory_set(seedstore, 13, 1, 'paper', tag);
);

get_custom_seed(center) ->
(
    seedstore = pos_offset(center,'down',2);
    if (block(seedstore) == 'chest',
        inventory_get(seedstore, 13):2:'seed';
    )
);

assert_portal_not_used(center) -> if (first(global_transportals, _:0 == center && _:1 == current_dimension()),
   sound('entity.villager.no', center);
   exit()
);

__on_tick() -> if (global_transportals,
   global_transportals = filter(map(global_transportals, 
      [pos, dim, ttl] = _;
      in_dimension(dim,
         if (ttl > 100 || !loaded(pos), null,
            animate(pos, ttl);
            if (ttl == 100,
               teleport_entites_with_portal(pos);
            );
            [pos, dim, ttl+1];
         )
      )
   ), _);
);

animate(pos, ttl) ->
(
   particle(if(rand(100)<ttl,'nautilus','end_rod'), pos+[0.5, rand(3), 0.5], 20, 0.5, 0.5/sqrt(ttl));
   if (ttl == 0,
      sound('block.portal.trigger', pos, 1, 0.77, 'block');
   );
   if (ttl == 55,
      particle('portal', pos+[0.5, 1.5, 0.5], 2500, 0, 2);
   );
   if (ttl == 100,
      particle('explosion', pos+[0.5, 1.5, 0.5], 100, 1, 0);
      sound('entity.generic.explode', pos, 1, 0.5, 'block');
   );
);

dimension_not_created(center) ->
(
   new_dim = get_world_name(center);
   dim_key = 'rift:'+new_dim;
   system_info('world_dimensions')~dim_key == null;
);

get_teleport(from, dim, to) -> str('execute as @e[x=%.1f,y=%.1f,z=%.1f,distance=..1.7] in %s run tp @s %d %d %d', from:0+0.5, from:1+1.0, from:2+0.5, dim, to:0, to:1, to:2);

teleport_entites_with_portal(pos) ->
(
   from = current_dimension();
   if (from == 'overworld',
      new_dim = get_world_name(pos);
      dim_key = 'rift:'+new_dim;
      in_dimension(dim_key,
         portal_location = find_portal_around([0, 64, 0], true);
      );
      run(get_teleport(pos, dim_key, portal_location));
      in_dimension(dim_key,
         particle('explosion', portal_location, 100, 1, 0);
         sound('entity.generic.explode', portal_location, 1, 0.5, 'block');
      );
      
   ,  is_rift(from) ,
      world_pos = name_to_center(from);
      if (world_pos, in_dimension('overworld', 
         world_pos:1 = top('terrain', world_pos)-3;
         portal_location = find_portal_around(world_pos, true); 
         in_dimension(from, run(get_teleport(pos, 'overworld', portal_location)));
         particle('explosion', portal_location+[0.5, 1.5, 0.5], 100, 1, 0);
         sound('entity.generic.explode', portal_location, 1, 0.5, 'block');
      ));
   );
);

name_to_center(dim) ->
(
   parse = dim~'rift:dim([pn])(\\d+)([pn])(\\d+)';
   if(!parse || length(parse)!=4, return(null));
   [sx, x, sz, z] = parse;
   x = number(x);
   z = number(z);
   if (sx == 'n', x = -x);
   if (sz == 'n', z = -z);
   16*[x, 4, z]+8;
);

find_portal_around(center, force) ->
(
   possible_portals = poi(center, 16, 'unemployed', 'any', true);
   if (possible_portals,
      possible_portals:0:2,
      if (force, [center:0, top('terrain', center), center:2 ])
   );
);

chunk_coords_from_pos(pos) -> [ floor(pos:0/16), floor(pos:2/16) ];

get_world_seed(pos) ->
(
   [chx, chz] = chunk_coords_from_pos(pos);
   system_info('world_seed')+1304011369*chx+2782775717*chz
);

get_world_name(pos) ->
(
   [chx, chz] = chunk_coords_from_pos(pos);
   'dim'+if(chx>0,'p','n')+abs(chx)+if(chz>0,'p','n')+abs(chz);
);

vanilla_id(name) -> if (name~':', name, 'minecraft:'+name);
vanilla_noid(id) -> if (slice(id, 0, 10)=='minecraft:', slice(id, 10), id);
is_rift(id) -> ( slice(id, 0, 5) == 'rift:' );
current_is_rift() -> is_rift(current_dimension());

create_dimension(pos, dimkey) ->
(
   name = get_world_name(pos);
   [pack_data, explain] = create_dimension_data(pos, dimkey);
   rift_scale = values(pack_data:'data':'rift':'dimension'):0:'rift_scale';
   particle('happy_villager', pos+[0,1,0], 20, 1, 0); // indicator before we lag
   sound('entity.bee.pollinate', pos, 1, 0.5);
   create_datapack(name,pack_data);
   enable_hidden_dimensions();
   in_dimension('rift:'+name,
      location = find_central_altair_position(rift_scale:0+5, rift_scale:1-5);
      plop_altair(location, pos, false, true);
      if(global_tps, set(pos_offset(location, 'down'), 'command_block[facing=up]{Command:"'+get_teleport(location, 'overworld', pos)+'"}'));
   );
   if(global_tps, set(pos_offset(pos, 'down'), 'command_block[facing=up]{Command:"'+get_teleport(pos, 'rift:'+name, location)+'"}'));
);

valid_spawn(at) ->
(
   below = pos_offset(at, 'down');
   above = pos_offset(at, 'up');
   (solid(below) && block(below)!='bedrock' && !solid(at) && !liquid(at) && !solid(above) && !liquid(above) ) ||
   (liquid(below) && air(above) && air(above))
);

find_central_altair_position(from, to) ->
(
   for(range(to, from, -1),
      cand = [0, _, 0];
      if ( _ > from && _ < to && valid_spawn(cand), return(cand));
   );
   for(range(to, from, -1),
      if ( _ > from && _ < to, for(diamond([0,_,0], 12, 0),
         if (valid_spawn(_), return (pos(_)));
      ))
   );
   [0, 80, 0];
);

create_dimension_data(pos, dimkey) ->
(
   name = get_world_name(pos);
   seed = if (dimkey, if(str(dimkey)==str(number(dimkey)), number(dimkey), hash_code(dimkey)) ,get_world_seed(pos));
   global_has_wg_data = bool(vanilla_options('dimension'));
   [base_type_data, data_id] = random_by_weight(global_world_types, salt(seed,1514098039));
   explain = [base_type_data:'lore', str(seed)];
   data = call(base_type_data:'base', name, seed);
   for (base_type_data:'modifiers', explain += call(_, data, salt(seed,947867849)) );
   quirk_seed = salt(seed, 192641249);
   if (base_type_data:'quirk_count',
      quirks = copy(base_type_data:'quirks');
      qcount = base_type_data:'min_quirk_count' + floor(rand(base_type_data:'quirk_count'-base_type_data:'min_quirk_count', quirk_seed));
      qorder = {};
      loop(qcount,
         [quirk, qid] = random_by_weight(quirks, quirk_seed);
         exp = call(quirk:'op', data, salt(seed, qorder:(quirk:'salt')*947867849+quirk:'salt'));
         qorder:(quirk:'salt') += 1;
         if(exp != null, 
            explain += exp;
            // used up quirk
            if (quirk:'single_use', delete(quirks, qid));
         );
      );
   );
   [data, explain];
);

random_by_weight(objects, seed) ->
(
   if (!objects, return(null));
   total = reduce(objects, _a+_:'weight', 0);
   r = rand(total, seed);
   choice = floor(r);
   current_weight = 0;
   current_id = 0;
   result = null;
   while( !result, length(objects),
      current_weight += objects:current_id:'weight';
      if (choice < current_weight, result = objects:current_id, current_id += 1);
   );
   [result, current_id];
);

explain_multinoise(selection) ->
(
   biome_stats = {};
   params = keys(selection:0:'parameters');
   biome_data = map(selection, [_:'biome', el = _; map(params, el:'parameters':_)]);
   loop(1000,
      sample = map(params, rand(2)-1);
      distances = map(biome_data, [_:0, reduce(_:1 - sample, _a+_*_, 0)]);
      max_dist = distances:0:1;
      closest = distances:0:0;
      biome_stats:reduce(distances, if(_:1 < max_dist, max_dist = _:1; _:0 ,_a), closest) += 1;
   );
   map(biome_stats, titlecase(vanilla_noid(_))+': '+round(biome_stats:_/10)+'% Among '+length(biome_stats));
);

titlecase(text) -> title(replace(text, '_', ' '));
from_rgb(color) -> [floor(color/256/256), floor((color % (256*256))/256), floor(color % 256)];


funkify_params(data, seed) -> 
(
// EXPERIMENTAL
// rewrite to change chech component level accorting to the types etc.
   changed = false;
   for (sort(keys(data)),
      value = data:_;
      if ( type(value) == 'map',
         [nd, chd] = funkify_params(value, seed);
         if (chd,
            data:_ = nd;
            changed = true;
         )
      , type(value) == 'number',
         if (_ == 'chance', // prob or 1-100
            data:_ = if(value <=1, 
               changed = true;
               rand(1, seed)
            , value > 10,
               changed = true;
                exp = rand(6, seed)-3;
                max(10, round(value*(2^exp)));
            , value
            );
         , floor(value)==value && _ ~ '[count|reach|tries|weight|size]', // int
            //max_allowed = round(2^ceil(ln(value)));
            exp = rand(6, seed)-3;
            data:_ = min(127, max(1, round(value*(2^exp))));
            changed = true;
         , abs(value) < 1,
            exp = rand(6, seed)-3;
            data:_ = max(0, min(1, value*2^exp));
            changed = true;
         )
      , type(value) == 'bool',
         data:_ = bool(rand(2, seed));
         if (data:_ != value,
            changed = true;
         )
      )
   );
   [data, changed];
);

global_config_commons = {
   'random_world_type' -> _(data, seed) -> (
      seed = salt(seed, 3183259837);
      values(data:'data':'rift':'dimension'):0:'type' = if (global_has_wg_data,
         wtype = rand(vanilla_options('dimension_type'), seed);
         //wtype = 'overworld';
         type_data = vanilla_data('dimension_type/'+wtype);
         type_data:'has_ceiling' = false;
         type_data;
      ,
         wtype = rand(['overworld', 'overworld_caves', 'the_end', 'the_nether'], seed);
         vanilla_id(wtype);
      );
      titlecase(wtype)+' Type';
   ),
   'random_generator_settings' -> _(exclude) -> _(outer(exclude), data, seed) -> (
      extra = '';
      dim = values(data:'data':'rift':'dimension'):0;
      dim:'rift_scale' = [0, 256]; // default
      seed = salt(seed, 491231297);
      dim:'generator':'settings' = if(global_has_wg_data,
         options = filter(vanilla_options('worldgen/noise_settings'), !has(exclude, _));
         generator = rand(options, seed);
         //generator = 'amplified';
         gen_data = vanilla_data('worldgen/noise_settings/'+generator);
         
         if (system_info('game_major_target') < 17,
            gen_data:'noise':'height' = 256;
            dim:'type':'logical_height' == 256;
            gen_data:'bedrock_roof_position' = -10;// cause I don't like it
         ,
            bottom = -16*floor(rand(9, seed));
            top = 256+16*floor(rand(25, seed));
            extra = 'From '+bottom+' To '+top;
            height = top - bottom;
            gen_data:'noise':'min_y' = bottom;
            gen_data:'noise':'height' = height;
            dim:'type':'min_y' = bottom;
            dim:'type':'height' = height;
            dim:'rift_scale' = [bottom, top];
            dim:'type':'logical_height' = height;
            gen_data:'bedrock_roof_position' = -2147483648;// cause I don't like it
         );
         if (generator == 'caves' || generator == 'nether', // igloo by-pass
            gen_data:'noise':'height' += -16; // making sure there is place for a bed from broken igloos
            dim:'type':'logical_height' += -16;
         );
         gen_data
      ,  // else strings only
         if (system_info('game_major_target') < 17, //  'nether', 'caves', // cause they can cause igloos to spawn above build limit
            generator = rand(['amplified',  'end', 'floating_islands', 'overworld'], seed)
         ,
            // in 1.17 have to match
            wtype = vanilla_noid(dim:'type');
            gen_opts = if(
               wtype == 'the_end' , ['end'], 
               wtype=='the_nether', [if (has(exclude, 'nether'),'end','nether')], 
               ['amplified', if(has(exclude,'caves'),'floating_islands', 'caves'), 'floating_islands', 'overworld']
            );
            if(length(gen_opts)>1, dim:'rift_scale' = [-64, 320]);
            generator = rand(gen_opts, seed);
         );
         vanilla_id(generator)
      );
      if (extra, [extra, titlecase(generator)+' Layout'], titlecase(generator)+' Layout');
   ),
   'frequent_structures' -> _(data, seed) -> (
      seed = salt(seed, 1413917753);
      structure_map = values(data:'data':'rift':'dimension'):0:'generator':'settings':'structures':'structures';
      value = rand(keys(structure_map), seed);
      data = structure_map:value;
      if (data:'spacing' > 2,
         scale = 1 + rand(5, seed);
         if (!rand(10, seed), scale = 1000);
         data:'separation' = floor(data:'separation'/scale);
         data:'spacing' = floor(data:'spacing'/scale);
         // mainly due to lag
         minsep = if (vanilla_noid(value) == 'village', 3, 1);
         if(data:'separation' < minsep , data:'separation' = minsep);
         if (data:'separation' >= data:'spacing', data:'spacing' = data:'separation'+1);
         titlecase(vanilla_noid(value))+' '+if(scale==1000, 'A Lot', ''+round(10*scale)/10+ ' Times')+' More Frequent';
      );
   ),
   'random_default_block' -> _(data, seed) -> (
      seed = salt(seed, 3663866621);
      gen = values(data:'data':'rift':'dimension'):0:'generator':'settings';
      value = __random_block_data(seed, 10);
      // so it doesn't lag the client that much
      if (value:'Name' == 'minecraft:slime_block', value:'Name' = 'minecraft:emerald_block');
      gen:'default_block' = value;
      titlecase(vanilla_noid(value:'Name'));
   )
};

global_banned_blocks = {'loom', 'fletching_table', 'composter', 'blast_furnace', 'smoker', 'barrel', 
'cartography_table', 'cauldron', 'smithing_table', 'beehive', 'bee_nest', 'lodestone', 
'sand', 'red_sand', 'tnt', 'spawner'};

// for testing of blocks that cannot be tested for solidness // only shulkerboxes so far
test_blocks() -> for(block_list(), print(player(), _); !(has(global_banned_blocks, _)) && !(_~'shulker') && solid(block(_)));


_valid_normal_block(b) -> (!(has(global_banned_blocks, b)) && !(b~'shulker') && b != 'gravel' && !(b ~'concrete_powder$') && solid(block(b)) );

//shortcuts

__mod_setting(weight, field, salt, value_function) ->{
   'weight' -> weight,
   'salt' -> salt,
   'single_use' -> true,
   'op' -> _(data, seed, outer(field), outer(value_function)) -> (
      value = call(value_function, seed);
      biome_data = values(data:'data':'rift':'worldgen':'biome'):0;
      if (has(biome_data, field), biome_data:field = value);
      if (has(biome_data:'effects', field), biome_data:'effects':field = value);
      dim_data = values(data:'data':'rift':'dimension'):0:'type';
      if (has(dim_data, field), dim_data:field = value);
      gen_data = values(data:'data':'rift':'dimension'):0:'generator':'settings';
      if (has(gen_data, field), gen_data:field = value);
      titlecase(field)+' '+if(field~'color', 'RGB'+from_rgb(value), type(value)=='number' && round(value) != value, round(100*value)/100, value);
   )
};
        
global_rare_blocks = {'diamond_block', 'diamond_ore', 'gold_block', 'gold_ore', 
'iron_block', 'iron_ore', 'emerald_block', 'emerald_ore', 'lapis_block', 'lapis_ore',
'netherite_block', 'wet_sponge', 'shulker_box'};
 

// chances for rare blocks depending on tries
// /script in rifts run map(range(20), ch = _+1; reduce(range(100000), _a+has(global_rare_blocks, vanilla_noid( __random_block_data(_, ch):'Name')), 0))
// 1 -> 70% 2 -> 50% 3 -> 36% 4 -> 26% 5 -> 19.5%  6-> 14.5% 7-> 11.6% 8-> 9.4% 9-> 7.9% 10-> 6.8%
// 11 -> 6.1% 15 -> 4.8% 20 -> 4.4%
__random_block_data(seed, tries) ->
(
   while(value = rand(block_list(), seed); !_valid_normal_block(value), tries, null);
   if (!_valid_normal_block(value), value = rand(keys(global_rare_blocks), seed));
   block_entry = { 'Name' -> vanilla_id(value) };
   default_state = block_state(value);
   for (default_state, default_state:_ = str(default_state:_)); // stringify values
   if (default_state, block_entry:'Properties' = default_state);
   block_entry
);

assert_compatibility(feature, ... fields) -> loop(length(fields)/2,
   if (fields:(2*_) != fields:(2*_+1), throw(feature+', '+fields:(2*_+1)+' is not present'))
);


// when adding new custom feature, make sure to use the vanilla world features as a base
// and scramble it.
// for snapshots, assert the value you replace to ensure compatibility. 
// Features that do not have expected fields will be ignored from world generation
// all add to 1000
// preserving that makes sure seeds don't change between adding different extra features.
global_custom_features = [
   {'weight' -> 300, 'salt' -> 4021098647, 'gen' -> _(gen, seed, major) -> (
      data = vanilla_data('worldgen/configured_feature/forest_rock') || throw('Freckles');
      rblock = __random_block_data(seed, 10);
      if (major > 16,
         assert_compatibility('Freckles',
            data:'config':'feature':'config':'decorator':'config':'inner':'config':'heightmap', 'MOTION_BLOCKING',
            data:'config':'feature':'config':'feature':'config':'state':'Name', 'minecraft:mossy_cobblestone',
         );
         data:'config':'feature':'config':'decorator':'config':'inner':'config':'heightmap' = 'OCEAN_FLOOR'
      );
      
      data:'config':'feature':'config':'feature':'config':'state' = rblock;
      b = floor(2^rand(3, seed));
      s = floor(2^rand(6, seed));
      cdata = data:'config':'decorator':'config':'count';
      if (major > 16,
         assert_compatibility('Freckles',
            cdata:'value':'min_inclusive', 0,
            cdata:'value':'max_inclusive', 2,
         );
         cdata:'value':'min_inclusive' = b;
         cdata:'value':'max_inclusive' = b+s;
      ,
         cdata:'base' = b;
         cdata:'spread' = s;
      );
      
      [data, 'Freckles of '+titlecase(vanilla_noid(rblock:'Name'))];
   )},
   {'weight' -> 100, 'salt' -> 4174618733, 'gen' -> _(gen, seed, major) -> (
      data = vanilla_data('worldgen/configured_feature/basalt_blobs') || throw('Diamonds');
      rblock = __random_block_data(seed, 10);
      inner = data:'config':'feature':'config':'feature':'config':'feature':'config';
      if (major > 16, assert_compatibility('Diamonds',
            inner:'state':'Name', 'minecraft:basalt',
            inner:'target':'Name', 'minecraft:netherrack',
            inner:'radius':'value':'min_inclusive', 3,
            inner:'radius':'value':'max_inclusive', 7,
            data:'config':'decorator':'config':'count', 75 ,
      ));
      inner:'state' = rblock;
      inner:'target' = {'Name' -> 'minecraft:air'};
      b = 2;
      s = floor(2^rand(3, seed));
      if (major > 16,
         inner:'radius':'value':'min_inclusive' = b;
         inner:'radius':'value':'max_inclusive' = b+s;
      ,
         inner:'radius':'base' = b;
         inner:'radius':'spread' = s;
      );
      if (major == 16, data:'config':'feature':'config':'feature':'config':'decorator':'config':'maximum' = 256);
      data:'config':'decorator':'config':'count'= floor(2^rand(2, seed));
      [data, 'Diamonds of '+titlecase(vanilla_noid(rblock:'Name'))];
   )},
   {'weight' -> 300, 'salt' -> 274534769, 'gen' -> _(gen, seed, major) -> (
      data = vanilla_data('worldgen/configured_feature/crimson_fungi') || throw('Columns');
      rblock = __random_block_data(seed, 10);
      surface_block = values(gen:'data':'rift':'worldgen':'biome'):0:'surface_builder':'config':'top_material';
      count = 1 + floor(2^rand(3, seed));
      shroom = data:'config':'feature':'config';
      assert_compatibility('Columns',
         data:'config':'decorator':'config':'count', 8,
         shroom:'valid_base_block':'Name', 'minecraft:crimson_nylium',
         shroom:'hat_state':'Name', 'minecraft:nether_wart_block',
         shroom:'stem_state':'Name', 'minecraft:crimson_stem',
         shroom:'decor_state':'Name', 'minecraft:shroomlight',
      );
      data:'config':'decorator':'config':'count' = count;
      shroom:'valid_base_block' = surface_block;
      shroom:'hat_state':'Name' = 'minecraft:air';
      stem = __random_block_data(seed, 10);
      shroom:'stem_state' = stem;
      
      if (!rand(4, seed),
         decor = __random_block_data(seed, 3);
         shroom:'decor_state' = decor;
      ,
         shroom:'decor_state':'Name' = 'minecraft:air';
      );
      [data, 'Columns of '+titlecase(vanilla_noid(stem:'Name')) + if(decor,' with '+titlecase(vanilla_noid(decor:'Name')), '']);
   )},
   {'weight' -> 300, 'salt' -> 463650907, 'gen' -> _(gen, seed, major) -> (
      data = vanilla_data('worldgen/configured_feature/'+if(major > 16,'prototype_ore_coal_upper','ore_coal')) || throw('Blob');
      rblock = __random_block_data(seed, 10);
      inner = data:'config':'feature':'config':'feature':'config':'feature':'config';
      if (major > 16, assert_compatibility('Blob',
         inner:'targets':0:'target':'tag', 'minecraft:stone_ore_replaceables',
         inner:'targets':0:'target':'predicate_type', 'minecraft:tag_match',
         inner:'targets':0:'state':'Name', 'minecraft:coal_ore',
         inner:'size', 17,
         data:'config':'feature':'config':'feature':'config':'decorator':'config':'height':'min_inclusive':'absolute', 136,
         data:'config':'decorator':'config':'count', 30,
      ));
      if(major > 16,inner:'targets':0:'target',inner:'target') = {
          'block' -> 'minecraft:air',
          'predicate_type' -> 'minecraft:block_match'
      };
      if(major > 16,inner:'targets':0:'state',inner:'state') = rblock;
      inner:'size' = floor(2^rand(6, seed));
      if (major > 16,
         data:'config':'feature':'config':'feature':'config':'decorator':'config':'min_inclusive' = {
            'above_bottom' -> 0
         }
      ,
         data:'config':'feature':'config':'feature':'config':'decorator':'config':'maximum' = 256;
      );
      if (major > 16,
         data:'config':'decorator':'config':'count' =10+floor(2^rand(4, seed));
      ,
         data:'config':'decorator':'config':'count' = {'base' -> 2, 'spread' -> floor(2^rand(3, seed))}
      );
      [data, 'Blobs of '+titlecase(vanilla_noid(rblock:'Name'))];
   )},
];

global_fid = 0;

salt(base_seed, amount) ->
(
    reset_seed(seed = base_seed+amount*5370548977925065747); // shifting
    rand(1, seed); // scrambling
    seed
);
// add to 100 without wg files, and 200 with wg files.
// quirks add to 1000
// quirks may add to something else, which is fine, since random weight selection will preserve the random
global_world_types = [
   {  
      'lore' -> 'Single biome world', // maybe limit generator settings to exclude caves, end and nether
      'weight' -> 20,
      'base' -> _(name, seed) -> {
         'data' -> { 'rift' -> { 'dimension' -> { name+'.json' -> {
            'type' -> 'minecraft:overworld',
            'generator' -> {
               'type' -> 'minecraft:noise',
               'seed' -> seed,
               'biome_source' -> {
                  'type' -> 'minecraft:fixed',
                  'seed' -> seed,
                  'biome' -> 'minecraft:plains'
               },
               'settings' -> 'minecraft:overworld',
            }
         }}}}
      },
      'modifiers' -> [
         global_config_commons:'random_world_type',
         call(global_config_commons:'random_generator_settings', {}),
         _(data, seed) -> (
            biome = rand(filter(sort(biome()), !is_rift(_)), salt(seed, 3377526371));
            values(data:'data':'rift':'dimension'):0:'generator':'biome_source':'biome' = vanilla_id(biome);
            titlecase(biome);
         )
      ],
      'quirk_count' -> 6, // upper bound
      'min_quirk_count' -> 0, // lower bound
      'quirks' -> [ // 1000 total
         // type quirks
         __mod_setting(120, 'ambient_light', 2281543507, _(seed) -> rand(0.5, seed)),
         __mod_setting(120, 'ultrawarm', 698692859, _(seed) -> rand([true, false], seed)),
         // gen settings quirks
         __mod_setting(120, 'sea_level', 2749567357, _(seed) -> floor(rand(128, seed))),
         
         // increase chances for structures // may not have effect if structure won't spawn
         {'weight' -> 400, 'salt' -> 4201505393, 'single_use' -> false, 'op' -> global_config_commons:'frequent_structures'},
         {'weight' -> 240, 'salt' -> 4288127323, 'single_use' -> true, 'op' -> global_config_commons:'random_default_block'},
      ]
   },
   {
      'lore' -> 'Checkerboard', // simple stuff - looks decent so far
      'weight' -> 20,
      'base' -> _(name, seed) -> {
         'data' -> { 'rift' -> { 'dimension' -> { name+'.json' -> {
            'type' -> 'minecraft:overworld',
            'generator' -> {
               'biome_source' -> {
                  'seed' -> seed,
                  'type' -> 'minecraft:checkerboard',
                  'biomes' -> [],
                  'scale' -> 1,
               },
               'seed' -> seed,
               'settings' -> 'minecraft:overworld',
               'type' -> 'minecraft:noise'
            }
         }}}}
      },
      'modifiers' -> [
         global_config_commons:'random_world_type',
         call(global_config_commons:'random_generator_settings', {}),
         _(data, seed) -> (
            seed = salt(seed, 716687603);
            biomes = filter(sort(biome()), !is_rift(_));
            count = floor(2+rand(10, seed)*rand(10, seed)*rand(10, seed)/100);
            selection = map(range(count), rand(biomes, seed));
            values(data:'data':'rift':'dimension'):0:'generator':'biome_source':'biomes' = map(selection, vanilla_id(_));
            map(selection,titlecase(_)+', one of '+count);
         ),
         _(data, seed) -> (
            seed = salt(seed, 3364034519);
            scale = floor(rand(4, seed));
            values(data:'data':'rift':'dimension'):0:'generator':'biome_source':'scale' = scale;
            (2^scale)+' Chunks Checker Size';
         ),
      ],
      'quirk_count' -> 8, // upper bound
      'min_quirk_count' -> 0, // lower bound
      'quirks' -> [ // 1000 total
         // type quirks
         __mod_setting(120, 'ambient_light', 41744821,  _(seed) -> rand(0.5, seed)),
         __mod_setting(120, 'ultrawarm', 3911651509, _(seed) -> rand([true, false], seed)),
         // gen settings quirks
         __mod_setting(120, 'sea_level', 1485487669, _(seed) -> floor(rand(128, seed))),
         
         // increase chances for structures // may not have effect if structure won't spawn
         {'weight' -> 400, 'salt' -> 2261218079, 'single_use' -> false, 'op' -> global_config_commons:'frequent_structures'},
         {'weight' -> 240, 'salt' -> 2479136227, 'single_use' -> true, 'op' -> global_config_commons:'random_default_block'},
      ]
   },
   {
      'lore' -> 'Multi Noise', // Very nice results especially with very low scale.
      'weight' -> 60,
      'base' -> _(name, seed) -> {
         'data' -> { 'rift' -> { 'dimension' -> { name+'.json' -> {
            'type' -> 'minecraft:overworld',
            'generator' -> {
               'biome_source' -> {
                  'seed' -> seed,
                  'type' -> 'minecraft:multi_noise',
                  'humidity_noise' -> { 'firstOctave' -> -7, 'amplitudes' -> [ 1.0, 1.0 ] },
                  'altitude_noise' -> { 'firstOctave' -> -7, 'amplitudes' -> [ 1.0, 1.0 ] },
                  'weirdness_noise' -> { 'firstOctave' -> -7, 'amplitudes' -> [ 1.0, 1.0 ] },
                  'temperature_noise' -> { 'firstOctave' -> -7, 'amplitudes' -> [ 1.0, 1.0 ] },
                  'biomes' -> [ tbd ]
               },
               'seed' -> seed,
               'settings' -> 'minecraft:overworld',
               'type' -> 'minecraft:noise'
            }
         }}}}
      },
      'modifiers' -> [
         global_config_commons:'random_world_type',
         call(global_config_commons:'random_generator_settings', {}),
         _(data, seed) -> (
            seed = salt(seed, 967651673);
            biome_size = 1+floor(rand(7, seed));// 1 - super small biomes, 7 - original vanilla size
            source = values(data:'data':'rift':'dimension'):0:'generator':'biome_source';
            for (filter(keys(source), _~'_noise'), source:_:'firstOctave' = -biome_size);
            'Biome Scale '+biome_size+'/7';
         ),
         _(data, seed) -> (
            seed = salt(seed, 4294597297);
            biomes = filter(sort(biome()), !is_rift(_));
            count = floor(5+rand(16, seed)); //5-20 biomes
            selection = map(range(count), rand(biomes, seed));
            biome_data = map(selection, {
               'parameters' -> { // doing (-1,1), not(-2, 2) to have more balanced 
                  'altitude' -> rand(2,seed)-1, 
                  'weirdness' -> rand(2,seed)-1,
                  'temperature' -> rand(2,seed)-1,
                  'humidity' -> rand(2,seed)-1,
                  'offset' -> rand(1, seed)
               },
               'biome' -> vanilla_id(_)
            });
            values(data:'data':'rift':'dimension'):0:'generator':'biome_source':'biomes' = biome_data;
            explain_multinoise(biome_data);
         ),
         
      ],
      'quirk_count' -> 8, // upper bound
      'min_quirk_count' -> 0, // lower bound
      'quirks' -> [ // 1000 total
         // type quirks
         __mod_setting(40, 'ambient_light', 3056598049, _(seed) -> rand(0.5, seed)),
         __mod_setting(40, 'ultrawarm', 141833633, _(seed) -> rand([true, false], seed)),
         // gen settings quirks
         __mod_setting(210, 'sea_level', 1012719889, _(seed) -> floor(rand(128, seed))),
         
         // increase chances for structures // may not have effect if structure won't spawn
         {'weight' -> 500, 'salt' -> 2168633813, 'single_use' -> false, 'op' -> global_config_commons:'frequent_structures'},
         {'weight' -> 210, 'salt' ->3858771079, 'single_use' -> true, 'op' -> global_config_commons:'random_default_block'},
      ]
   },
   {
      'lore' -> 'Funky biome',
      'weight' -> if (global_has_wg_data, 100, 0), // only if worldgen data is present and 1.16
      'base' -> _(name, seed) -> {
         'data' -> { 'rift' -> { 'dimension' -> { name+'.json' -> {
            'type' -> 'minecraft:overworld',
            'rift' -> name,
            'generator' -> {
               'type' -> 'minecraft:noise',
               'seed' -> seed,
               'biome_source' -> {
                  'type' -> 'minecraft:fixed',
                  'seed' -> seed,
                  'biome' -> 'minecraft:plains'
               },
               'settings' -> 'minecraft:overworld',
            }
         }}}}
      },
      'modifiers' -> [
         global_config_commons:'random_world_type',
         call(global_config_commons:'random_generator_settings', {'nether','caves'}),
         _(data, seed) -> (
            seed = salt(seed, 3248756387);
            biome = rand(vanilla_options('worldgen/biome'), seed);
            dim = values(data:'data':'rift':'dimension'):0;
            name = dim:'rift';
            dim:'generator':'biome_source':'biome' = 'rift:'+name;
            biome_data = vanilla_data('worldgen/biome/'+biome);
            biome_data:'surface_builder' = vanilla_data('worldgen/configured_surface_builder/'+vanilla_noid(biome_data:'surface_builder'));
            data:'data':'rift':'worldgen' = { 'biome' -> { name+'.json' -> biome_data }};
            titlecase(biome);
         ),
      ],
      'quirk_count' -> 25, // upper bound
      'min_quirk_count' -> 15, // lower bound
      'quirks' -> [ // whatever total - just make sure not to change the totals
         // type quirks
         __mod_setting(30, 'ambient_light', 3984616477, _(seed) -> rand(0.5, seed)),
         __mod_setting(30, 'ultrawarm', 376299761, _(seed) -> rand([true, false], seed)),
         // gen settings quirks
         __mod_setting(60, 'sea_level', 2549333291, _(seed) -> floor(rand(128, seed))),
         
         // increase chances for structures // may not have effect if structure won't spawn
         {'weight' -> 100, 'salt' -> 3923374541, 'single_use' -> false, 'op' -> global_config_commons:'frequent_structures'},
         {'weight' -> 60, 'salt' -> 2309673617, 'single_use' -> true, 'op' -> global_config_commons:'random_default_block'},
         // biome quirks
         {'weight' -> 60, 'salt' -> 3092630759, 'single_use' -> true, 'op' -> _(data, seed) -> ( // custom underblock
            surface = values(data:'data':'rift':'worldgen':'biome'):0:'surface_builder':'config';
            value = __random_block_data(seed, 10);
            surface:'under_material' = value;
            titlecase(vanilla_noid(value:'Name'));
         )},
         {'weight' -> 60, 'salt' -> 2061670003, 'single_use' -> true, 'op' -> _(data, seed) -> ( // custom water block
            surface = values(data:'data':'rift':'worldgen':'biome'):0:'surface_builder':'config';
            value = __random_block_data(seed, 10);
            surface:'underwater_material' = value;
            titlecase(vanilla_noid(value:'Name'))+' Under Water';
         )},
         __mod_setting(40, 'scale', 3719444363, _(seed) -> if(rand([true, false], seed), rand(2.0, seed), -0.11+rand(0.05, seed))),
         __mod_setting(40, 'depth', 115834853, _(seed) -> (rand(2, seed)-1)),
         __mod_setting(40, 'water_color', 2487248693, _(seed) -> floor(rand(256*256*256, seed))),
         __mod_setting(40, 'water_fog_color', 668136061, _(seed) -> floor(rand(256*256*256, seed))),
         __mod_setting(40, 'sky_color', 3091033151, _(seed) -> floor(rand(256*256*256, seed))),
         __mod_setting(40, 'fog_color', 3695161069, _(seed) -> floor(rand(256*256*256, seed))),
         __mod_setting(40, 'foliage_color', 2273525869, _(seed) -> floor(rand(256*256*256, seed))),
         __mod_setting(40, 'grass_color', 850509467, _(seed) -> floor(rand(256*256*256, seed))),
         // potentially add particles to effects (see soul_sand_valley)
         // ptentially grass_color_modifier (swamp or dark_forest)

         {'weight' -> 150, 'salt' ->2237345459, 'single_use' -> false, 'op' -> _(data, seed) -> (  //  - these typically don't match, so trying more of it.
            features = values(data:'data':'rift':'worldgen':'biome'):0:'features';
            if (!features, return(null));
            stage_id = floor(rand(length(features), seed));
            new_name = rand(vanilla_options('worldgen/configured_feature'), seed);
            features:stage_id += vanilla_id(new_name);
            'Chances of '+titlecase(new_name);
         )},
         // either replace / or funkify
         {'weight' -> if (global_experimental_features, 0, 100), 'salt' ->3285030817, 'single_use' -> false, 'op' -> _(data, seed) -> (  // replace vanilla feature - acts more like removeal
            features = values(data:'data':'rift':'worldgen':'biome'):0:'features';
            if (!features, return(null));
            stage_id = floor(rand(length(features), seed));
            if (features:stage_id,
               feat_id = floor(rand(length(features:stage_id), seed));
               old = features:stage_id:feat_id;
               old_name = vanilla_noid(old);
               if (old_name!=old, // vanilla feature
                  new_name = rand(vanilla_options('worldgen/configured_feature'), seed);
                  features:stage_id:feat_id = vanilla_id(new_name);
                  titlecase(old_name)+' replaced with '+titlecase(new_name)
         )))},
         {'weight' -> if (global_experimental_features, 100, 0), 'salt' ->862665967, 'single_use' -> false, 'op' -> _(data, seed) -> (  // tweak params of existing feature. // very restricted and can crash
            features = values(data:'data':'rift':'worldgen':'biome'):0:'features';
            if (!features, return(null));
            stage_id = floor(rand(length(features), seed));
            if (features:stage_id,
               feat_id = floor(rand(length(features:stage_id), seed));
               feature = features:stage_id:feat_id;
               feature_name = vanilla_noid(feature);
               if (feature_name!=feature, // vanilla feature
                  feature_data = vanilla_data('worldgen/configured_feature/'+feature_name);
                  [feature_data, changed] = funkify_params(feature_data, seed);
                  if (changed,
                     if (!has(data:'data':'rift':'worldgen':'configured_feature'), 
                        data:'data':'rift':'worldgen':'configured_feature' = {}
                     );
                     data:'data':'rift':'worldgen':'configured_feature':(feature_name+'.json') = feature_data;
                     features:stage_id:feat_id = 'rift:'+feature_name;
                     'Funky '+titlecase(feature_name);
         ))))},
         // add structures // most will not place anyways, so just adding a chance
         {'weight' -> 200, 'salt' ->2046057037, 'single_use' -> false, 'op' -> _(data, seed) -> (
            biome_data = values(data:'data':'rift':'worldgen':'biome'):0;
            value = rand(plop():'configured_structures', seed);
            value_namespaced = vanilla_id(value);
            if (biome_data:'starts' ~ value_namespaced == null,
               biome_data:'starts' += value_namespaced;
               titlecase(value);
            )
         )},
         // add mobs
         {'weight' -> 150, 'salt' ->3370101577, 'single_use' -> false, 'op' -> _(data, seed) -> (
            spawn_data = values(data:'data':'rift':'worldgen':'biome'):0:'spawners';
            spawn_type = rand(['ambient', 'creature', 'monster', 'water_ambient', 'water_creature'], seed);
            current_list = spawn_data:spawn_type;
            count = reduce(current_list, max(_a, _:'weight'), 0) || 100;
            initial = floor(rand(4, seed))+1;
            final = initial+1+floor(rand(6, seed));
            mob = rand(entity_types(spawn_type), seed);
            current_list += {
               'maxCount' -> final,
               'minCount' -> initial,
               'type' -> vanilla_id(mob),
               'weight' -> count
            };
            titlecase(mob);
         )},
         // add custom features- version specific
         {'weight' -> 100, 'salt' ->2907701207, 'single_use' -> false, 'op' -> _(data, seed) -> (
            name = values(data:'data':'rift':'dimension'):0:'rift'+'_'+global_fid;
            global_fid += 1;
            try(
               [feature, fid] = random_by_weight(global_custom_features, seed);
               [feature_data, feature_desc] = call(feature:'gen', data, salt(seed, feature:'salt'), system_info('game_major_target'));
            ,
               error = 'Error generating '+_+'. Seems like the world gen changed for it in this version.';
               if (global_verbose, print(player(), error), logger(error));
               return(null);
            );
            //values(data:'data':'rift':'worldgen':'biome'):0:'features':0 += 'rift:'+name;
            features = values(data:'data':'rift':'worldgen':'biome'):0:'features';
            if (!features, features += []);
            features:(-1) += 'rift:'+name;
            
            if (!has(data:'data':'rift':'worldgen':'configured_feature'), 
               data:'data':'rift':'worldgen':'configured_feature' = {}
            );
            data:'data':'rift':'worldgen':'configured_feature':(name+'.json') = feature_data;
            feature_desc;
         )},
      ]
   }
];