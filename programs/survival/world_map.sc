
// world_map app by gnembon
// requires carpet 1.4.20

__command() -> ' 
use:
 - eat your berry
 or
 - /world_map tp
';

__config() -> {
	//'stay_loaded' -> true,
	'scope' -> 'global'
};

global_map_view_distance = 0;

// whether use actual world structure data, not seed based - much smaller even on generated chunks
global_use_actual_structure_data = false;

// tracks players that are no maps
global_player_chunk_map = {};

// use threads to seamlessly generate chunk data
// slower than synced but no lag spikes
global_parallel = true;

// generation or degeneration
global_generate_forward = true;

// output timing and progress information
global_debug = false;


// some common decorations
global_decorations = {
   'cacti' -> [
      [0.95, 1, 'cactus', 'cactus', 'tripwire'],
      [0.9,  1, 'cactus', 'tripwire'],
   ],
   'camp' -> [
      [0.98, 1, 'campfire']
   ],
   'bush' -> [
      [0.8,  1, 'dead_bush']
   ],
   'bamboo' -> [
      [0.7, 1, 'podzol', 'bamboo[leaves=none]', 'bamboo[leaves=small]', 'bamboo[leaves=small]', 
            'bamboo[leaves=large]','tripwire'],
      [0.7, 1, 'podzol', 'bamboo[leaves=none]', 'bamboo[leaves=small]', 'bamboo[leaves=small]', 'tripwire'],
      [0.7, 0, 'podzol', 'bamboo[leaves=none]', 'bamboo[leaves=small]', 'tripwire'],
   ]
};

__decorate(decor_list, extras) ->
(
   decor = [];
   for(decor_list, for(global_decorations:_, decor+=_));
   for(decor_list, for(extras, decor+=_));
   decor
);

__basic_biome_template(block, replaces, decorations) -> 
(
   biome = {
      'block' -> block,
      'replaces' -> replaces,
   };
   if (decorations, put(biome, 'decoration', decorations));
   biome
);
__simple_biome(block, decorations) -> __basic_biome_template(block, {}, decorations);
__grassy_biome(decorations) -> __basic_biome_template('grass_block', {'dirt'}, decorations);
__leaf(x) -> x+'_leaves[persistent=true]';


global_features = {
   'spring_lava_double' -> [0.90, 0, 'lava'],
   'brown_mushroom_nether' -> [0.95, 1, 'brown_mushroom'],
   'red_mushroom_nether' -> [0.95, 1, 'red_mushroom'],
};

__create_custom_biome(biome) ->
(
   scale =  biome(biome, 'scale'); 
   cat = biome(biome, 'category');
   if (scale > 0.25,
      if (scale > 0.4,
         cat += '_mountains'
      ,
         cat += '_hills'
      )
   );
   if (biome(biome, 'depth') < -0.2, cat = 'deep_ocean');
   if (global_debug, print('Biome '+biome+' is modded, used '+cat+' as a base'));
   
   config = copy(global_biome_data:cat);
   previous_main_block = config:'block';
   main_block = str(biome(biome, 'top_material'));
   under_block = str(biome(biome, 'under_material'));
   if (!has(config, 'decoration'), config:'decoration' = []);
   config:'block' = main_block;
   config:'replaces' += under_block;
   if (previous_main_block != main_block,
      for(config:'decoration', decor = _;
         for (range(2, length(decor)),
            if(decor:_ == previous_main_block, decor:_ = main_block)
   )));
   
   put(config:'decoration':0, [0.8, 0, under_block], 'insert');
   //put(config:'decoration':0, [0.95, 1, main_block], 'insert');
   
   //if (scale > 0.25,
   //   put(config:'decoration':0, [0.8, 1, main_block], 'insert');
   //   if (scale > 0.4, put(config:'decoration':0, [0.8, 1, under_block, main_block], 'insert'))
   //);
   
   for (biome(biome, 'features'), for(filter(_, has(global_features:_)),
      put(config:'decoration':0, global_features:_, 'insert');
   ));
   
   global_biome_data:biome = config;
   config
);

// all biomes information

global_biome_data = {

   // nether biomes
   
   'nether_wastes' -> __simple_biome('netherrack', [
         [0.85, 0, 'nether_gold_ore'],
         [0.85, 0, 'nether_quartz_ore'],
         [0.95, 1, 'fire'],
   ]),
   'crimson_forest' -> {
      'block' -> 'crimson_nylium',
      'replaces' -> {'netherrack'},
      'decoration' -> [
         [0.85, 1,  'crimson_stem','crimson_stem', 'nether_wart_block'],
         [0.70, 1, 'crimson_fence', 'nether_wart_block'], 
         [0.85, 1, 'crimson_fungus'],
         [0.7, 1, 'crimson_roots'],
         [0.90, 1, 'shroomlight'],
      ]
   },
   'warped_forest' -> {
      'block' -> 'warped_nylium',
      'replaces' -> {'netherrack'},
      'decoration' -> [
         [0.85, 1,  'warped_stem','warped_stem', 'warped_wart_block'],
         [0.70, 1, 'warped_fence', 'warped_wart_block'],
         [0.85, 1, 'warped_fungus'],
         [0.7, 1, 'warped_roots'],
         [0.7, 1, 'nether_sprouts'],
         [0.94, 1, 'shroomlight'],
      ]
   },
   'soul_sand_valley' -> __simple_biome('soul_sand', [
         [0.94, 1, 'bone_block', 'bone_block', 'bone_block[axis=z]'],
         [0.94, 1, 'bone_block', 'bone_block', 'bone_block[axis=x]'],
         [0.90, 1, 'bone_block', 'bone_block'],
         [0.94, 1, 'bone_block'],
         [0.6, 0, 'soul_soil'],
         [0.9, 1, 'soul_fire'],
   ]),
   'basalt_deltas' -> __simple_biome('blackstone',
      [
         [0.9, 1, 'basalt', 'basalt', 'basalt'],
         [0.7, 1, 'basalt', 'basalt'],
         [0.7, 1, 'basalt'],
         [0.7, 0, 'magma_block'],
         [0.7, 0, 'lava']
      ]
   ),
   
   //end biomes
   
   'the_end' -> __simple_biome('black_stained_glass', null),
   
   'small_end_islands' -> {
      'block' -> 'black_stained_glass',
      'replaces' -> {'end_stone_brick_wall'},
      'decoration' -> [
         [0.6, 0, 'end_stone_brick_wall']
      ]
   },
   'end_barrens' -> __simple_biome('end_stone_brick_slab', null),
   'end_midlands' -> __simple_biome('end_stone', [
         [0.9, 1, 'end_stone_brick_slab']
   ]),
   'end_highlands' -> __simple_biome('end_stone', [
         [0.95, 1, 'end_stone', 'chorus_plant[down=true,up=true]', 
            'chorus_plant[down=true,up=true]', 'chorus_flower[age=5]'],
         [0.9, 1, 'end_stone', 'chorus_plant[down=true,up=true]', 'chorus_flower[age=5]'],
         [0.8, 1,'chorus_plant[down=true,up=true]', 'chorus_plant[down=true,up=true]', 'chorus_flower[age=5]'],
         [0.7, 1, 'chorus_plant[down=true,up=true]', 'chorus_flower[age=5]'],
         [0.8, 1, 'end_stone']
   ]),
   
   // overworld biomes
   
   'river' -> __simple_biome('lapis_block', null),
   'frozen_river'-> __simple_biome('packed_ice', null),
   'beach'-> __simple_biome('sand', null),
   'snowy_beach' -> __simple_biome('sand', [
         [0.1, 1, 'snow']
   ]),
   'stone_shore' -> __simple_biome('stone', null),
   'frozen_ocean' -> __simple_biome('blue_stained_glass', [
         [0.9, 1, 'packed_ice', 'snow_block'],
         [0.8, 1, 'blue_ice', 'snow'],
         [0.7, 1, 'packed_ice'],
      ]
   ),
   'cold_ocean' -> __simple_biome('blue_stained_glass', null),
   'ocean' -> __simple_biome('cyan_stained_glass', null),
   'lukewarm_ocean' -> __simple_biome('light_blue_stained_glass', null),
   'warm_ocean' -> {
      'block' -> 'light_blue_stained_glass',
      'replaces' -> {'fire_coral','horn_coral','tube_coral','brain_coral','bubble_coral','sea_pickle','water'},
      'decoration' -> [
         [0.9, -1, 'smooth_sandstone', 'sea_pickle[pickles=4,waterlogged=true]'],
         [0.7, -1, 'fire_coral_block', 'fire_coral[waterlogged=true]'],
         [0.7, -1, 'horn_coral_block', 'horn_coral[waterlogged=true]'],
         [0.7, -1, 'tube_coral_block', 'tube_coral[waterlogged=true]'],
         [0.7, -1, 'brain_coral_block', 'brain_coral[waterlogged=true]'],
         [0.7, -1, 'bubble_coral_block', 'bubble_coral[waterlogged=true]'],
      ]
   },
   'desert' -> {
      'block' -> 'sand',
      'replaces' -> {'sandstone'},
      'decoration' -> __decorate( ['cacti', 'camp', 'bush'], [
         [ 0.8, 0, 'sandstone']
      ])
   },
   'desert_lakes' -> {
      'block' -> 'sand',
      'replaces' -> {'water'},
      'decoration' -> __decorate( ['bush'], [
         [0.8, 0, 'sandstone'],
         [0.5, 0, 'water'],
      ])
   },
   'desert_hills' -> __simple_biome('sand', [
         [0.7, 1, 'sandstone'], 
         [0.7, 1, 'sandstone_slab']
   ]),
   'plains' -> __grassy_biome(
      __decorate( ['camp'], [
         [0.9, 1, 'poppy'],
         [0.9, 1, 'dandelion'],
         [0.93, 1, 'cornflower'],
         [0.7, 1, 'grass'],
      ])
   ),
   'mushroom_fields' -> {
      'block' -> 'mycelium',
      'replaces' -> {'dirt'},
      'decoration' -> [
         [0.8, 1, 'mushroom_stem', 'red_mushroom_block'],
         [0.8, 1, 'mushroom_stem', 'brown_mushroom_block'],
      ]
   },
   'mushroom_field_shore' -> {
      'block' -> 'mycelium',
      'replaces' -> {'dirt'},
      'decoration' -> [
         [0.7, 1, 'red_mushroom'],
         [0.7, 1, 'brown_mushroom'],
      ]
   },
   'sunflower_plains' -> __grassy_biome( [
         [0.5, 1, 'sunflower[half=lower]', 'sunflower[half=upper]'],
   ]),
   'flower_forest' -> __grassy_biome( [
         [0.6, 1, 'red_tulip'],
         [0.7, 1, 'orange_tulip'],
         [0.8, 1, 'pink_tulip'],
         [0.8, 1, 'allium'],
   ]),
   'swamp' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt', 'stone_slab'},
      'decoration' -> __decorate( ['camp'], [
         [0.8, 1, 'oak_log', __leaf('oak')],
         [0.8, 1, 'blue_orchid'],
         [0.8, 0, 'grass_block', 'grass'],
         [0.6, 0, 'stone_slab[waterlogged=true]', 'lily_pad'],
      ])
   },
   'swamp_hills' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt', 'stone_slab'},
      'decoration' -> [
         [0.8, 1, 'dirt', 'oak_log', __leaf('oak')],
         [0.8, 1, 'grass_block', 'blue_orchid'],
         [0.8, 1, 'grass_block', 'grass'],
         [0.6, 0, 'stone_slab[waterlogged=true]', 'lily_pad'],
      ]
   },
   'snowy_tundra' -> {
      'block' -> 'snow_block',
      'replaces' -> {'dirt'},
      'decoration' -> __decorate( ['camp'], [
         [0.9335, 1, 'spruce_log', __leaf('spruce')],
         [0.85, 0, 'grass_block', 'grass'],
      ])
   },
   'snowy_mountains' -> __simple_biome('snow_block', [
         [0.7, 1, 'snow_block'], 
         [0.7, 1, 'snow[layers=5]']
   ]),
   'snowy_taiga' -> {
      'block' -> 'snow_block',
      'replaces' -> {'dirt'},
      'decoration' -> __decorate( ['camp'], [
         [0.6, 1, 'spruce_log', __leaf('spruce')],
      ])
   },
   'ice_spikes' -> {
      'block' -> 'snow_block',
      'replaces' -> {'dirt'},
      'decoration' -> [
         [0.90, 1, 'packed_ice', 'packed_ice', 'packed_ice'],
         [0.8, 1, 'packed_ice', 'packed_ice'],
      ]
   },
   'forest' -> __grassy_biome(
      __decorate( ['camp'], [
         [0.8, 1, 'oak_log', __leaf('oak')],
         [0.8, 1, 'birch_log', __leaf('oak')],
      ])
   ),
   'dark_forest' -> __grassy_biome(
      __decorate( ['camp'], [
         [0.9, 1, 'mushroom_stem', 'mushroom_stem', 'red_mushroom_block'],
         [0.9, 1, 'mushroom_stem', 'mushroom_stem', 'brown_mushroom_block'],
         [0.4, 1, 'dark_oak_log', __leaf('dark_oak')],
      ])
   ),
   'dark_forest_hills' -> __grassy_biome( [
         [0.6, 1, 'dirt', 'dark_oak_log', __leaf('dark_oak')],
         [0.9, 1, 'mushroom_stem', 'mushroom_stem', 'red_mushroom_block'],
         [0.9, 1, 'mushroom_stem', 'mushroom_stem', 'brown_mushroom_block'],
         [0.4, 1, 'dark_oak_log', __leaf('dark_oak')],
   ]),
   'jungle' -> __grassy_biome(
       __decorate( ['camp'], [
         [0.8, 1, 'jungle_log', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 1, 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 1, 'melon'],
         [0.4, 1, __leaf('oak')],
      ])
   ),
   'bamboo_jungle' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt', 'podzol'},
      'decoration' -> __decorate( ['bamboo'], [
         [0.9, 0, 'dirt', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.9, 1, 'jungle_log', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.9, 1, 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 1, 'melon'],
         [0.4, 1, __leaf('oak')],
      ])
   },
   'bamboo_jungle_hills' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt', 'podzol'},
      'decoration' -> __decorate( ['bamboo'], [
         [0.9, 0, 'dirt', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.9, 1, 'jungle_log', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.9, 1, 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 1, 'grass_block'],
         [0.4, 1, __leaf('oak')],
      ])
   },
   'jungle_hills' -> __grassy_biome( [
         [0.8, 1, 'dirt', 'jungle_log', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 1, 'dirt', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.6, 1, 'dirt', 'grass_block'],
         [0.5, 1, 'grass_block'],
         [0.4, 1, __leaf('oak')],
   ]),
   'modified_jungle' -> __grassy_biome( [
         [0.8, 1, 'dirt', 'jungle_log', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 1, 'dirt', 'jungle_log', 'jungle_log', __leaf('jungle')],
         [0.8, 4, 'dirt', 'jungle_log', __leaf('jungle')],
         [0.8, 3, 'dirt', __leaf('jungle')],
         [0.7, 2, 'dirt', __leaf('jungle')],
         [0.6, 1, 'dirt', 'grass_block'],
         [0.5, 1, 'grass_block'],
         [0.4, 1, __leaf('oak')],
   ]),
   'mountains' -> __simple_biome('stone', [
         [0.98, 1, 'stone', 'snow_block','campfire'],
         [0.7, 1, 'stone', 'grass_block', 'snow'],
         [0.6, 1, 'stone', 'snow[layers=5]'],
         [0.6, 1, 'grass_block', 'grass'],
         [0.3, 1, 'stone'],
   ]),
   'wooded_mountains' -> __simple_biome('stone', [
         [0.7, 1, 'stone', 'grass_block', 'snow'],
         [0.8, 1, 'dirt', 'spruce_log', __leaf('spruce')],
         [0.6, 1, 'grass_block', 'grass'],
         [0.3, 1, 'stone'],
   ]),
   'gravelly_mountains' -> __simple_biome('stone', [
         [0.7, 1, 'stone', 'grass_block', 'snow'],
         [0.6, 1, 'stone', 'snow[layers=5]'],
         [0.2, 1, 'gravel'],
   ]),
   'modified_gravelly_mountains' -> __simple_biome('stone', [
         [0.7, 1, 'stone', 'grass_block', 'snow'],
         [0.5, 1, 'gravel', 'gravel'],
         [0.6, 1, 'stone', 'snow[layers=5]'],
         [0.2, 1, 'gravel'],
   ]),
   'wooded_hills' -> __grassy_biome( [
         [0.98, 1, 'grass_block','campfire'],
         [0.5, 1, 'grass_block'],
         [0.8, 1, 'dirt', 'oak_log', __leaf('oak')],
         [0.8, 1, 'dirt', 'birch_log', __leaf('oak')],
   ]),
   'birch_forest' -> __grassy_biome( [
         [0.70, 1, 'birch_log', __leaf('birch')],
   ]),
   'birch_forest_hills' -> __grassy_biome( [
         [0.85, 1, 'dirt', 'birch_log', __leaf('birch')],
         [0.75, 1, 'birch_log', __leaf('birch')],
         [0.3, 1, 'grass_block' ]
   ]),
   'taiga' -> __grassy_biome(
      __decorate( ['camp'], [
         [0.70, 1, 'spruce_log', __leaf('spruce')],
         [0.90, 1, 'sweet_berry_bush[age=3]'],
   ])),
   'giant_spruce_taiga' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt','podzol'},
      'decoration' -> [
         [0.85, 1, 'spruce_log', __leaf('spruce'), __leaf('spruce'), __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'spruce_log', __leaf('spruce'), __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'spruce_log', 'spruce_log', 'spruce_log', __leaf('spruce')],
         [0.8, 1, 'fern'],
         [0.7, 1, 'mossy_cobblestone'],
         [0.4, 0, 'podzol']
      ]
   },
   'giant_spruce_taiga_hills' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt','podzol'},
      'decoration' -> [
         [0.85, 1, 'dirt', 'spruce_log', __leaf('spruce'), __leaf('spruce'), __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'dirt', 'spruce_log', __leaf('spruce'), __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'dirt', 'spruce_log', 'spruce_log', 'spruce_log', __leaf('spruce')],
         [0.8, 1, 'fern' ],
         [0.7, 1, 'mossy_cobblestone'],
         [0.4, 1, 'podzol']
      ]
   },
   'giant_tree_taiga' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt','podzol'},
      'decoration' -> [
         [0.85, 1, 'spruce_log', 'spruce_log', 'spruce_log', 'spruce_log', __leaf('spruce')],
         [0.8, 1, 'spruce_log', 'spruce_log', __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'spruce_log', 'spruce_log', __leaf('spruce')],
         [0.8, 1, 'fern' ],
         [0.7, 1, 'mossy_cobblestone'],
         [0.4, 0, 'podzol' ]
      ]
   },
   'giant_tree_taiga_hills' -> {
      'block' -> 'grass_block',
      'replaces' -> {'dirt','podzol'},
      'decoration' -> [
         [0.85, 1, 'spruce_log', 'spruce_log', 'spruce_log', __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'spruce_log', 'spruce_log', __leaf('spruce'), __leaf('spruce')],
         [0.8, 1, 'spruce_log', 'spruce_log', 'spruce_log', __leaf('spruce')],
         [0.7, 1, 'dirt', 'grass_block'],
         [0.8, 1, 'fern' ],
         [0.7, 1, 'mossy_cobblestone' ],
         [0.5, 1, 'podzol' ] 
      ]
   },
   'taiga_hills' -> __grassy_biome( [
         [0.70, 1, 'dirt', 'spruce_log', __leaf('spruce')],
         [0.85, 1, 'spruce_log', __leaf('spruce')],
         [0.3, 1, 'grass_block' ]
   ]),
   'taiga_mountains' -> __grassy_biome([
         [0.70, 1, 'dirt', 'spruce_log', __leaf('spruce')],
         [0.7, 1, 'dirt', 'grass_block' ],
         [0.85, 1, 'spruce_log', __leaf('spruce')],
         [0.3, 1, 'grass_block' ]
   ]),
   'snowy_taiga_hills' -> __simple_biome('snow_block', [
         [0.70, 1, 'snow_block', 'spruce_log', __leaf('spruce')],
         [0.85, 1, 'spruce_log', __leaf('spruce')],
         [0.3, 1, 'snow_block' ]
   ]),
   'savanna' -> __grassy_biome(
      __decorate( ['camp'], [
         [0.75, 1, 'acacia_log', __leaf('acacia')],
         [0.6, 1, 'grass'],
      ])
   ),
   'savanna_plateau' -> __grassy_biome( [
         [0.8, 1, 'dirt', 'acacia_log', __leaf('acacia')],
         [0.5, 1, 'grass_block', 'grass'],
         [0.5, 1, 'grass_block'],

   ]),
   'shattered_savanna_plateau' -> __grassy_biome( [
         [0.7, 1, 'dirt', 'grass_block', 'air', 'air', 'stone', 'stone', 'dirt', 'grass_block'],
         [0.7, 1, 'dirt', 'grass_block', 'air', 'air', 'air', 'dirt', 'grass_block'],
         [0.7, 1, 'dirt', 'grass_block', 'air', 'air', 'air', 'grass_block'],
         [0.8, 1, 'dirt', 'dirt', 'air', 'air', 'air', 'grass_block', 'acacia_log',__leaf('acacia'), 'air'],
         [0.75, 1, 'dirt', 'dirt', 'dirt', 'acacia_log', __leaf('acacia')],
         [0.7, 1, 'dirt', 'dirt', 'stone', 'stone', 'grass_block'],
         [0.5, 1, 'grass_block'],
         [0.5, 1, 'dirt', 'acacia_log', __leaf('acacia')],
   ]),
   'shattered_savanna' -> __grassy_biome( [
         [0.9, 1, 'dirt', 'grass_block',  'air', 'air', 'dirt', 'acacia_log', __leaf('acacia')],
         [0.7, 1, 'dirt', 'grass_block', 'air', 'dirt', 'acacia_log',__leaf('acacia')],
         [0.7, 1, 'dirt', 'dirt', 'stone', 'grass_block'],
         [0.8, 1, 'dirt', 'acacia_log', __leaf('acacia')],
         [0.3, 1, 'grass_block', 'grass'],
   ]),
   'badlands' -> __simple_biome('red_sand',
      __decorate( ['camp', 'bush'], [
         [0.93, 1, 'cactus', 'cactus', 'tripwire'],
         [0.8, 0, 'red_sandstone'],
      ])
   ),
   'eroded_badlands' -> __simple_biome('red_sand', [
         [0.6, 1, 'light_gray_terracotta', 'red_terracotta'],
         [0.7, 1, 'light_gray_terracotta', 'red_terracotta', 'yellow_terracotta'],
         [0.8, 1, 'light_gray_terracotta', 'red_terracotta', 'yellow_terracotta', 'white_terracotta'],
   ]),
   'badlands_plateau' -> __simple_biome('red_sand', [
         [0.8, 1, 'coarse_dirt'],
         [0.6, 1, 'terracotta'],
         [0.7, 1, 'white_terracotta'],
         [0.7, 1, 'yellow_terracotta'],
         [0.7, 1, 'orange_terracotta'],
         [0.7, 1, 'light_gray_terracotta'],
         [0.7, 1, 'red_terracotta'],
         [0.9, 1, 'white_terracotta', 'dead_bush'],
         [0.9, 1, 'yellow_terracotta', 'dead_bush'],
         [0.9, 1, 'orange_terracotta', 'dead_bush'],
         [0.9, 1, 'light_gray_terracotta', 'dead_bush'],
         [0.85, 1, 'coarse_dirt', 'oak_log', __leaf('oak')],
   ]),
   'modified_badlands_plateau' -> __simple_biome('red_sand', [
         [0.7, 1, 'terracotta'],
         [0.8, 1, 'white_terracotta'],
         [0.8, 1, 'yellow_terracotta'],
         [0.8, 1, 'orange_terracotta'],
         [0.8, 1, 'light_gray_terracotta'],
         [0.9, 1, 'white_terracotta', 'dead_bush'],
         [0.9, 1, 'yellow_terracotta', 'dead_bush'],
         [0.9, 1, 'orange_terracotta', 'dead_bush'],
         [0.9, 1, 'light_gray_terracotta', 'dead_bush'],
   ]),
   
};

// slightly modified
global_biome_data:'wooded_badlands_plateau' = copy(global_biome_data:'badlands_plateau');
global_biome_data:'wooded_badlands_plateau':'decoration':(-1):0 = 0.6;

global_biome_data:'tall_birch_forest' = copy(global_biome_data:'birch_forest');
global_biome_data:'tall_birch_forest':'decoration' += [0.8, 1, 'birch_log', 'birch_log', __leaf('birch')];

global_biome_data:'snowy_taiga_mountains' = copy(global_biome_data:'snowy_taiga_hills');
global_biome_data:'snowy_taiga_mountains':'decoration' += [
   0.70, 1, 'snow_block', 'snow_block', 'spruce_log', __leaf('spruce')
];

global_biome_data:'tall_birch_hills' = copy(global_biome_data:'birch_forest_hills');
global_biome_data:'tall_birch_hills':'decoration' += [0.8, 1, 'dirt', 'birch_log', 'birch_log', __leaf('birch')];

// direct copies
global_biome_data:'deep_ocean' = global_biome_data:'ocean';
global_biome_data:'deep_frozen_ocean' = global_biome_data:'frozen_ocean';
global_biome_data:'deep_cold_ocean' = global_biome_data:'cold_ocean';
global_biome_data:'deep_lukewarm_ocean' = global_biome_data:'lukewarm_ocean';

global_biome_data:'jungle_edge' = global_biome_data:'plains';
global_biome_data:'modified_jungle_edge' = global_biome_data:'plains';
global_biome_data:'modified_wooded_badlands_plateau' = global_biome_data:'wooded_badlands_plateau';
global_biome_data:'mountain_edge' = global_biome_data:'mountains';

// fundamental biomes (categories):
global_biome_data:'none' = __simple_biome('glass', null);
global_biome_data:'none_hills' = __simple_biome('glass', null);
global_biome_data:'none_mountains' = __simple_biome('glass', null);
// taiga (covered - all three)

global_biome_data:'extreme_hills' = global_biome_data:'mountain_edge';
global_biome_data:'extreme_hills' = global_biome_data:'mountains';
global_biome_data:'extreme_hills' = global_biome_data:'mountains';
// jungle
global_biome_data:'jungle_mountains' = global_biome_data:'modified_jungle';

global_biome_data:'mesa' = global_biome_data:'badlands';
global_biome_data:'mesa_hills' = global_biome_data:'modified_badlands_plateau';
global_biome_data:'mesa_mountains' = global_biome_data:'badlands_plateau';
// plains (covered)
global_biome_data:'plains_hills' = __grassy_biome( [
         [0.98, 1, 'grass_block','campfire'],
         [0.5, 1, 'grass_block'],
   ]);
   
global_biome_data:'plains_mountains' =  __grassy_biome( [
         [0.98, 1, 'dirt', 'grass_block','campfire'],
         [0.7, 1, 'dirt', 'grass_block', 'grass'],
         [0.6, 1, 'grass_block', 'grass'],
         [0.6, 1, 'grass_block', 'grass'],
         [0.3, 1, 'grass_block'],
   ]);
// savanna (covered)
global_biome_data:'savanna_hills' = global_biome_data:'savanna_plateau';
global_biome_data:'savanna_mountains' = global_biome_data:'savanna_plateau';

global_biome_data:'icy' = global_biome_data:'snowy_tundra';
global_biome_data:'icy_hills' = global_biome_data:'snowy_mountains';
global_biome_data:'icy_mountains' = global_biome_data:'snowy_mountains';
// the_end (covered)
global_biome_data:'the_end_hills' = global_biome_data:'end_midlands';
global_biome_data:'the_end_mountains' = global_biome_data:'end_highlands';
// beach (covered)
global_biome_data:'beach_hills' = global_biome_data:'beach';
global_biome_data:'beach_mountains' = global_biome_data:'beach';

// forest (covered)
global_biome_data:'forest_hills' = global_biome_data:'wooded_hills';
global_biome_data:'forest_mountains' = global_biome_data:'wooded_mountains';
// ocean (covered)
global_biome_data:'ocean_hills' = global_biome_data:'deep_ocean';
global_biome_data:'ocean_mountains' = global_biome_data:'deep_ocean';
// desert (covered)
global_biome_data:'desert_mountains' = __simple_biome('sand', [
   [0.7, 1, 'sand', 'sand'], 
   [0.7, 1, 'sand']
]);
// river (covered)
global_biome_data:'river_hills' = global_biome_data:'river';
global_biome_data:'river_mountains' = global_biome_data:'river';
// swamp (covered)
global_biome_data:'swamp_mountains' = global_biome_data:'swamp_hills';

global_biome_data:'mushroom' = global_biome_data:'mushroom_fields_shore';
global_biome_data:'mushroom_hills' = global_biome_data:'mushroom_fields';
global_biome_data:'mushroom_mountains' = global_biome_data:'mushroom_fields';

global_biome_data:'nether' = global_biome_data:'nether_wastes';
global_biome_data:'nether_hills' = __simple_biome('netherrack',
   [
      [0.8, 1, 'netherrack', 'netherrack'],
      [0.7, 1, 'netherrack'],
      [0.9, 0, 'lava']
   ]
);

global_biome_data:'nether_mountains' = __simple_biome('netherrack',
   [
      [0.9, 1, 'netherrack', 'netherrack', 'netherrack'],
      [0.8, 1, 'netherrack', 'netherrack'],
      [0.7, 1, 'netherrack'],
      [0.9, 0, 'lava']
   ]
);


// structures
global_structure_data = 
{
   // standard structures
   'bastion_remnant'  -> { 'main' -> ['polished_blackstone_slab', 1, 
         'cracked_polished_blackstone_bricks', 'gilded_blackstone', 'polished_blackstone_brick_stairs'] },
   //'nether_fossil'  -> { 'main' -> [ 'bone_block', 1, 'bone_block', 'bone_block'] }, 
         // these are not contributing to anything and are only spamming 
   'witch_hut'        -> { 'main' -> ['spruce_slab', 1, 'spruce_planks', 'cauldron'] },
   'ruined_portal'    -> { 'main' -> [ 'crying_obsidian', 1, 'gold_block', 'crying_obsidian'] },
   'treasure'         -> { 'main' -> ['air', 1, 'sandstone_wall', 'chest'] },
   'monument'         -> { 'main' -> ['prismarine_brick_slab', 1, 'sea_lantern', 'prismarine_brick_stairs'] },
   'desert_pyramid'    -> { 'main' -> [ 'cut_sandstone', 1, 'chiseled_red_sandstone', 'smooth_red_sandstone_stairs'] },
   'jungle_pyramid'    -> { 'main' -> [ 'mossy_cobblestone', 1, 
         'mossy_cobblestone', 'mossy_cobblestone', 'mossy_cobblestone_stairs'] },
   'mansion'          -> { 'main' -> [ 'dark_oak_planks', 1, 
         'dark_oak_planks', 'dark_oak_wood', 'cobblestone', 'dark_oak_stairs'] },
   'pillager_outpost' -> { 'main' -> [ 'birch_slab', 1, 'cobblestone', 'dark_oak_wood', 'dark_oak_slab'] },
   'shipwreck'        -> { 'main' -> [ 'jungle_slab', 1, 
         'jungle_stairs[half=top,facing=south]', 'jungle_fence', 'white_wool'] },
   'stronghold'       -> { 'main' -> [ 'stone_brick_slab', 1, 
         'stone_bricks', 'bookshelf', 'end_portal_frame', 'end_portal'] },
   // custom data
   'fortress' -> { 
      'main' ->    ['nether_brick_slab', 1, 'nether_bricks', 'nether_bricks' , 'nether_brick_fence'],
      'special' -> _(pos, piece) -> if (piece:0 == 'nemt', 'nether_brick_fence'), 
   },
   'ocean_ruin' -> {
      'main' -> [ 'mossy_cobblestone_slab', 1, 'magma_block', 'mossy_cobblestone_wall'],
      'variant' -> _(pos, pieces) -> if ( biome(pos) ~ 'warm', ['sandstone_slab', 1, 'magma_block', 'sandstone_wall'])
   },
   'igloo' -> {
      'main' -> ['snow_block', 1, 'snow_block', 'oak_trapdoor'],
      'variant' -> _(world_pos, pieces) -> 
         if( length(pieces)==1, ['snow_block', 1, 'snow_block', 'redstone_torch'], null)
   },
   'village' -> {
      'main' -> ['oak_slab', 1, 'cobblestone', 'glass', 'oak_planks', 'bell'],
      'variant' -> _(pos, pieces) -> (
         biome = biome(pos);
         if (
            biome ~ 'desert',  ['cut_sandstone_slab'   , 1, 'sandstone'  , 'glass', 'cut_sandstone', 'bell'],
            biome ~ 'savanna', ['acacia_slab'          , 1, 'cobblestone', 'glass', 'acacia_planks', 'bell'],
            biome ~ 'tundra',  ['polished_diorite_slab', 1, 'blue_ice'   , 'glass', 'snow_block'   , 'bell'],
            biome ~ 'taiga',   ['spruce_slab'          , 1, 'cobblestone', 'glass', 'spruce_planks', 'bell'],
         )
      )
   },
   'mineshaft' -> {
      'main' -> ['rail', 1, 'oak_fence', 'oak_planks' , 'torch'],
      'variant' -> _(pos, pieces) -> 
         if ( biome(pos) ~ 'badlands', ['rail', 1, 'dark_oak_fence', 'dark_oak_planks' , 'torch']),
      'refiner' -> _(display_set) -> ( // finds only the outer edge of the structure, not to pollute the map
         perimeter_set = {};
         for(display_set,
            if (scan(_, 1, 0, 1, has(display_set, pos(_))) < 9,
               perimeter_set:_ = display_set:_ ;
            )
         );
         perimeter_set;
      ),
   },
   
   //custom end city model
   
   'endcity' -> { 'custom' -> _(structure_pos, pieces) -> 
      (
         dim = current_dimension();
         print_pos = call(global_data:dim:'map_mapper', structure_pos);
         corridores = {};
         shulker_rooms = {};
         loot_rooms = {};
         elytra = null;
         min_y = 255;
         // custom scale factor for the cities, otherwise it will be just a few blocks
         shrinking_factor = 2*sqrt(global_config:dim:'scale');
         for(pieces,
            [width, height, len] = _:3 - _:2;
            center = (_:2+_:3)/2;
            piece_irl_offset = center - structure_pos;
            piece_map_offset = piece_irl_offset / shrinking_factor;
            piece_map_coord = print_pos + piece_map_offset;
            min_y = min(min_y, piece_map_coord:1);
            if (
                // ze boat
               height == 23,
                  xdif = center:0-structure_pos:0;
                  zdif = center:2-structure_pos:2;
                  elytra = if (abs(xdif) > abs(zdif), if (xdif<0, 'west', 'east'), if (zdif<0, 'north', 'south') );
                  elytra_pos = piece_map_coord,
                  
               // larger structures, most odd size are loot rooms and even - shulker towers
               width >= 9 && width==len && height > 2,
                   if (width == 16 || width%2, loot_rooms, shulker_rooms) += piece_map_coord,
               
               // skipping roofs over various rooms
               height > 2, 
                  corridores += piece_map_coord
            )
         );
         // adjusting bottom of the structure to the bottom of the map
         y_correction = print_pos:1-floor(min_y)+1;
         city = {};
         for (corridores, city:pos_offset(_,'up',y_correction) = 'purpur_block');
         for (shulker_rooms, city:pos_offset(_,'up',y_correction) = 'purpur_pillar');
         for (loot_rooms, city:pos_offset(_,'up',y_correction) = 'end_stone_bricks');
         if (elytra, 
            boat_pos = pos_offset(elytra_pos,'up',y_correction);
            city:boat_pos = str('dragon_wall_head[facing=%s]', elytra);
            boat_pos = pos_offset(boat_pos, elytra, -1);
            city:boat_pos = str(
               'purpur_stairs[half=top,facing=%s]', 
               if(elytra=='east', 'west', elytra=='west', 'east', elytra=='north', 'south', 'north')
            );
            city:pos_offset(boat_pos, 'up') = 'end_rod';
            
         );
         city:pos_offset(print_pos,'up') = 'end_rod';
         city
      )
   },
};
// for pre 1.16.2 compatibility
global_structure_data:'end_city' = global_structure_data:'endcity';
global_structure_data:'jungle_temple' = global_structure_data:'jungle_pyramid';
global_structure_data:'desert_temple' = global_structure_data:'desert_pyramid';
global_structure_data:'swamp_hut' = global_structure_data:'witch_hut';


// chunk states / statuses

global_chunk_statuses = [
   {
      'id' -> 'prepare',
      'debug_fill' -> 0x99999955,
      'populate' -> _(world_chunk) -> __prepare_chunk(world_chunk),
      'unpopulate' -> _(world_chunk) -> __vacate_chunk(world_chunk),
   },
   {
      'id' -> 'biomes',
      'debug_fill' -> 0x12345655,
      'populate' -> _(world_chunk) -> __apply_biome_changes(
            world_chunk,
            _(biome, p) -> if (air(p)||block(p)=='black_concrete', set(p, __main_biome_block(biome))), // block setter
            _(biome, p, yset) -> for(yset, set_biome(p:0, _, p:2, biome, false)) // biome setter
      ),
      'unpopulate' -> _(world_chunk) -> __apply_biome_changes(
            world_chunk, 
            _(biome, p) -> if( __can_remove_biome_marker_on_flat(biome, p), set(p, 'black_concrete')), // block setter
            _(biome, p, yset) -> for(yset, set_biome(p:0, _, p:2, 'the_void', false)) // biome setter
      ),
   },
   {
      'id' -> 'structures',
      'debug_fill' -> 0xFFFF0055,
      'populate' -> _(world_chunk) -> __apply_structure_changes(
            world_chunk, 
            _(p, b) -> if (!solid(p),set(p, b)), // area block setter
            _(p, b) -> set(p, b) // structure start setter
      ),
      'unpopulate' -> _(world_chunk) -> __apply_structure_changes(
            world_chunk, 
            _(p, b) -> if(b ~ block(p), set(p, 'air') ), // area block setter
            _(p, b) -> if(b ~ block(p), set(p, 'air') ) // structure start setter
      ),
   },
   {
      'id' -> 'decorations',
      'debug_fill' -> 0xFF888855,
      'populate' -> _(world_chunk) -> __apply_decoration_changes(
            world_chunk,
            '__good_feature_spot', // valid position predicate
            _(biome, p, block) -> if(air(p) || __can_remove_biome_marker(biome, p), set(p, block)) // block setter
      ),
      'unpopulate' -> _(world_chunk) -> __apply_decoration_changes(
            world_chunk,
            _(p) -> true,   // valid position predicate
            _(biome, p, block) -> if( block ~ block(p), set(p, 'air') ) // block setter
      ),
   },
];

// fetches required Y levels to set a biome to
// nether has 3d biomes while other dimensions 2d
__required_biome_change_elevation(map_level, dim) -> if ( dim == 'the_nether', ([range(8)]-1)+map_level, [0]);



__main_biome_block(biome) -> (global_biome_data:biome:'block' || __create_custom_biome(biome):'block' );

__to_chunk_coords(pos, dim) -> [floor(pos:0/16), floor(pos:2/16), dim];

__from_chunk_coords(chunkpos) -> [[16*chunkpos:0, 0, 16*chunkpos:1], chunkpos:2];

__distancexz(vec1, vec2) -> sqrt((vec1:0-vec2:0)^2+(vec1:2-vec2:2)^2);

__can_remove_biome_marker(biome, pos) -> 
(
   if (pos:1 == global_config:current_dimension():'map_center':1, 
      __can_remove_biome_marker_on_flat(biome, pos) 
   )
);

__can_remove_biome_marker_on_flat(biome, pos) -> 
(
   target = __main_biome_block(biome);
   existing = str(block(pos));
   (target == existing) || existing == 'air' || has(global_biome_data:biome:'replaces':existing)
);

// decorations can be place where there is no signs of anything
__good_feature_spot(pos) -> air(pos:0, global_config:current_dimension():'map_center':1+1, pos:2);

// list tuples around a square around (0,0) with a specific distance
__create_chunk_ring(distance) ->
(
   ring_chunks = [];
   for(range(distance, -distance, -1), ring_chunks +=  [ _ , distance ]);
   for(range(distance, -distance, -1), ring_chunks += [-distance, _ ]);
   for(range(-distance, distance, 1), ring_chunks += [_, -distance ] );
   for(range(-distance, distance, 1), ring_chunks += [distance, _ ] );
   ring_chunks;
);

// creates list of offsets of chunks around 0,0 indicating what area needs to be loaded around players.
__create_chunk_update_map(view_distance) ->
(
   max_status = length(global_chunk_statuses)-1;
   chunk_order = [[0,0, max_status]];
   loop(view_distance,
      for (__create_chunk_ring(_+1),
         chunk_order += [_:0, _:1, max_status];
      )
   );
   loop(length(global_chunk_statuses)-1, outer_ring = _;
      for (__create_chunk_ring(view_distance+outer_ring+1),
         chunk_order += [_:0, _:1, max_status-1-outer_ring];
      );
   );
   chunk_order
);

// hash of map settings
__map_digest(dim) -> 
(
   config = global_config:dim;
   hash_code([config:'actual_center', config:'map_center', config:'scale', global_use_actual_structure_data])
);

__on_settings_changed() ->
(
   __setup_globals();
   __setup_player_tracking();
   __save_settings();
);



__setup_globals() ->
(
   global_status_cache = {};
   global_chunk_order = __create_chunk_update_map(global_map_view_distance);
   global_player_chunk_map = {};
   global_use_actual_structure_data = false;
   global_data = {};
   for (['overworld', 'the_end', 'the_nether'],
      global_data:_ = {};
      global_data:_:'digest' = __map_digest(_);
      scale = global_config:_:'scale';
      map_center = global_config:_:'map_center';
      world_center = global_config:_:'actual_center';
      
      global_data:_:'world_mapper' = (
         _(map_pos, outer(scale), outer(map_center), outer(world_center)) -> world_center+scale*(map_pos-map_center);
      );
      global_data:_:'map_mapper' = (_(world_pos, outer(scale), outer(map_center), outer(world_center)) ->
      (
         map_delta_pos = (world_pos-world_center)/scale;
         map_delta_pos:1 = 0;
         map_center+map_delta_pos;
      ));
   );
);

__track_player_on_map(pos, dim, player_name) -> 
   put(global_player_chunk_map, player_name, __to_chunk_coords(pos, dim) );

__remove_from_map(player_name) -> delete(global_player_chunk_map, player_name);

__check_player_on_map(player) -> 
   in_dimension(player,  _get_chunk_status(__to_chunk_coords(pos(player), player~'dimension'), false) >= 0 );

__is_player_on_map(player, deepcheck) -> 
(
   player_name = player~'name';
   if (has(global_player_chunk_map, player_name), // registered on the map
      player_dimension = player~'dimension';
      player_position = pos(player);
      // player shoudn't have moved too much and stayed in the same dimension
      [mappos, dim] = __from_chunk_coords(global_player_chunk_map:player_name);
      if (dim == player_dimension && __distancexz(mappos, player_position) < 200,
         //yes, player is on the map and moved less than 200 blocks in the last check
         true
      ,
         // otherwise we perform deeper check if the player is still in the map tile
         // before we do some booboo
         __check_player_on_map(player)
      );
   ,  // not registered on the map, may still be there - check only periodically
      if (deepcheck,
         is_found = __check_player_on_map(player);
         if (is_found, print(player, format('gi You are now on the map area')));
         is_found
      ,
         false
      )
   );
);



__setup_player_tracking() ->
(
   for (filter(player('all'), __check_player_on_map(_)),
      global_player_chunk_map:(_~'name') = null;
   );
   
   __on_tick() -> 
   (
      current_time = tick_time();
      if(current_time%20 == 0,
         if (length(global_status_cache) > 10000, global_status_cache = {} );
         __player_tick(current_time%600 == 0);
      )
   )
);

__player_tick(thorough) ->
(
   for (filter(player('all'), __is_player_on_map(_, thorough) ),
      in_dimension(_, __check_map_around_player(_));
   )
);

__on_player_connects(player) -> __player_tick(true);
__on_player_disconnects(player, reason) -> __remove_from_map(player~'name');



__load_settings() ->
(
   tag = load_app_data();
   if (tag, 
      __load_settings_from_tag(tag);
   ,
      print('Loading defaults, no config found');
      __setup_defaults(); 
      __save_settings()
   );
);
   

// validate map scale
__validate_scale(scale) -> min(128, max(16, floor(scale/16)*16 ));

// validate view distance
__validate_view_distance(view_distance) -> min(8, max(1, floor(view_distance)));
   

__load_settings_from_tag(tag) ->
(  
   global_config = {};
   for(['overworld', 'the_nether', 'the_end'], dim = _;
      config = {};
      config:'map_center' = tag:(dim+'.MapCenter.[]');
      config:'actual_center' = tag:(dim+'.WorldCenter.[]');
      config:'scale' = __validate_scale(tag:(dim+'.Scale'));
      global_config:dim = config;
   );
   global_map_view_distance = __validate_view_distance(tag:'ViewDistance');
);

// default dimension heigths for the maps
global_dimension_map_level = { 'overworld' -> 65, 'the_nether' -> 130, 'the_end' -> 56 };

__setup_defaults() ->
(  
   global_config = {};
   for(['overworld', 'the_nether', 'the_end'], dim = _;
      config = {};
      map_coord = if(dim == 'overworld', 8000000, 1000000);
      config:'map_center' = [map_coord, global_dimension_map_level:dim, map_coord];
      config:'actual_center' = [0,0,0];
      config:'scale' = if (dim == 'the_end', 32, 16);
      global_config:dim = config;
   );
   global_map_view_distance = 4;
);

__save_settings() ->
(
   tag = nbt('{}');
   for(['overworld', 'the_nether', 'the_end'], dim = _;
      tag:(dim+'.MapCenter') = global_config:dim:'map_center';
      tag:(dim+'.WorldCenter') = global_config:dim:'actual_center';
      tag:(dim+'.Scale') = global_config:dim:'scale';
   );
   tag:'ViewDistance' = global_map_view_distance;
   store_app_data(tag);
);

// chunk debug display

__highlight_chunk(world_chunk, long, color, wireframe) -> if(global_debug,
(
   [chx, chz, dim] = world_chunk;
   chunk_lo = [16*chx, global_config:dim:'map_center':1-1, 16*chz];
   attributes = {
      'from' -> chunk_lo,
      'to'   -> chunk_lo+[16, 5, 16],
      'color' -> color,
   };
   if (wireframe,
      attributes:'line' = 10.0;
   ,
      attributes:'fill' = color;
   );
   draw_shape('box', long, attributes);
));

// checks if player is on the map and triggers generation

global_map_is_busy = false;
__check_map_around_player(player) ->
(
   name = player~'name';
   dim = player~'dimension';
   world_chunk = __to_chunk_coords(pos(player), dim);
   if(world_chunk != global_player_chunk_map:name,
      if (global_map_is_busy,
         if (global_debug, print('busy...'));
         __highlight_chunk(world_chunk, 50, 0xdd000099, true);
      ,
         global_map_is_busy = true;
         global_player_chunk_map:name = world_chunk;
         __check_map_at_location(world_chunk, null);
      )
   )
);

// makes sure map is generated around the player

__check_map_at_location(world_chunk, callback) ->
(
   max_status = length(global_chunk_statuses);
   if (global_generate_forward, 
      __populate(world_chunk, callback, global_parallel, range(max_status), false)
   ,
      
      __vacate(world_chunk, global_parallel, map(range(max_status), max_status-_-1), false)
   )
);

// ensures chunks are populated around the world chunk

__populate(world_chunk, callback, parallel, statuses, verbose) ->
(
   generator = ( _(outer(world_chunk), outer(callback), 
                   outer(parallel), outer(statuses), outer(verbose)) -> synchronize('map_gen',
      [chunk_x, chunk_z, dim] = world_chunk;
      __highlight_chunk(world_chunk, 48000, 0x00990099, true);
      for(statuses,
         stime = time();
         status = _;
         chunk_futures = [];
         for (global_chunk_order, [dx, dz, target_status] = _;
            if (target_status >= status,
               w_chunk = [chunk_x+dx, chunk_z+dz, dim];
               current_status = _get_chunk_status(w_chunk, true); 
               if (current_status == status-1,
                  populator = ( _(outer(w_chunk), outer(status), outer(parallel)) -> (  
                     in_dimension(w_chunk:2,
                        __highlight_chunk(w_chunk, 2400, global_chunk_statuses:status:'debug_fill', false);
                        call(global_chunk_statuses:status:'populate', w_chunk);
                        _set_chunk_status(w_chunk, status);
                        __highlight_chunk(w_chunk, 0, global_chunk_statuses:status:'debug_fill', false);
                     );
                     null
                  ));
                  chunk_futures += if (parallel, task(populator), call(populator));
               );
            )
         );
         if (parallel, for (chunk_futures, task_join(_)));
         if (global_debug && length(chunk_futures) && verbose, print( str(
            '   ... populating %s for %d chunks took %.2fs',
            global_chunk_statuses:status:'id',
            length(chunk_futures), 
            (time()-stime)/1000 
         )));
      );
      __highlight_chunk(world_chunk, 0, 0x00990099, true);
      if (callback, call(callback));
      global_map_is_busy = false;
   ));
   if (parallel, task(generator), call(generator));
);

// reverse operation to populate

__vacate(world_chunk, parallel, statuses, verbose) ->
(
   generator = ( _(outer(world_chunk), outer(parallel), outer(statuses), outer(verbose)) -> (
      [chunk_x, chunk_z, dim] = world_chunk;
      __highlight_chunk(world_chunk, 48000, 0xFF000099, true);
      for(statuses,
         stime = time();
         // with chunk statuses going 0-prep, 1-biomes, 2-structures, 3-decoration
         // we want to rever the process: 3-decoration gone, 2-strucuters gone, 1-biomes gone, 0-clean
         status = _;
         chunk_futures = [];
         for (global_chunk_order, [dx, dz, target_status] = _;
            target_status = length(global_chunk_statuses)-target_status-1;
            if (target_status <= status,
               w_chunk = [chunk_x+dx, chunk_z+dz, dim];
               current_status = _get_chunk_status(w_chunk, true); 
               if (current_status == status,
                  vacator = ( _(outer(w_chunk), outer(status), outer(parallel))-> (    
                     in_dimension(w_chunk:2,
                        __highlight_chunk(w_chunk, 2400, global_chunk_statuses:status:'debug_fill', false);
                        _set_chunk_status(w_chunk, status-1);
                        call(global_chunk_statuses:status:'unpopulate', w_chunk);
                        __highlight_chunk(w_chunk, 0, global_chunk_statuses:status:'debug_fill', false);
                     );
                     null;
                  ));
                  chunk_futures += if (parallel, task(vacator), call(vacator));
               );
            )
         );
         if (parallel, for (chunk_futures, task_join(_)));
         if (global_debug && length(chunk_futures) && verbose, print( str(
            '   ... vacating %s for %d chunks took %.2fs',
            global_chunk_statuses:status:'id',
            length(chunk_futures), 
            (time()-stime)/1000 
         )));
      );
      __highlight_chunk(world_chunk, 0, 0xFF000099, true);
   ));
   if (parallel, task(generator), call(generator));
);

// testing only

__repop_lazy() ->
(
   p = player();
   if (world_chunk = global_player_chunk_map:(p~'name'),
      task( _(outer(world_chunk))->(
         start = time();
         max_status = length(global_chunk_statuses);
         task_join(__vacate(world_chunk, true, map(range(max_status-1), max_status-_-1), true));
         task_join(__populate(world_chunk, null, true, map(range(max_status-1), _+1), true));
         print(str('Repopulated(parallel) in %.2fs', (time()-start)/1000));
      ));   
   );
   ''
);

__repop_eager() ->
(
   p = player();
   if (world_chunk = global_player_chunk_map:(p~'name'),
      start = time();
      max_status = length(global_chunk_statuses);
      __vacate(world_chunk, false, map(range(max_status-1), max_status-_-1), true);
      //__vacate(world_chunk, false, map(range(max_status), max_status-_-1), true);
      game_tick(50);
      __populate(world_chunk, null, false, map(range(max_status-1), _+1), true);
      //__populate(world_chunk, null, false, map(range(max_status), _), true);
      print(str('Repopulated(eager) in %.2fs', (time()-start)/1000));
   );
   ''
);

// synchronized chunk status read/write ops

global_status_cache = {};
__get_chunk_check_mutex(world_chunk) -> str(
   'status_check %d %d %s', 
   world_chunk:0 % global_map_view_distance, 
   world_chunk:1 % global_map_view_distance,
   world_chunk:2
);

_get_chunk_status(world_chunk, validate) ->
(
   [chx, chz, dim] = world_chunk;
   synchronize( __get_chunk_check_mutex(world_chunk),
      if (has(global_status_cache:world_chunk),
         global_status_cache:world_chunk
      ,
         chpos = [chx*16, global_config:dim:'map_center':1-2, chz*16];
         paper_tag = inventory_get(chpos, 0):2;
         status = if (!paper_tag,
            -1
         ,
            if ((paper_tag:'Id'== global_data:dim:'digest') || !validate, paper_tag:'Status', -1);
         );
         if (validate, global_status_cache:world_chunk = status);
         status
      )
   )
);

_set_chunk_status(world_chunk, status) ->
(
   [chx, chz, dim] = world_chunk;
   chpos = [chx*16, global_config:dim:'map_center':1-2, chz*16];
   synchronize( __get_chunk_check_mutex(world_chunk),
      if (status >= 0, 
         task_dock(
            set(chpos, 'dropper');
            tag = nbt('{}');
            tag:'Status' = status;
            tag:'Id' = global_data:dim:'digest';
            inventory_set(chpos, 0, 1, 'paper', tag);
         );
      ,
         set(chpos, 'air');
      );
      delete(global_status_cache:world_chunk);
   )
);

///// World/map generation steps

// 0. Remove previous terrain

__prepare_chunk(world_chunk) ->
(
   [x, z, dim] = world_chunk;
   map_level = global_config:dim:'map_center':1;
   
   // task_dock will synchronize it, making without_updates reliable
   task_dock(without_updates(in_dimension(dim,
      volume(16*x, map_level-2, 16*z, 16*x+15, map_level, 16*z+15, set(_, 'black_concrete') );
      if (map_level >= global_dimension_map_level:dim,
         // better than volume(16*x, map_level+1, 16*z, 16*x+15, 255, 16*z+15, set(_, 'air') );
         volume(16*x, 0, 16*z, 16*x+15, 0, 16*z+15, pp = _;
            for (range(top('surface', pp), map_level, -1), set(pos_offset(pp, 'up', _), 'air')) 
         );
      ,
         volume(16*x, map_level+1, 16*z, 16*x+15, map_level+19, 16*z+15, set(_, 'air') );
         volume(16*x, map_level+20, 16*z, 16*x+15, map_level+20, 16*z+15, set(_, 'black_concrete') );
      )
   )));
);

__vacate_chunk(world_chunk) ->
(
   [x, z, dim] = world_chunk;
   map_level = global_config:dim:'map_center':1;
   task_dock( without_updates( in_dimension(dim,
      volume(16*x, map_level-2, 16*z, 16*x+15, map_level+20, 16*z+15, set(_, 'air') );
   )));
);

// 1. compute biome maps and set the biome depending on the dimension

__apply_biome_changes(world_chunk, block_setter, biome_setter) -> 
(
   [chunk_x, chunk_z, dim] = world_chunk;
   map_level = global_config:dim:'map_center':1;
   world_mapper = global_data:dim:'world_mapper';
   blockset = [];
   volume(16*chunk_x, map_level, 16*chunk_z, 16*chunk_x+15, map_level, 16*chunk_z+15,
      print_pos = pos(_);
      sampling_pos = call(world_mapper, print_pos)+[8,0,8];
      biome = biome(sampling_pos);
      blockset += [biome, print_pos];
   );
   biome_ysets = __required_biome_change_elevation(map_level, dim);
   task_dock( in_dimension(dim,
         for(blockset,
            [biome, pos] = _;
            call(block_setter, biome, pos);
            call(biome_setter, biome, pos, biome_ysets);
         );
         //reload_chunk(16*chunk_x, 0, 16*chunk_z);
   ))
);


// 2. Add/remove structures to the map

__place_mini_feature(feature, print_pos, setter) ->
(
   offset = feature:1;
   for( range(2, length(feature)),
      block = feature:_;
      decor_pos = pos_offset(print_pos, 'up', _i+offset);
      call(setter, decor_pos, block);
   )
);

__apply_structure_changes(world_chunk, area_visitor, structure_start_visitor) -> 
(
   //prep
   [chunk_x, chunk_z, dim] = world_chunk;
   config = global_config:dim;
   
   structure_data = [];
   chunk_size = config:'scale'/16;
   
   structure_fetcher = if (global_use_actual_structure_data,
      _(p) -> structures(p)
   ,
      _(p) -> structure_eligibility(p, null, true)
   );
   
   pieces_fetcher = if (global_use_actual_structure_data, 
      _(data, name, pos) -> structures(pos, name):'pieces'
   ,
      _(data, name, pos) -> data:'pieces'
   );
   
   world_mapper = global_data:dim:'world_mapper';
   map_mapper = global_data:dim:'map_mapper';
   
   block_sets = {};
   special_sets = {};
   structure_heads = {};
   custom_structures = {};
   
   volume(16*chunk_x, config:'map_center':1, 16*chunk_z, 16*chunk_x+15, config:'map_center':1, 16*chunk_z+15,
      print_pos = pos(_);
      sampling_pos = call(world_mapper, print_pos)+[8,0,8];
      point_structures = [];
      // grabbing all structures in the affected area;
      loop(chunk_size, cdx = _;
         loop(chunk_size, cdz = _;
            str_pos = sampling_pos+[16*cdx, 0, 16*cdz];
            starts = call(structure_fetcher, str_pos);
            for (filter(pairs(starts), _:1), point_structures += [_:0, _:1, str_pos]);
         )
      );
      for (point_structures, [structure, data, str_pos] = _;
         if ( has(global_structure_data:structure),
            structure_data = global_structure_data:structure;
            pieces = call(pieces_fetcher, data, structure, str_pos);
            if (has(structure_data:'custom'),
               custom_structures:print_pos = call(structure_data:'custom', str_pos, pieces);
            , // else not a custom feature
               puzzles = if (
                        has(structure_data, 'variant') && 
                        (alternative = call(structure_data:'variant', str_pos, pieces)) &&
                        alternative ,
                  alternative;
               ,
                  structure_data:'main';
               );
               structure_heads:print_pos = puzzles;
               area_block = block(puzzles:0);
               special = structure_data:'special';
               piece_set = {};
               for(pieces,
                  [name, orientation, from, to] = _;
                  // these need to be centered according to the map as well
                  from = pos_offset(call(map_mapper, from), 'up');
                  to = pos_offset(call(map_mapper, to), 'up');
                  
                  volume(from:0, from:1, from:2, to:0, to:1, to:2, piece_set:pos(_) = area_block);
                  
                  if (special && call(special, str_pos, _), special_sets:((from+to)/2) = call(special, str_pos, _));
               );
               if (piece_set && has(structure_data, 'refiner'),
                  piece_set = call(structure_data:'refiner', piece_set);
               );
               for(piece_set, block_sets:_ = piece_set:_);
            );
         )
      )
   );

   task_dock( in_dimension(dim,  // with updates (we want structure elements to connect properly)
      for(pairs(block_sets),
         call(area_visitor, _:0, _:1)
      );
      for(pairs(special_sets),
         call(area_visitor, _:0, _:1);
      );
      for(pairs(structure_heads),
         __place_mini_feature(_:1, _:0, structure_start_visitor )
      );
      for(values(custom_structures),
         for (pairs(_), call(structure_start_visitor, _:0, _:1));
      );
   ));
);

// 3. Random decoration bits. 

// Noisegen doesn't seem to be that random so modulating it with a small irrational epsilons

global_pipix = pi-2.14;
global_pipiz = euler-1.71;
__apply_decoration_changes(world_chunk, valid_position_predicate, block_setter) ->
(
   //prep
   [chunk_x, chunk_z, dim] = world_chunk;
   config = global_config:dim;
   decors = [];
   world_mapper = global_data:dim:'world_mapper';
   
   volume(16*chunk_x, config:'map_center':1, 16*chunk_z, 16*chunk_x+15, config:'map_center':1, 16*chunk_z+15,
      print_pos = pos(_);
      sampling_pos = call(world_mapper, print_pos)+[8,0,8];
      biome_name = biome(sampling_pos);
      noise_pos = sampling_pos/config:'scale';
      for(global_biome_data:biome_name:'decoration' || [],
         decor = _;
         value = simplex(noise_pos:0*global_pipix, 0, noise_pos:2*global_pipiz, _i);
         if (value > decor:0 && call(valid_position_predicate, print_pos),
            decors += [decor, print_pos, biome_name];
         )
      )
   );
   //set
   task_dock( without_updates( in_dimension( dim,
      for(decors, [feature, pos, biome_name] = _;
         __place_mini_feature(feature, pos,
            _(decor_pos, decor_block, outer(biome_name), outer(block_setter)) ->
                  call(block_setter, biome_name, decor_pos, decor_block)
         )
      )
   )));
);

__on_load() ->
(
   __load_settings();
   __setup_globals();
   __setup_player_tracking();
   print('Map setup completed');
);
__on_load();

// that's all important things, now convinience
// will code eating a sweet berry as a teleport call

__on_player_finishes_using_item(player, item, hand) -> if (item:0 == 'sweet_berries', __tp(player) );

global_in_teleport = {};
__release_teleport(player_name) -> delete(global_in_teleport:player_name);

tp() -> 
(
   p = player();
   if (p, in_dimension(p,
      __tp(player());
   ));
);

// verifies player has proper place to land on a terrain and if its not generated moves player to a safe position.
__check_safe_landing(player, pos, dim, immediate) ->
(
   in_dimension(dim, 
      add_chunk_ticket(pos, 'portal', global_map_view_distance+length(global_chunk_statuses));
      pos = [floor(pos:0), global_config:dim:'map_center':1, floor(pos:2)];
      world_chunk = __to_chunk_coords(pos, dim);
      status = _get_chunk_status(world_chunk, true);
      if(immediate && status < 0,
         if (!solid(pos), set(pos, 'black_concrete'));
         set(pos+[0,1,0], 'air');
         set(pos+[0,2,0], 'air');
         set(pos+[0,3,0], 'air');
         modify(player, 'pos', pos+[0.5,2.2,0.5]);
      );
      status
   )
);

// to be called when map is generated to tp start player tracking , and place them on the map
__land_player_on_map(player, dim, target) ->
(
   if (dim == 'the_end', __setup_end_center());
   modify(player, 'pos', target);
   __track_player_on_map(target, dim, player~'name');
   schedule(10, '__release_teleport', player~'name');
);

// returns appropriate safe tp location on the map based on the tentative location
__appropriate_teleport_map_location(requested) ->
(
   top_y = top('motion', requested);
   requested:1 = top_y+1.2;
   requested
);


// teleports player to the map, checking if the map is already generated, and conditionally places the player
// location of the placement can be customized with a post_placer callback
__teleport_player_to_map(player, dim, target, immediate, post_placer) ->
(
   target_status = __check_safe_landing(player, target, dim, immediate);
   if (target_status == length(global_chunk_statuses)-1,
      target = if(post_placer,
         call(post_placer, target_status, player)
      ,
         __appropriate_teleport_map_location(target)
      );
      __land_player_on_map(player, dim, target);
      __check_map_at_location(__to_chunk_coords(target, dim), null);
   ,
      if (!immediate, print('Preparing map, hold on...'));
      player_name = player~'name';
      __check_map_at_location(
         __to_chunk_coords(target, dim),
         _(outer(dim), outer(target), outer(player_name), outer(post_placer), outer(target_status)) -> task_dock(
            player = player(player_name);
            if (player && player~'dimension' == dim,
               target = if(post_placer,
                  call(post_placer, target_status, player)
               ,
                  __appropriate_teleport_map_location(target)
               );
               __land_player_on_map(player, dim, target);
            );
            schedule(10, '__release_teleport', player_name);
         )
      );
   );
);

__teleport_player_to_world(player, target) ->
(
   try
   (
      yposes = [];
      for(range(50), yposes+=74+_; yposes+=74-_);
      
      for( yposes, cy = _;
         scan(target:0, 0, target:2, 8, 0, 8, cx = _x; cz = _z;
            if( air(cx, cy, cz) && air(cx, cy+1, cz) && solid(cx, cy-1, cz),
               target = [cx, cy, cz]+[0.5,0.2,0.5];
               throw();
            )
         )
      );
      for( yposes, cy = _;
         scan(target:0, 0, target:2, 8, 0, 8, cx = _x; cz = _z;
            if( air(cx, cy, cz) && air(cx, cy+1, cz) && air(cx, cy-1, cz),
               target = [cx, cy, cz]+[0.5,0.2,0.5];
               set(cx, cy-1, cz, __main_biome_block(biome(target)));
               throw();
            )
         )
      );
      print(format('rb Couldn\'t find a proper spawn spot'));
   ,
      __remove_from_map(player~'name');
      add_chunk_ticket(target, 'teleport', 4);
      modify(player, 'pos', target);
   );
   schedule(10, '__release_teleport', player~'name');
);

__assert_target_location_is_valid(target) ->
(
   if (abs(target:0) > 15000000 || abs(target:2) > 15000000,
      print(format('r Cannot teleport player back more than 15M blocks away from the center of the world'));
      exit()
   );
);


__tp(player) ->
(
   player_name = player~'name';
   dim = player~'dimension';
   
   if (has(global_in_teleport:player_name), return());
   global_in_teleport += player_name;
   
   if ( ! __is_player_on_map(player, true),
      target = call(global_data:dim:'map_mapper', pos(player));
      __assert_target_location_is_valid(target);
      __teleport_player_to_map(player, dim, target, false, null);
   , //else
      target = call(global_data:dim:'world_mapper', pos(player));
      __assert_target_location_is_valid(target);
      __teleport_player_to_world(player, target);
   );
   '';
);

__setup_end_center() ->
(
   end_center = global_config:'the_end':'map_center';
   set(end_center, 'end_portal');
   set(end_center+[1,0,0], 'bedrock');
   set(end_center+[-1,0,0], 'bedrock');
   set(end_center+[0,0,1], 'bedrock');
   set(end_center+[0,0,-1], 'bedrock');
   end_platform = call(global_data:'the_end':'map_mapper',[100,48,0]);
   set(end_platform, 'obsidian');
);

__on_player_changes_dimension(player, from, from_dim, to, dim) -> 
if (has(global_player_chunk_map, player_name = player~'name'),   
   global_in_teleport += player_name;
   // release player ticket in case this will take longer
   delete(global_player_chunk_map, player_name);
   if (
   to==null,
      // drop tracking
      __release_teleport(player_name);
   , dim == 'the_end', // else if we travel from ow to te
      to_map = call(global_data:dim:'map_mapper', to);
      // prepare remote location to keep it loaded while we set it up
      __teleport_player_to_map(player, dim, to_map, true, null);
      
   , // else (we go nether-ow, or ow-nether)
      world_from = call(global_data:from_dim:'world_mapper', from);
      world_to = world_from*if(from_dim=='overworld', 1/8, 8);
      to_target_map = call(global_data:dim:'map_mapper', world_to);
      
      __teleport_player_to_map(player, dim, to_target_map, false, _(status, player, outer(to_target_map)) ->(
         force_portal =  status < (length(global_chunk_statuses)-1);
         portal_location = null;
         player_in_portal = (player~'portal_timer' > 0);
         if (!force_portal,
            portals = filter(poi(pos_offset(to_target_map,'up', 10), 16), _:0 == 'nether_portal');
            if (portals,
               portal_location = portals:0:2;
               while(block(portal_location)=='nether_portal', 22, 
                  portal_location = portal_location + [0,if(player_in_portal, -1, 1),0]
               );
               without_updates(set(portal_location, 'obsidian'));
               portal_location = portal_location + [0,1,0];
            )
         );
         if (portal_location == null,
            __setup_mini_portal(to_target_map, player~'dimension');
            portal_location = to_target_map + [0,if(player_in_portal,1,4),0];
         );
         map(portal_location, floor(_))+[0.5, 0.2, 0.5]
      ));
   );
);

__setup_mini_portal(pos, dim) -> without_updates(
   accent_block = __main_biome_block(biome(call(global_data:dim:'world_mapper', pos)));
   set(pos, 'obsidian');
   for([1, 2],
      set(pos+[-1,_,0], 'obsidian');
      set(pos+[0,_,0], 'nether_portal');
      set(pos+[1,_,0], 'obsidian');
   );
   set(pos+[-1,3,0], accent_block);
   set(pos+[0,3,0], 'obsidian');
   set(pos+[1,3,0], accent_block);
);

__on_player_right_clicks_block(player, item, hand, block, face, hitvec) ->
(
   if (item:0 == 'flint_and_steel' 
            && face=='up' 
            && __is_player_on_map(player, false) 
            && hand == 'mainhand' 
            && block == 'obsidian' 
            && (dim = player ~ 'dimension') != 'the_end'
            && pos(block):1 == global_config:dim:'map_center':1,// this is air: 
            //&& block(pos_offset(block, 'up')) == 'fire',
      schedule(0, _(outer(block), outer(dim)) -> 
         if(block(pos_offset(block, 'up'))=='fire',
            __setup_mini_portal(pos(block), dim)
         )
      );
   )
);


global_slime_display = {};
__on_player_uses_item(player, item, hand) ->
(
   if (item:0 == 'slime_ball' && __is_player_on_map(player, false) && player~'dimension' == 'overworld',
      player_name = player~'name';
      if(has(global_slime_display, player_name),
         delete(global_slime_display, player_name);
         return()
      );
      put(global_slime_display, player_name, null);
      radius = item:1;
      center_map = pos(player);
      
      center_world = call(global_data:'overworld':'world_mapper', center_map);
      map_mapper = global_data:'overworld':'map_mapper';
      center_world = [floor(center_world:0/16)*16+8, 0, floor(center_world:2/16)*16+8];
      scan(0, 0, 0, radius, 0, radius,
         world_pos = center_world + [_x*16, 0, _z*16];
         if (in_slime_chunk(world_pos),
            map_pos = call(map_mapper, world_pos);
            map_pos:1 = top('surface', map_pos)-2;
            marker = spawn(
               'falling_block',map_pos, '{BlockState:{Name:"minecraft:slime_block"},Time:1,NoGravity:1,DropItem:0}'
            );
            // scarpet trick to have them never set as block (be on ground)
            modify(marker,'no_clip');
	
            //marker = create_marker(null, map_pos, 'slime_block');
            entity_event(marker, 'on_tick', '_slime_tracker', player_name, map_pos:1);
            entity_event(marker, 'on_removed', '_stop_slime_tracking', player_name);
         )
      );
      ''
   )
);

_stop_slime_tracking(e, player_name) -> delete(global_slime_display, player_name);

_slime_tracker(e, player_name, start_y) ->
(
   player = player(player_name);
   if (!player || !(player~'holds':0=='slime_ball') || !has(global_slime_display, player_name), 
      modify(e, 'remove') // removing triggers on_removed event as well.
   ,
      age = e~'age';
      modify(e, 'y', start_y+min(age/5, 1.5));
      if (age > 50, modify(e, 'age', 50));
   );
);


// demo
clear_cache() ->
(
   global_status_cache = {};
);
