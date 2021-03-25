//
// Places the flower pattern that bonemeal creates when in flower forests.
// large areas may cause quite a bit of lag to run.
// Only places grass blocks + flowers in flower forest biomes.
//
// command usage:
// /flower <position> print - prints what flower will generate at given position
// /flower <position> create <radius> - places grass block with flowers on top in given radius (square area)
// /flower <position> create <radius> only <flower> - same as create, but only places the chosen flower type
//

// flowers that generate in flower forest biome (in order)
global_FLOWERS = ['dandelion', 'poppy', 'allium', 'azure_bluet', 'red_tulip', 'orange_tulip', 'white_tulip', 'pink_tulip', 'oxeye_daisy', 'cornflower', 'lily_of_the_valley'];

__config() -> {
  'stay_loaded' -> true,
  'commands' -> {
    '<pos> print' -> _(position) -> print(get_flower_at(position)),
    '<pos> create <radius>' -> 'flower_area',
    '<pos> create <radius> only <flower>' -> 'filtered_flower_area'
  },
  'arguments' -> {
    'radius' -> {
      'type' -> 'int',
      'min' -> 0,
      'max' -> 1000,
      'suggest' -> [10, 50, 100, 250]
    },
    'flower' -> {
      'type' -> 'block',
      'suggest' -> global_FLOWERS
    }
  }
};

// fills area of given radius with flowers
flower_area(position, radius) -> (
  count = 0;
  without_updates(
    scan(position, radius, 0, radius,
      count += __flower_forest_fill(_);
    );
  );
  print('Filled ' + count + ' flowers');
);

// fills area of given radius with flowers of given type
filtered_flower_area(position, radius, flower_filter) -> (
  // removes "minecraft:" in flower name if exists
  fixed_flower_name = flower_filter - 'minecraft:';
  flower_id = global_FLOWERS ~ fixed_flower_name;

  // checks if given filter is a flower
  if(flower_id == null,
    print('not a valid flower: ' + flower_filter);
    return();
  );

  count = 0;
  without_updates(
    scan(position, radius, 0, radius,
      count += __filtered_flower_forest_fill(_, fixed_flower_name, flower_id);
    );
  );
  print('Filled ' + count + ' flowers');
);

_get_flower_id_at(position) -> (
  x = position : 0;
  z = position : 2;

  // gets simplex noise for given  coordinate with seed 2345 (from vanilla code)
  val = simplex(x / 24, z / 24, null, 2345);
  // makes sure output is between 0 and 0.9999
  val_clamped = min(max(val, 0), 0.9999);
  flower_id = floor(val_clamped * length(global_FLOWERS));
);

get_flower_at(position) -> (
  flower_id = _get_flower_id_at(position);
  global_FLOWERS : flower_id;
);

// check for flower forest biome
_is_flower_forest(position) -> (
  biome(position) == 'flower_forest';
);

// fills an area with flowers
__flower_forest_fill(position) -> (
  flower_pos = pos(position);

  // check for flower forest biome
  if(_is_flower_forest(flower_pos),
    // gets flower at position
    found_flower = get_flower_at(flower_pos);
    __set_flower_at(flower_pos, found_flower);
    // return 1
    1,
  // else
    // return 0
    0
  )
);

// fills an area with flower of given type
__filtered_flower_forest_fill(position, flower, flower_id) -> (
  flower_pos = pos(position);

  // check for flower forest biome
  if(_is_flower_forest(flower_pos),
    // gets flower at position
    found_flower_id = _get_flower_id_at(flower_pos);

    // checks if flower is wanted type
    if(found_flower_id == flower_id,
      __set_flower_at(flower_pos, flower);
      // return 1
      1,
    // else
      // return 0
      0
    ),
  // else
    // return 0
    0
  )
);

// places flower at position
__set_flower_at(position, flower) -> (
  // place grass for flower
  set(pos_offset(position, 'down'), 'grass_block');

  // place flower
  set(position, flower);

  // set block above to air (to give space to walk)
  set(pos_offset(position, 'up'), 'air');
);
