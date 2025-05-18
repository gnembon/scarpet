// bots
//  save and restore sets of bots (carpet players) and auto-restore bots on server restart
// by Rocka84 (foospils)
// based on keepalive.sc by gnembon
// v1.4.1

global_app_name = system_info('app_name');
global_required_permission = 5;

_print(msg) -> if(player(), print(player(), msg), print(msg));
_print_formatted(...msg) -> _print(format(['bc '  + global_app_name + ': ', ...msg]));
_error(msg) -> (
  _print(format('br '  + global_app_name + ': ', 'r ' + msg));
  exit();
);

_set_name(set) -> 'by 📂 ' + set;
_bot_name(bot) -> 'bl 🤖 ' + bot;

schedule(0, _() -> (
  setting = run('carpet commandPlayer'):1:4;
  if (
    setting ~ ' true'  != null, global_required_permission = 0,
    setting ~ ' false' != null, global_required_permission = 5,
    setting ~ ' ops'   != null, global_required_permission = 2,
    global_required_permission = number(setting ~ '\\d')
  );
));

_check_permission() -> (
  if (player() == null || global_singleplayer, return(global_required_permission < 5));
  player()~'permission_level' >= global_required_permission;
);

__config() -> {
  'scope' -> 'global',
  'commands' ->
  {
    '' -> _() -> _print_formatted('w Manage sets of scarpet players a.k.a. bots'),
    'list' -> 'list_sets',
    'save <set>' -> 'save_set',
    'apply <set>' -> 'apply_set',
    'delete <set>' -> 'delete_set',
    'delete' -> ['delete_set', null],
    'show <set>' -> 'show_set',
    'kill <bot>' -> _(b) -> if(_check_permission(), kill(b), _error('Not allowed')),
    'kill _all' -> 'kill_all',
    'kill _set <set>' -> 'kill_set',
    'info <bot>' -> 'info',
    'info _all' -> 'info_all',
  },
  'arguments' -> {
    'set' -> {
      'type' -> 'term',
      'suggester'-> _(args) -> filter(keys(global_bots), !(_ ~ '^#')),
    },
    'bot' -> {
      'type' -> 'term',
      'suggester'-> _(args) -> filter(player('all'), _~'player_type' == 'fake'),
    }
  }
};

global_bots = read_file('sets', 'nbt');
global_bots = if (global_bots, parse_nbt(global_bots), {});

_persist() -> (
  write_file('sets', 'nbt', encode_nbt(global_bots));
);

_get_hand(p, hand) -> (
  inv = query(p, 'holds', hand);
  if (inv, [inv:0, str(inv:1)], false);
);

_bot_data(p) -> (
  {
    'n' -> p~'name',
    'd' -> p~'dimension',
    'x' -> p~'x',
    'y' -> p~'y',
    'z' -> p~'z',
    'a' -> p~'yaw',
    'p' -> p~'pitch',
    'g' -> p~'gamemode',
    'f' -> p~'flying',
    'm' -> p~'mount'~'pos' || false,
    'v' -> p~'mount'~'name' || false,
    'r' -> _get_hand(p, 'mainhand'),
    'l' -> _get_hand(p, 'offhand'),
    's' -> p~'selected_slot',
    'c' -> p~'sneaking',
    'at' -> p~'swinging',
    'u' -> false,
  };
);

save_set(set) -> (
  if (!_check_permission(), _error('Not allowed'));

  global_bots:set = [];
  for (filter(player('all'), _~'player_type' == 'fake'), global_bots:set += _bot_data(_));
  _persist();

  _print_formatted(_set_name(set), 'w  saved.');
);

_set_hand(p, slot, data) -> (
  if (data, inventory_set(p, slot, number(data:1), data:0), inventory_set(p, slot, 0));
);

global_tries = {};
__configure_bot_when_available(data) -> (
  if ((global_tries:(data:'n') += 1) > 200, (
    _print('gave up on ' + data:'n');
    delete(global_tries, data:'n');
    return();
  ));
  p = player(data:'n');
  if (!p, (
    // _print('waiting for ' + data:'n');
    schedule(1, '__configure_bot_when_available', data);
    return();
  ));
  // _print('found ' + data:'n');

  delete(global_tries, data:n);
  if (p~'player_type' != 'fake', return());

  modify(p, 'flying', data:'f');
  modify(p, 'gamemode', data:'g');

  if (data:'m', (
    mounted = filter(in_dimension(data:'d', entity_area('*', data:'m', [0.5, 0.5, 0.5])), _~'type' != 'player');
    if (mounted, (
      schedule(1, _(p, m) -> modify(p, 'mount', m), p, mounted:0);
    ), (
      schedule(5, _(p, m) -> modify(p, 'pos', m), p, data:'m');
      schedule(6, _(n) -> run(str('player %s mount', n)), data:'n');
    ));
  ));

  run(str('player %s %s', data:'n', if(data:'c', 'sneak', 'unsneak')));

  selected_slot = data:'s'||0;
  modify(p, 'selected_slot', selected_slot);
  _set_hand(p, selected_slot, data:'r');
  _set_hand(p, -1, data:'l');

  if (data:'at', run(str('player %s attack continuous', data:'n')));
  if (data:'u', run(str('player %s use continuous', data:'n')));
);

apply_set(set) -> (
  if (!_check_permission(), _error('Not allowed'));

  for (global_bots:set, (
    p = player(_:'n');

    if (p, (
      if (p~'player_type' != 'fake', continue());
      run(str('/player %s stop', _:'n'));
      run(str('/execute in %s run teleport %s %f %f %f %f %f', _:'d', _:'n', _:'x', _:'y', _:'z', _:'a', _:'p'));
    ),(
      run(str('player %s spawn at %f %f %f facing %f %f in %s', _:'n', _:'x', _:'y', _:'z', _:'a', _:'p', _:'d'));
    ));

    __configure_bot_when_available(_);
  ));

  _print_formatted(_set_name(set), 'w  applied.');
);

global_to_delete = null;

delete_set(set) -> (
  if (!_check_permission(), _error('Not allowed'));

  if (set == null, (
	if (global_to_delete != null, _print_formatted(_set_name(global_to_delete), 'y  not deleted.'));
    global_to_delete = null;

  ), global_to_delete == set, (
    global_to_delete = null;
    delete(global_bots, set);
    _persist();
    _print_formatted(_set_name(set), 'r  deleted.');

  ), !has(global_bots, set), (
    global_to_delete = null;
    _print_formatted(_set_name(set), 'r  does not exist.');

  ), (
    global_to_delete = set;

    _print_formatted(
      'w Are you sure you want to delete ', _set_name(set), 'w ? ',
      'lb [YES] ', str('!/%s delete %s', global_app_name, set),
      'rb [NO]', str('!/%s delete', global_app_name),
    );
  ));
);

kill_set(set) -> (
  if (!_check_permission(), _error('Not allowed'));

  for (global_bots:set, kill(_:'n'));
  _print_formatted('w Bots in ', _set_name(set), 'w  killed.');
);

kill_all() -> (
  if (!_check_permission(), _error('Not allowed'));

  for (filter(player('all'), _~'player_type' == 'fake'), kill(_~'name'));
  _print_formatted(_bot_name('All'), 'w  bots killed.');
);

kill(bot) -> (
  run('player ' + bot + ' stop');
  run('player ' + bot + ' dismount');
  run('player ' + bot + ' kill');
);

_on_exit() -> (
  save_set('#autosave');
  for (filter(player('all'), _~'player_type' == 'fake'), (
    run('player ' + _~'name' + ' dismount');
  ));
);

if (system_info('server_ip') != null, (
  global_singleplayer = false;
  __on_server_shuts_down() -> _on_exit();
  __on_server_starts() -> schedule(40, 'apply_set', '#autosave');
), (
  global_singleplayer = true;
  __on_close() -> _on_exit();
  __on_server_starts() -> schedule(1, 'apply_set', '#autosave');
));


list_sets() -> (
  for(pairs(global_bots), (
    if (_:0 ~ '^#', continue());
    _print(format(_set_name(_:0), 'w  (' + length(_:1) + ' Bots)'))
  ));
  _print('');
);

show_set(set) -> (
  _print(format(_set_name(set)));
  for(global_bots:set, _show_bot(_));
  _print('');
);

info(name) -> (
  _show_bot(_bot_data(player(name)));
);

info_all() -> (
  for (filter(player('all'), _~'player_type' == 'fake'), _show_bot(_bot_data(player(_))));
);

_round2(in) -> round(in*100)/100;

_show_bot(bot) -> (
  data = [
    _bot_name(bot:'n'),
    'w  at ', 'be ' + _round2(bot:'x') + ' ' + _round2(bot:'y') + ' ' + _round2(bot:'z'),
    'w  in ', 'bv ' + bot:'d',
    'w  in ', 'bm ' + bot:'g', 'w  mode',
  ];
  if (bot:'f',  data += 'bc  flying');
  if (bot:'c',  data += 'bc  sneaking');
  if (bot:'m', (
    put(data, null, [
      'w  riding ', 'bc ' + (bot:'v'||'a vehicle'),
    ], 'extend');
  ));
  if (bot:'r', (
    put(data, null, [
      'w  holding ', 'bq ' + bot:'r':0,
      'w  in ', 'bq mainhand',
    ], 'extend');
  ));
  if (bot:'l', (
    put(data, null, [
      'w  holding ', 'bt ' + bot:'l':0,
      'w  in ', 'bt offhand',
    ], 'extend');
  ));

  _print(format(data));
);

