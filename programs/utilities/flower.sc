// Utility for the flower pattern in flower forests
// May cause quite a bit of lag if run on a large area

// use legacy noise for versions before 1.18
global_use_legacy_noise = system_info('game_major_target') < 18;

// list of flowers that generate in flower forest biome (in order)
global_flower_list = [
  'dandelion',
  'poppy',
  'allium',
  'azure_bluet',
  'red_tulip',
  'orange_tulip',
  'white_tulip',
  'pink_tulip',
  'oxeye_daisy',
  'cornflower',
  'lily_of_the_valley'
];
global_flower_count = length(global_flower_list);

// glass blocks that have a similar color to each of the flowers
global_glass_list = [
  'yellow_stained_glass',     // dandelion
  'brown_stained_glass',      // poppy
  'magenta_stained_glass',    // allium
  'light_gray_stained_glass', // azure_bluet
  'red_stained_glass',        // red_tulip
  'orange_stained_glass',     // orange_tulip
  'glass',                    // white_tulip
  'pink_stained_glass',       // pink_tulip
  'gray_stained_glass',       // oxeye_daisy
  'blue_stained_glass',       // cornflower
  'white_stained_glass'       // lily_of_the_valley
];

// noise constants
// "seed" and "scale" values are from vanilla_worldgen/worldgen/configured_feature/flower_flower_forest.json
// scale must be converted to a float then back to double
// if using a custom noise, "firstOctave" must be 0 and "amplitudes" must be [1.0], otherwise this script doesn't support it
global_flower_seed = 2345;
global_scale_1 = 0.02083333395421505;
global_scale_2 = global_scale_1 * 1.0181268882175227;
global_double_perlin_amplitude = 0.8333333333333333;

// these seeds are the first 2 nextLong calls of new Random(global_flower_seed) (for 2345: -1223197305642693068, -8087649459364435462)
// then xor with "octave_0".hashCode() (1261148513)
// todo: have these values automatically calculated from global_flower_seed
global_perlin_seed_1 = -1223197304453310635;
global_perlin_seed_2 = -8087649458443489125;

// settings
global_setting_ignore_biome = false;
global_setting_glass_mode = false;
global_setting_place_air_above = true;

__config() -> {
  'stay_loaded' -> true,
  'commands' -> {
    '' -> '__print_help',
    'count <from> <to>' -> '_run_count_area',
    'fill <from> <to>' -> ['_run_fill_area', null],
    'fill <from> <to> filter <flower>' -> '_run_fill_area',
    'glasstypes' -> '__print_glass_types_list',
    'print <pos>' -> '__print_flower_at',
    'settings' -> '__print_settings_help',
    'settings <setting>' -> '__print_setting',
    'settings <setting> <bool>' -> '__change_setting'
  },
  'arguments' -> {
    'from' -> {
      'type' -> 'pos'
    },
    'to' -> {
      'type' -> 'pos'
    },
    'flower' -> {
      'type' -> 'block',
      'suggest' -> global_flower_list
    },
    'setting' -> {
      'type' -> 'term',
      'options' -> ['ignore_biome', 'glass_mode', 'place_air_above']
    }
  }
};

__print_help() -> (
  help_lines = [
    format('b Flower Script'),
    'Utility for the flower pattern in flower forests',
    'May cause quite a bit of lag if run on a large area',
    '',
    format('b Command Usage:'),
    '/flower - prints help',
    '/flower count <from> <to> - counts number of flowers in area',
    '/flower fill <from> <to> - fill area with flowers',
    '/flower fill <from> <to> filter <flower> - fill area with only given flower',
    '/flower print <pos> - prints the flower at a position',
    '/flower settings <setting> - prints setting value',
    '/flower settings <setting> <true/false> - set setting',
    ''
  ];

  p = player();
  for(help_lines, print(p, _));

  __print_settings_help();
);

count_area(pos1, pos2) -> (
  // create list of zeros for each flower
  area_counts = map(range(global_flower_count), 0);

  // count blocks in area
  volume(pos1, pos2,
    position = pos(_);
    if(global_setting_ignore_biome || _is_flower_forest(position),
      area_counts : _get_flower_id_at(position) += 1;
    );
  );

  p = player();
  // print list of flowers
  print(p, format('b Flowers in area: '));
  for(area_counts,
    print(p, str('%s: %d', global_flower_list : _i, _));
  );
);

_run_count_area(pos1, pos2) -> (
  // print pre-counting message
  print(player(), str('Counting area of %d blocks...', __get_volume_size(pos1, pos2)));

  // run counting task
  task('count_area', pos1, pos2);
);

// fills an area. filter_flower_id must be null for no filter, or an index of global_flower_list
fill_area(pos1, pos2, filter_flower_id) -> (
  count = 0;
  volume(pos1, pos2,
    // adds one for each block in volume that was placed
    count += __place_flower_at(pos(_), filter_flower_id);
  );

  //print post-filling message
  print(player(), str('Filled %d blocks', count));
);

_run_fill_area(pos1, pos2, flower_filter) -> (
  p = player();

  // check for OP
  if(p ~ 'permission_level' < 2,
    __error('You must be OP to fill blocks');
  );
  // check if glass_mode is not enabled and height of volume is more than 1
  if(!global_setting_glass_mode && floor(pos1 : 1) != floor(pos2 : 1),
    __error('You must have glass_mode setting enabled to fill an area with a height greater than 1');
  );

  // if flower is being filtered, get id of filter
  if(flower_filter != null,
    // removes "minecraft:" in name and gets index of flower
    filter_flower_id = global_flower_list ~ (flower_filter - 'minecraft:');

    // checks if given filter is a flower
    if(filter_flower_id == null,
      __error('Not a valid flower: ' + flower_filter);
    ),
  //else:
    filter_flower_id = null;
  );

  // print pre-filling message
  print(p, str('Filling area of %d blocks...', __get_volume_size(pos1, pos2)));

  // run filling task
  task('fill_area', pos1, pos2, filter_flower_id);
);

__print_flower_at(position) -> (
  print(player(), str('Flower at %s: %s', join(', ', position), global_flower_list : _get_flower_id_at(position)));
);

// position must only be integers
_get_flower_id_at(position) -> (
  [x, y, z] = position;
  if(global_use_legacy_noise,
    // gets simplex noise for given coordinate with seed 2345 (from vanilla code)
    val = simplex(x / 24, z / 24, null, 2345),

  //else:
    // gets double perlin noise for given coordinate using calculated perlin seeds
    noise1 = perlin(x * global_scale_1, y * global_scale_1, z * global_scale_1, global_perlin_seed_1);
    noise2 = perlin(x * global_scale_2, y * global_scale_2, z * global_scale_2, global_perlin_seed_2);
    val = (noise1 + noise2) * global_double_perlin_amplitude - global_double_perlin_amplitude + 0.5;
  );

  // makes sure output is between 0 and 0.9999
  val_clamped = min(max(val, 0), 0.9999);
  flower_id = floor(val_clamped * global_flower_count);
  // return flower_id
);

// check for flower forest biome
_is_flower_forest(position) -> (
  biome(position) == 'flower_forest';
);

// finds and places flower at given pos
__place_flower_at(position, filter_flower_id) -> (
  // check for flower forest biome
  if(global_setting_ignore_biome || _is_flower_forest(position),
    // gets flower at position
    found_flower_id = _get_flower_id_at(position);

    // checks if flower is wanted type
    if(filter_flower_id == null || filter_flower_id == found_flower_id,
      without_updates(__set_block_at(position, found_flower_id));
      1, // return 1
    //else:
      0; // return 0
    ),
  //else:
    0; // return 0
  );
);

// places flower/glass at position
__set_block_at(position, flower_id) -> (
  if(global_setting_glass_mode,
    // place glass
    set(position, global_glass_list : flower_id),

  //else:
    // place grass for flower
    set(pos_offset(position, 'down'), 'grass_block');

    // place flower
    set(position, global_flower_list : flower_id);

    if(global_setting_place_air_above,
      // set block above to air (to give space to walk)
      set(pos_offset(position, 'up'), 'air');
    );
  );
);

__get_volume_size(pos1, pos2) -> (
  dx = abs(pos1 : 0 - pos2 : 0) + 1;
  dy = abs(pos1 : 1 - pos2 : 1) + 1;
  dz = abs(pos1 : 2 - pos2 : 2) + 1;

  dx * dy * dz; // return volume size
);

__error(message) -> (
  exit(print(player(), format('r ' + message)));
);

__print_glass_types_list() -> (
  p = player();
  print(p, format('b Glass Types:'));
  for(global_flower_list,
    print(p, str('%s - %s', _, global_glass_list : _i));
  );
);

__print_settings_help() -> (
  help_lines = [
    format('b Settings:'),
    format('y ignore_biome') + ' - checking for flower forest biome before counting/filling will be skipped',
    format('y glass_mode') + ' - similarly colored glass blocks will be placed instead of flower blocks. Required if height of area is more than 1',
    format('y place_air_above') + ' - air will be filled above flowers (only when glass_mode is false)'
  ];

  p = player();
  for(help_lines, print(p, _));
);

__print_setting(setting) -> (
  print(player(), str('Current value of %s: %b', setting, var('global_setting_' + setting)));
);

__change_setting(setting, value) -> (
  var('global_setting_' + setting) = value;
  print(player(), str('Set %s to %b', setting, value));
  __save_settings();
);

__load_settings() -> (
  settings = read_file('flower_settings', 'json');
  // if null (file missing) keep default options
  if(settings != null,
    global_setting_ignore_biome = settings : 'ignore_biome';
    global_setting_glass_mode = settings : 'glass_mode';
    global_setting_place_air_above = settings : 'place_air_above';
  );
);

__save_settings() -> (
  settings = {
    'ignore_biome' -> global_setting_ignore_biome,
    'glass_mode' -> global_setting_glass_mode,
    'place_air_above' -> global_setting_place_air_above
  };
  write_file('flower_settings', 'json', settings);
);

// load the settings from <script path>/flower.data/flower_settings.json
__load_settings();
