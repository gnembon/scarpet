global_waypoint_config = {
    // Config option to allow players to tp to the waypoints ( Either via `/waypoint list` or `/waypoint tp` ) 
    // 0 : NEVER
    // 1 : CREATIVE PLAYERS
    // 2 : CREATIVE AND SPECTATOR PLAYERS
    // 3 : OP PLAYERS
    // 4 : ALWAYS
    'allow_tp' -> 2
};

_can_player_tp() -> (
    global_waypoint_config:'allow_tp' == 4 ||
    ( global_waypoint_config:'allow_tp' == 3 && player()~'permission_level' > 1) || 
    ( global_waypoint_config:'allow_tp' == 1 && player()~'gamemode'=='creative') ||
    ( global_waypoint_config:'allow_tp' == 2 && player()~'gamemode_id'%2)
);
_is_tp_allowed() -> global_waypoint_config:'allow_tp'; // anything but 0 will give boolean true

waypoints_file = read_file('waypoints','JSON');
saveSystem() -> (
    write_file('waypoints', 'JSON', global_waypoints);
);
global_authors = {};
global_dimensions = {'overworld'}; // so we only show waypoints in dimensions that have any; shoud also support custom ones
if(waypoints_file == null, 
    global_waypoints = {'Origin' ->[[0,100,0], 'Default waypoint', null, 'overworld']}; saveSystem(),
    global_waypoints = waypoints_file; 
    map(values(global_waypoints), 
        if( (auth = _:2) != null, global_authors += auth);
        global_dimensions += _:3
    );
);

global_settings=read_file('settings', 'JSON');
if(global_settings==null, global_settings={});

global_track = {};

_get_list_item(name, data, tp_allowed, player) -> (
    desc = if(data:1, '^g ' + data:1);
    cond_desc = if(!tp_allowed, desc);
    selected = if(global_track:player == name, 'd X', 'd \ \ ');
    if(global_track:player == name, 
        sel_action = str('!/%s track disable', system_info('app_name'));
        sel_hover = str('^g Click to stop tracking'),
        sel_action = str('!/%s track %s', system_info('app_name'), name);
        sel_hover = str('^g Click to start tracking')
    );
    item = ['w [', selected, sel_hover, sel_action, 'w ]  ', 'by '+name , desc, str('w : %s %s %s ', map(data:0, round(_)))];
    if(tp_allowed, 
        item += str('!/%s tp %s', system_info('app_name'), name);
        item += '^g Click to teleport!',
        // if tp is not allowed, append descripti/read_fileon tooltip
        item += desc
    );
    if(data:2, 
        item += 'g by ';
        item += cond_desc;
        item += 'gb '+data:2;
        //if(!_is_tp_allowed(), item += desc)
        item += cond_desc;
    );
    item
);

list(author) -> (
    player = player();
    if(author != null && !has(global_authors, author), _error(author + ' has not set any waypoints'));
    print(player, format('bc === List of current waypoints ==='));
    tp_allowed = _can_player_tp();
    for(global_dimensions,
        current_dim = _;
        dim_already_printed = false;
        for(pairs(global_waypoints),
            [name, data]= _;
            if(current_dim== data:3 && (author == null || author==data:2),
                if(!dim_already_printed, print(player, format('l in '+current_dim)); dim_already_printed=true); // to avoid printing dim header when filtering authors
                print(player, format( _get_list_item(name, data, tp_allowed, player)))
            )
        )
    )
);

del_prompt(name) -> (
	global_to_delete = name;
	print(player(), format(
		'y Are you sure you want to delete ',
		'yb '+name,
		'y ? ',
		'lb [YES] ',
		str('!/%s confirm_del', system_info('app_name')),
		'rb [NO]',
		str('!/%s cancel_del', system_info('app_name')),
	))
);
confirm_del() -> (
	if(global_to_delete,
		del(global_to_delete);
		global_to_delete = null,
		_error('No deletion to confirm')
	)
);
cancel_del() -> (
	if(global_to_delete,
		print(player(), str('Deletion of %s was cancelled', global_to_delete));
		global_to_delete = null,
		_error('No deletion to confirm')
	)
);

del(name) -> (
    if(delete(global_waypoints,name),
    	global_track:player() = null;
    	print(player(), 'Waypoint ' + name + ' deleted.'),
    	//else, failed
    	_error('Waypoint ' + name + ' does not exist'));
    saveSystem();
);

add(name, poi_pos, description) -> (
    if(
        name=='disable',
        _error('That name is not available, it has a special funciton'),

        has(global_waypoints, name), 
        _error('You are trying to overwrite an existing waypoint. Delete it first.'),
        // else, add new one
        player = player();
        if(poi_pos==null, poi_pos=player~'pos');
        global_waypoints:name = [poi_pos, description, str(player), player~'dimension'];
        global_authors += str(player);
        global_dimensions += player~'dimension';
        print(player, format(
            'g Added new waypoint ',
            str('bg %s ', name),
            str('g at %s %s %s', map(poi_pos, round(_))),
        ));
        saveSystem();
    );
);

edit(name, description) -> (
    if(!has(global_waypoints, name), _error('That waypoint does not exist'));
    global_waypoints:name:1 = description;
    print(player(), format('g Edited waypoint\'s description'))
);

tp(name) -> (
    if(!_can_player_tp(), _error(str('%s players are not allowed to teleport', player()~'gamemode')) ); //for modes 1 and 2
    loc = global_waypoints:name:0;
    dim = global_waypoints:name:3;
    if(loc == null, _error('That waypoint does not exist'));
    print('Teleporting ' +player()+ ' to ' + name);
    run(str('execute in %s run tp %s %s %s %s', dim, player(), loc:0, loc:1, loc:2));
);

track(name) -> (
    player = player();
    if(
        name==null ,
            print(player, format('g Stopped tracking direction')),
        has(global_waypoints, name), 
            print(player, format(str('g Started tracking direction to %s', name))),
        // else, not a name nor null
            _error('Waypoint ' + name + ' does not exist')
    );

    global_track:player = name;
    _track_tick(player);
);

_track_tick(player) -> (
    splayer = str(player);
    if(global_track:player,
        schedule(1, '_track_tick', player),
        display_title(player, 'clear');
        exit();
    );
    
    if( (dim = global_waypoints:(global_track:player):3) == player~'dimension',
	    ppos = player~'pos';
	    voffset = player~'eye_height'*0.6;

        width = if(!has(global_settings:splayer, 'width'), 2, global_settings:splayer:'width');
        length = if(!has(global_settings:splayer, 'length'), 2, global_settings:splayer:'length');

        voff = [0,voffset,0];
	    from = ppos + voff;
	    destination = global_waypoints:(global_track:player):0;

        to = destination-from; // vector joining form with destination
	    distance = sqrt(reduce(to, _*_+_a, 0));
        length = min(length, distance);
        if(length==-1, length=distance);

	    to = to * length/distance;
        if(global_settings:splayer:'flat', to = to * [1, 0, 1] + voff);

	    //create direction marker
	    if( global_settings:splayer:'particles',
	        particle_line('item spectral_arrow 0.8 0.1 1 4', ppos + player~'motion', ppos + to, 2, player),
	        draw_shape('line', 1, 'from', player~'motion' + voff, 'to', to, 'follow', player(), 'player', player, 'line', width)
	    );

	    // show distance and handkle auto turn off
	    if(global_settings:splayer:'distance', display_title(player, 'actionbar', str('Distance to %s: %.0fm', global_track:player, distance)));
	    if( (d = global_settings:splayer:'autodisable')!=-1 && distance <= d, 
	        print(player, format('g You reached your destinaton!')); 
	        global_track:player = null
	    ),
	    // else, print bad dimension
	    display_title(player, 'actionbar', str('Tracking %s in %s', global_track:player, dim) )
	)
);

help() -> (
    player = player();
    print(player, format('bd ==Help for the Waypoints app=='));
    print(player, format(str('f the following commands are available with /%s', system_info('app_name')) ));
    print(player, format('q \ \ add <name> [<pos>] [<description>]', 'fb \ | ', 'g add a new waypoint at given position with given description'));
    print(player, format('q \ \ del <waypoint>', 'fb \ | ', 'g delete existing waypoint'));
    print(player, format('q \ \ edit <waypoint> <description>', 'fb \ | ', 'g edit the description of an existing waypoint'));
    print(player, format('q \ \ list [<author>]', 'fb \ | ', 'g list all existing waypoints, optionally filtering by author'));
    print(player, format('q \ \ settings track [<what> <value>]', 'fb \ | ', 'g sets strack options'));
    if(_is_tp_allowed(),  print(player, format('q \ \ tp <waypoint>', 'fb \ | ', 'g teleport to given waypoint')));  
);

_error(msg)->(
    print(player(), format(str('r %s', msg)));
    exit()
);

_settings(key, value) -> (
    splayer = str(player());
    if(!has(global_settings, splayer), global_settings:splayer = {});
    global_settings:splayer:key = value;
    write_file('settings', 'JSON', global_settings);
);
global_default_settings = {
    'type' -> {'name'->'particles', 'defaults'->['render', 'particle']},
    'distance' -> {'name'->'distance', 'defaults'->['off', 'on']},
    'autodisable' -> 'off',
    'length' -> 2,
    'width' -> 2,
    'flat' -> {'name'->'flat', 'defaults'->['false', 'true']},
};

show_settings() -> (
    splayer = str(player());
    print(splayer, format('b Your current settings are:'));
    for(keys(global_default_settings),
        name = _;
        if(has(global_default_settings:name, 'name'),   //to grab the true name of a display name if they are different
            key = global_default_settings:name:'name';
            default = global_default_settings:name:'defaults',
            
            key = name;
            default = global_default_settings:name
        );
        
        is_default = !has(global_settings:splayer, key);
        value = if(type(default)=='list',
                    default:(global_settings:splayer:key),
                    if(is_default, default, global_settings:splayer:key)
        );
        if(name=='autodisable'&&value==-1, value=default);
        if(name=='length'&&value==-1, value='inf');

        modify_cmd = str('?/%s settings track %s ', system_info('app_name'), name);
        modify_tlt = '^g Click me to modify!';
        print(splayer, format(
            'bd\ \ '+name, modify_tlt, modify_cmd,
            'f \ \ »  ', modify_tlt, modify_cmd,
            'q '+value,  modify_tlt, modify_cmd,
            if(is_default, 'g \ (Unmodified value)')
        ))
    );
);

_get_commands() -> (
    base_commands = {
      '' -> 'help',
      'del <waypoint>' -> 'del_prompt',
      'confirm_del' -> 'confirm_del',
      'cancel_del' -> 'cancel_del',
      'add <name>' -> ['add', null, null],
      'add <name> <pos>' -> ['add', null],
      'add <name> <pos> <description>' -> 'add',
      'edit <waypoint> <description>' -> 'edit',
      'list' -> ['list', null],
      'list <author>' -> 'list',
      'track <waypoint>' -> 'track',
      'track disable' -> ['track', null],
      //'settings track line vector' ->             _()  -> _settings('vector', true),
      //'settings track line direction' ->          _()  -> _settings('vector', false),
      'settings track' -> 'show_settings',
      'settings track type particle' ->           _()  -> _settings('particles',true),
      'settings track type render' ->             _()  -> _settings('particles',false),
      'settings track distance on' ->             _()  -> _settings('distance',true),
      'settings track distance off' ->            _()  -> _settings('distance',false),
      'settings track autodisable off' ->         _()  -> _settings('autodisable', -1),
      'settings track autodisable <distance>' ->  _(d) -> _settings('autodisable', d),
      'settings track width <width>' ->           _(w) -> _settings('width', w),
      'settings track length <length>' ->         _(l) -> _settings('length', l),
      'settings track length inf' ->              _()  -> _settings('length', -1),
      'settings track flat <bool>' ->             _(b) -> _settings('flat', b), 
    };
   if(_is_tp_allowed(), put(base_commands, 'tp <waypoint>', 'tp'));
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
            'suggester'-> _(args) -> keys(global_authors),
      },
      'distance' -> {
            'type' -> 'int',
            'suggest' -> [5, 10],
            'min' -> 0
      },
      'width' -> {
            'type' -> 'int',
            'suggest' -> [5, 10],
            'min' -> 0
      },
      'length' -> {
            'type' -> 'int',
            'suggest' -> [5, 10],
            'min' -> 0
      },
   }
};
