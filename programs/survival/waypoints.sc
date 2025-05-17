// Waypoints - Server wide waypoint system
// Originally by Firigion and boyenn
// Reworked by Rocka84 (foospils)
// v1.4.1

global_waypoint_config = {
  // Config option to allow players to tp to the waypoints ( Either via `/waypoint list` or `/waypoint tp` )
  // 0 : NEVER
  // 1 : CREATIVE PLAYERS
  // 2 : CREATIVE AND SPECTATOR PLAYERS
  // 3 : OP PLAYERS
  // 4 : ALWAYS
  'allow_tp' -> 1,
  'track_ticks' -> 1
};

config_file = read_file('config', 'JSON');
if (config_file != null, (
  global_waypoint_config = global_waypoint_config + config_file;
));

_set_config(key, value) -> (
  if (player()~'permission_level' < 2, _error('not allowed'));
  if (!has(global_waypoint_config, key), _error('unknown config key'));

  if (value == null, (
    print(player(), format('bd\ \ ' + key, 'f \ \ »  ', 'q ' + global_waypoint_config:key));
    return();
  ));

  global_waypoint_config:key = number(value);
  write_file('config', 'JSON', global_waypoint_config);
  run('/script load ' + global_app_name);
);


global_default_settings = {
  'track_autodisable' -> -1,
  'track_indicator' -> 'both',
  'list_show_author' -> false,
  'list_default_dimension' -> 'current',
  'list_sort_by' -> 'name',
};

global_settings=read_file('settings', 'JSON');
if(global_settings==null, global_settings={});

_set_setting(key, value) -> (
  splayer = str(player());
  if(!has(global_settings, splayer), global_settings:splayer = {});

  if (value == global_default_settings:key,
    delete(global_settings:splayer, key),
    global_settings:splayer:key = value
  );
  write_file('settings', 'JSON', global_settings);
);

_get_setting(player, key) -> (
  splayer = str(player());
  if(!has(global_settings, splayer), global_settings:splayer = {});
  if(has(global_settings:splayer, key), global_settings:splayer:key, global_default_settings:key);
);

global_authors = {};
global_dimensions = {};
global_track = {};
global_to_delete = null;
global_app_name = system_info('app_name');

global_waypoints = read_file('waypoints', 'JSON');
if(global_waypoints == null, (
  global_waypoints = {};
),(
  for(values(global_waypoints), (
    global_authors += _:2;
    global_dimensions += _:3
  ));
));

_save_waypoints() -> (
    write_file('waypoints', 'JSON', global_waypoints);
);

_can_player_tp() -> (
  global_waypoint_config:'allow_tp' == 4 ||
  ( global_waypoint_config:'allow_tp' == 3 && player()~'permission_level' > 1) ||
  ( global_waypoint_config:'allow_tp' == 1 && player()~'gamemode'=='creative') ||
  ( global_waypoint_config:'allow_tp' == 2 && player()~'gamemode_id'%2)
);
_is_tp_allowed() -> global_waypoint_config:'allow_tp'; // anything but 0 will give boolean true


_distance(a, b) -> (
  segment = b - a;
  sqrt((segment:0 * segment:0) + (segment:1 * segment:1) + (segment:2 * segment:2));
);

_get_list_item(name, data, tp_allowed, show_author, player) -> (
  desc = if(data:1, '^g ' + data:1);

  selected = if(global_track:player == name, 'e ➡', 'w  ●');
  if(global_track:player == name, (
    sel_action = str('!/%s track disable', global_app_name);
    sel_hover = str('^g Stop tracking')
  ), (
    sel_action = str('!/%s track %s', global_app_name, name);
    sel_hover = str('^g Track')
  ));

  coords = str('%s %s %s', map(data:0, round(_)));

  item = [
    selected, sel_hover, sel_action,
  ];

  if(tp_allowed, (
    put(item, null, [
      't  ⭇', str('!/%s tp %s', global_app_name, name), '^g Teleport'
    ], 'extend');
  ));

  put(item, null, [
    'yb  '+name, sel_hover, sel_action,
    desc,
    'w : ' + coords + ' (' + round(_distance(player~'pos', data:0)) + 'm)', '&' + coords, '^g Copy coords',
  ], 'extend');

  if(show_author && data:2, (
    put(item, null, [
      'g  by ', desc,
      'gb '+data:2, desc,
    ], 'extend');
  ));

  item
);

list(dimensions, author) -> (
  player = player();
  print(player, format([' \n', 'c   ━═= ', 'bc Waypoints ','c =═━']));

  if (length(global_waypoints) < 1, (
    print(player, '\nThere aren\'t any waypoints yet.');
    print(player, format('w Use ', 'be /' + global_app_name + ' add', '?/' + global_app_name + ' add', 'w  to create the first one!'));
    return();
  ));

  if (
    author == 'all',    author = null,
    author == 'myself', author = str(player)
  );
  if (author != null, (
    if(!has(global_authors, author), (
      print(player, format('w \n Player ', 'bi ' + author, 'w  has not set any waypoints yet.'));
      return();
    ));
    show_author = false;
  ),(
    show_author = _get_setting(player, 'list_show_author');
  ));

  if (dimensions == null || dimensions == 'null', dimensions = _get_setting(player, 'list_default_dimension'));
  if (
    dimensions == 'current',    dimensions = [player~'dimension'],
    dimensions == 'all',        dimensions = global_dimensions,
    type(dimensions) != 'list', dimensions = split(',', dimensions)
  );

  tp_allowed = _can_player_tp();

  if (_get_setting(player, 'list_sort_by') == 'distance', (
    waypoint_names = sort_key(keys(global_waypoints), _distance(player~'pos', global_waypoints:_:0));
  ),(
    waypoint_names = sort(keys(global_waypoints));
  ));

  for (dimensions, (
    if (!has(global_dimensions, _), (
      print(player, format('r \nThere are no waypoints in dimension ', 'br ' + _));
      continue();
    ));
    current_dim = _;
    dim_already_printed = false;
    for(waypoint_names, (
      if (current_dim == global_waypoints:_:3 && (author == null || author == global_waypoints:_:2), (
        if (!dim_already_printed, (
          print(player, format('l \n🌍 '+current_dim));
          dim_already_printed=true;
        ));
        print(player, format(_get_list_item(_, global_waypoints:_, tp_allowed, show_author, player)));
      ));
    ));
  ));
  print(player, '');
);

delete_waypoint(name) -> (
  if (name == null, (
	if (global_to_delete != null, print(player(), format('w Waypoint ', 'b ' + global_to_delete, 'y  not deleted.')));
    global_to_delete = null;

  ), global_to_delete == name, (
    global_to_delete = null;
    delete(global_waypoints, name);
    _save_waypoints();
    _exit('w Waypoint ', 'b ' + name, 'r  deleted.');

  ), !has(global_waypoints, name), (
    global_to_delete = null;
    _exit('w Waypoint ', 'b ' + name, 'r  does not exist.');

  ), player()~'permission_level' < 2 && global_waypoints:name:2 != str(player()), (
    global_to_delete = null;
    _exit('w Waypoint ', 'b ' + name, 'r  is not your\'s.');

  ), (
    global_to_delete = name;

    print(player(), format(
      'y Are you sure you want to delete ', 'yb '+name, 'y ? ',
      'lb [YES] ', str('!/%s delete %s', global_app_name, name),
      'rb [NO]', str('!/%s delete', global_app_name),
    ));
  ));
);

add(name, poi_pos, description) -> (
  if (name=='disable', (
    _error('That name is not available, it has a special function')
  ), has(global_waypoints, name), (
    _exit('w Waypoint ', 'b ' + name, 'r  already exists.', 'w Delete it first.');
  ));

  player = player();
  if (poi_pos==null, poi_pos = map(player~'pos', floor(_)) + [0.499, 0, 0.499]); // snap to center of block under player
  global_waypoints:name = [poi_pos, description, str(player), player~'dimension'];
  global_authors += str(player);
  global_dimensions += player~'dimension';
  print(player, format(
    'g Added new waypoint ',
    str('bg %s ', name),
    str('g at %s %s %s', map(poi_pos, round(_))),
  ));
  _save_waypoints();
);

edit(name, description) -> (
  if(!has(global_waypoints, name), (
    _exit('w Waypoint ', 'b ' + name, 'r  does not exist.');
  ), player()~'permission_level' < 2 && global_waypoints:name:2 != str(player()), (
    _exit('w Waypoint ', 'b ' + name, 'r  is not your\'s.');
  ));
  global_waypoints:name:1 = description;
  print(player(), format('g Edited waypoint\'s description'))
);

tp(name) -> (
  if(!_can_player_tp(), _error(str('Teleporting not allowed in %s mode', player()~'gamemode')) );
  loc = global_waypoints:name:0;
  dim = global_waypoints:name:3;
  if(loc == null, _exit('w Waypoint ', 'b ' + name, 'r  does not exist.'));
  print('Teleporting ' +player()+ ' to ' + name);
  run(str('execute in %s run tp %s %s %s %s', dim, player(), loc:0, loc:1, loc:2));
);

track(name) -> (
  player = player();
  if(name == null, (
    if (global_track:player != null, print(player, format('g Stopped tracking direction to ', 'gb ' + global_track:player)));
    global_track:player = null;

  ), !has(global_waypoints, name), (
    _exit('w Waypoint ', 'b ' + name, 'r  does not exist.');

  // ), global_waypoints:(name):3 != player~'dimension', (
  //   _exit('w Waypoint ', 'b ' + name, 'r  is in another dimension.');

  ), global_track:player != name, (
    print(player, format('g Tracking direction to ', 'gb ' + name));
    if (global_track:player == null, (
      global_track:player = name;
      _track_tick(player);
    ), (
      global_track:player = name;
    ));
  ));
);

// __on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> (
//   if (global_track:player, run('execute as ' + player + ' run ' + global_app_name + ' track disable'));
// );

_track_tick(player) -> (
  splayer = str(player);
  if(global_track:player, (
    schedule(global_waypoint_config:'track_ticks', '_track_tick', player);
  ),(
    display_title(player, 'clear');
    exit();
  ));

  if(global_waypoints:(global_track:player):3 != player~'dimension', return());

  track_ticks = global_waypoint_config:'track_ticks';

  ppos = player~'pos';
  look = player~'look';
  eyes = [0, player~'eye_height', 0];
  destination = global_waypoints:(global_track:player):0;

  shape_distance = player~'eye_height';
  autodisable = _get_setting(player, 'track_autodisable');
  indicator = _get_setting(player, 'track_indicator');

  distance = _distance(ppos, destination); //distance from players feet to destination

  if(autodisable > -1 && distance <= autodisable, (
    display_title(player, 'actionbar', format('g You reached your destination!'));
    global_track:player = null;
    return();
  ));

  segment = destination - (ppos + eyes); //vector from players eyes to the destination
  direction = segment / sqrt((segment:0 * segment:0) + (segment:1 * segment:1) + (segment:2 * segment:2)); //vector divided by its length


  if (indicator == 'rendered' || indicator == 'both', (
    if(distance <= shape_distance, (
      shape_pos = destination - ppos;
    ),(
      shape_pos = (shape_distance * direction) + eyes; //draw in _direction_ with _shape_distance_ relative to players _eyes_.
    ));

    shapes = [
      [
        'sphere',
        track_ticks,
        'player', player,
        'follow', player,
        'center', shape_pos,
        'radius', 0.03,
        'color', 0x000000FF,
        'fill', 0xEE00FF44,
      ],
      [
        'label',
        track_ticks,
        'player', player,
        'follow', player,
        'pos', shape_pos,
        'height', 0.3,
        'text', format(['y ' + global_track:player]),
        'size', 3,
      ],
    ];
    if (indicator == 'rendered', (
      shapes += [
        'label',
        track_ticks,
        'player', player,
        'follow', player,
        'pos', shape_pos,
        'height', -0.3,
        'text', '(' + round(distance) + 'm)',
        'size', 2,
      ],
    ));
    draw_shape(shapes);
  ));

  if (indicator == 'text' || indicator == 'both', (
    dy = look:1 - direction:1;
    char_y = if(dy > 0.05, '↓', dy < -0.05, '↑', ' ');

    if (floor(ppos:1) == floor(destination:1), char_y = '🚩');
    display_data = ['lb    '];

    if (indicator == 'text', display_data += 'y ' + global_track:player + ' ');
    display_data += 'w ' + round(distance) + 'm ';

    scalar_xz = (look:0 * direction:2) - (look:2 * direction:0);
    if(floor(ppos:0) == floor(destination:0) && floor(ppos:2) == floor(destination:2), (
      display_data:0 = 'lb 🚩'+char_y+' ';
      display_data  += 'lb '+char_y+'🚩';
    ), scalar_xz < -0.05, (
      display_data:0 = 'lb <'+char_y+' ';
      display_data  += 'lb   ';
    ), scalar_xz >  0.05, (
      display_data  += 'lb '+char_y+'>';
    ), (
      display_data:0 = 'lb  '+char_y+' ';
      display_data  += 'lb '+char_y+' ';
    ));

    display_title(player, 'actionbar', format(display_data));
  ));
);

help() -> (
  player = player();
  print(player, format('bd ==Help for the Waypoints app=='));
  print(player, format(str('f the following commands are available with /%s', global_app_name) ));
  print(player, format('q \ \ add <name> [<pos>] [<description>]', 'fb \ | ', 'g add a new waypoint at given position with given description'));
  print(player, format('q \ \ del <waypoint>', 'fb \ | ', 'g delete existing waypoint'));
  print(player, format('q \ \ edit <waypoint> <description>', 'fb \ | ', 'g edit the description of an existing waypoint'));
  print(player, format('q \ \ list [<dimension>] [<author>]', 'fb \ | ', 'g list all existing waypoints, optionally filtering by dimensions and/or author'));
  print(player, format('q \ \ settings [<category> <what> <value>]', 'fb \ | ', 'g sets options'));
  if(_is_tp_allowed(),  print(player, format('q \ \ tp <waypoint>', 'fb \ | ', 'g teleport to given waypoint')));
  if (player()~'permission_level' > 1, (
    print(player, format('q \ \ config <config>', 'fb \ | ', 'g get config option (admin only)'));
    print(player, format('q \ \ config <config> <value>', 'fb \ | ', 'g set config option (admin only)'));
  ));
);

_error(msg)->(
  _exit(str('r %s', msg))
);

_exit(...msg) -> (
  print(player(), format(msg));
  exit();
);

show_settings() -> (
  splayer = str(player());
  if(!has(global_settings, splayer), global_settings:splayer = {});
  print(splayer, format('b Current settings:'));
  for(sort(keys(global_default_settings)), (
    key = _;
    if (has(global_settings:splayer, key), (
      is_default = false;
      value = global_settings:splayer:key;
    ),(
      is_default = true;
      value = global_default_settings:key;
    ));

    if(key=='track_autodisable' && value==-1, value='off');

    name = split('_', key);
    category = name:0;
    delete(name, 0);
    name = join('_', name);

    modify_cmd = str('?/%s settings %s %s ', global_app_name, category, name);
    modify_tlt = '^g Click to modify';

    print(splayer, format(
      'bd\ \ '+category+' '+name, modify_tlt, modify_cmd,
      'f \ \ »  ', modify_tlt, modify_cmd,
      'q '+value,  modify_tlt, modify_cmd,
      if(is_default, 'g \ (Unmodified value)')
    ))
  ));
);

_get_commands() -> (
  base_commands = {
    '' -> 'help',
    'delete' -> ['delete_waypoint', null],
    'delete <waypoint>' -> 'delete_waypoint',

    'add <name>' -> ['add', null, null],
    'add <name> <pos>' -> ['add', null],
    'add <name> <pos> <description>' -> 'add',
    'edit <waypoint> <description>' -> 'edit',

    'list'                      -> _()    -> list(null, null),
    'list <dimension>'          -> _(d)   -> list(d, null),
    'list <dimension> <author>' -> _(d,a) -> list(d, a),

    'track <waypoint>' -> 'track',
    'track disable'    -> ['track', null],

    'settings' -> 'show_settings',
    'settings track indicator <type>'       -> _(t) -> _set_setting('track_indicator', t),
    'settings track autodisable off'        -> _()  -> _set_setting('track_autodisable', -1),
    'settings track autodisable <distance>' -> _(d) -> _set_setting('track_autodisable', d),
    'settings list  show_author <value>'    -> _(v) -> _set_setting('list_show_author', v),
    'settings list  default_dimension <dimension>' -> _(v) -> _set_setting('list_default_dimension', v),
    'settings list  sort_by distance'       -> _()  -> _set_setting('list_sort_by', 'distance'),
    'settings list  sort_by name'           -> _()  -> _set_setting('list_sort_by', 'name'),
  };
  if(_is_tp_allowed(), put(base_commands, 'tp <waypoint>', 'tp'));
  if (player()~'permission_level' > 1, (
    put(base_commands, 'config <config>', ['_set_config', null]);
    put(base_commands, 'config <config> <config_value>', '_set_config');
  ));
  base_commands;
);

__config() -> {
  'scope'->'global',
  'stay_loaded'-> true,
  'commands' -> _get_commands(),
  'arguments' -> {
    'waypoint' -> {
      'type' -> 'term',
      'suggester'-> _(args) -> keys(global_waypoints),
    },
    'name' -> {
      'type' -> 'term',
      'suggest' -> [], // to make it not suggest anything
    },
    'description' -> {
      'type' -> 'text',
      'suggest' -> [],
    },
    'author' -> {
      'type' -> 'term',
      'suggester'-> _(args) -> (
        suggest = ['all', 'myself'];
        put(suggest, 0, keys(global_authors), 'extend');
        suggest;
      ),
    },
    'dimension' -> {
      'type' -> 'term',
      'suggester'-> _(args) -> (
        suggest = ['all', 'current'];
        put(suggest, 0, keys(global_dimensions), 'extend');
        suggest;
      ),
    },
    'distance' -> {
      'type' -> 'int',
      'suggest' -> [5, 10],
      'min' -> 0
    },
    'value' -> {
      'type' -> 'bool',
    },
    'config' -> {
      'type' -> 'term',
      'suggester'-> _(args) -> keys(global_waypoint_config),
    },
    'config_value' -> {
      'type' -> 'term',
    },
    'type' -> {
      'type' -> 'term',
      'options' -> ['rendered', 'text', 'both'],
    },
  }
};

