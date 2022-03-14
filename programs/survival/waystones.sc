// This app makes lodestones into waystones players can teleport to.
// The goal is to add waystones to the game in a balanced and natural feeling way.

// When a player clicks a lodestone it displays a list of
// waystones the player has perviously visited.
// the icon for each location is the block the lodestone was placed on.
// Waystone can be named by naming the lodestone in an anvil or with a name tag
// after the fact.

// Configurable Features:
// Teleporting to a waystone costs an ender_eye
// A players waypoints get reset on death.
// Activating a waystone requires 6 copper blocks in a 5x5 area around the lodestone
// by default waystones only work in the overworld dimension.
// by default waystones can't cross dimensions.

// Missing features,
// - pagination, currently the app will display 54 waypoints per player.


global_settings = {
    'dimensional_crossing'->false,
    'enabled_dimensions'->'overworld',
    'offering'->'ender_eye',
    'clear_waypoints_on_death'->true,
    'structure_material'->'copper',
    'structure_size'->6
};

__config()->{
    'command_permission'->'ops',
    'scope'->'global',
    'commands'->{
        ''->'open_waystones_screen',
        'setting offering <item>'->['setting_command','offering'],
        'setting offering'->['setting_read','offering'],
        'setting dimensionalCrossing <bool>'->['setting_command','dimensional_crossing'],
        'setting dimensionalCrossing'->['setting_read','dimensional_crossing'],
        'setting clearWaypointsOnDeath <bool>'->['setting_command','clear_waypoints_on_death'],
        'setting clearWaypointsOnDeath'->['setting_read','clear_waypoints_on_death'],
        'setting enabledDimensions <dimensions>'->['setting_command','enabled_dimensions'],
        'setting enabledDimensions'->['setting_read','enabled_dimensions'],
        'setting structureMaterial <dimensions>'->['setting_command','structure_material'],
        'setting structureMaterial'->['setting_read','structure_material'],
        'setting structureSize <int>'->['setting_command','structure_size'],
        'setting structureSize'->['setting_read','structure_size'],
    },
    'arguments' -> {
        'item'->{'type'->'term', 'suggest'->[
            'ender_pearl', 'ender_eye', 'amethyst_shard', 'end_crystal', 'chorus_fruit', 'totem_of_undying']},
        'setting'->{'type'->'term'},
        'dimensions'->{'type'->'text', 'suggest'->['overworld', 'the_nether', 'the_end', 'overworld the_nether']}
    }
};


// Keep track of all the waystones within the world.
// [x,y,z] > name, dimension, icon
global_waystones = {};
// keep track of the waypoints each player has discovered.
// uuid > [x,y,z](waystone pos) > [x,y,z](tp safe pos)
global_waypoints = {};


__on_start() -> (
    _read_waystones();
    file = read_file('waystone_settings', 'json');
    if(file, global_settings = file);
);
__on_close() -> (
    _write_waystones();
    write_file('waystone_settings', 'json', global_settings);
);

_app_message(p, m) -> (
    display_title(p, 'actionbar', format('wb '+m));
);

setting_read(key) -> (
    print(player(), str('%s: %s', key, global_settings:key));
);

setting_command(val, key) -> (
    global_settings:key = val;
    print(player(), str('%s: %s', key, global_settings:key));
);

__on_player_places_block(p, item, hand, block)->(
    if(item:0=='lodestone',
        pos = pos(block);
        floor = str(block(pos:0, pos:1 - 1, pos:2));
        if(_has_required_structure(pos),
            global_waystones:pos = {
                'icon'->floor,
                'dimension'->current_dimension(),
                'name'->_get_name_from_nbt(item:2) || floor+' Waypoint' 
            };
            _app_message(p, str('Activated %s Waystone', global_waystones:pos:'name'));
            _write_waystones();
            sound( 'block.enchantment_table.use', pos );
            sound( 'minecraft:block.amethyst_block.hit', pos );
        ,
            _app_message(p, str('Waystone structure requires %d %s to activate',
                global_settings:'structure_size',global_settings:'structure_material'));
            sound( 'block.copper.step', pos );
        );
    );
);

__on_player_breaks_block(p, block)->(
    if(block~'lodestone',
        pos = pos(block);
        if(global_waystones:pos,
            sound( 'minecraft:item.trident.thunder', pos );
            _app_message(p, str('Removed %s Waystone', global_waystones:pos:'name'));
            delete(global_waystones:pos); 
        );
    );
);

__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
    if(block~'lodestone' && !p~'sneaking',
        dimension = current_dimension();
        if(global_settings:'enabled_dimensions'~dimension,
            pos = pos(block);
            
            if(global_waystones:pos,
                if( !_handle_name_tag(item, pos, p, hand) && hand=='mainhand',
                    uuid = p~'uuid';
                    _read_player_waypoints(uuid);
                    _mark_player_waypoint(pos, p~'pos', uuid); 
                    sound( 'minecraft:block.amethyst_block.hit', block );
                    _open_waypoint_screen(p, pos, uuid);
                    _write_player_waypoints(uuid);
                );
            ,
                _app_message(p, str('Place the lodestone near a %d block %s structure to activate',
                    global_settings:'structure_size',global_settings:'structure_material'));
            );
        ,
            _app_message(p, 'Waystones dont\'t work in this dimension');
            sound( 'item.trident.return', block );
        );
    );
);

_has_required_structure(pos) -> (
    if(global_settings:'structure_material' && global_settings:'structure_size', 
        count = 0;
        scan(pos, 2,2,2, if( _~(global_settings:'structure_material'), count+=1) );
        count >= global_settings:'structure_size';
    ,true);
);

_has_required_item(p) -> (
    if(!global_settings:'offering', return(true));
    slot = inventory_find(p, global_settings:'offering');
    if(slot,
        item = inventory_get(p, slot);
        inventory_set(p, slot, item:1 - 1);
        true;
    ,
        _app_message(p, str('The Waystone requires an %s offering', global_settings:'offering'));
        sound( 'item.trident.return', p~'pos' );
        false;
    );
);

_warp_player(p, pos, dimension, point)->(
    if(_has_required_item(p),
        in_dimension(dimension, 
            run(str('tp %s %d %d %d', p~'command_name', ...(pos + [0.5, 0, 0.5]) ));
        );
        schedule( 2, _(p, pos, dimension)->(
            in_dimension(dimension, 
                sound( 'item.trident.thunder', pos );
                particle('totem_of_undying', p~'pos'+[0,1,0], 100, 1, 0.1);
            );
        ), p, pos, dimension);
        _app_message(p, global_waystones:point:'name');
    );
);

__on_player_dies(p) -> (
    if(global_settings:'clear_waypoints_on_death', 
        uuid = p~'uuid';
        _read_player_waypoints(uuid);
        delete(global_waypoints:uuid);
        _write_player_waypoints(uuid);
    ); 
);

_handle_name_tag(item, pos, p, hand) -> (
    if(item:0=='name_tag' && item:2:'display',
        global_waystones:pos:'icon':2 = item:2;
        global_waystones:pos:'name' = _get_name_from_nbt(item:2);
        slot = if(hand=='mainhand', p~'selected_slot', -1);
        inventory_set(p, slot, item:1 - 1);
        sound( 'block.enchantment_table.use', pos );
        _app_message(p, 'Waypoint named '+global_waystones:pos:'name');
    true, false);
);

_mark_player_waypoint(stone_pos, point_pos, uuid) -> (
    if(!global_waypoints:uuid, global_waypoints:uuid = {}); 
    global_waypoints:uuid:stone_pos = map(point_pos, floor(_));
);

// op's waystone screen
open_waystones_screen() -> (
    p = player();
    icons = map(pairs(global_waystones), [_:0, _:1, _:0 + [1,0,0]] );
    if(length(icons) > 0,  
        _print_icons_to_screen(_create_warps_screen(p,'fb All Waystones'), icons);
    ,
        _app_message(p, 'No waystones to display');
    );
);

// open when player clicks on a lodestone
_open_waypoint_screen(p, pos, uuid) -> (
    icons = [];
    bad_keys = [];

    // merge waypoints and waystones data into icons.
    for(pairs(global_waypoints:uuid),
        if( global_waystones:(_:0), 
            icons += ([_:0,global_waystones:(_:0),_:1]);
        , 
            bad_keys += _:0;
        );
    );

    // Clear player waypoints with missing waystone entires
    for(bad_keys, delete(global_waypoints:uuid:_));

    // filter out entires in different dimensions.
    if(!global_settings:'dimensional_crossing', 
        dimension = current_dimension();
        icons = filter(icons, _:1:'dimension' == dimension);
    );

    if(length(icons) > 0,  
        _print_icons_to_screen(_create_warps_screen(p, str('fb Visited Waypoints', p~'name')), icons);
    );
);

_create_warps_screen(p, title) -> (
    create_screen(p, 'generic_9x6', format(title), _(screen, p, action, data) -> (
        if(data:'slot' > 53, return());
        if(action=='pickup',
            btn = inventory_get(screen, data:'slot');
            if(!btn, return('cancel'));
            btn = parse_nbt(btn:2);
            _warp_player(p, btn:'pos', btn:'dimension', btn:'point');
            close_screen(screen);
        );
        return('cancel');
    ));
);

_print_icons_to_screen(screen, icons) -> (
    for(slice(sort_key(icons, _:1:'name'),0, min(length(icons),53)),
        stone = _:1;
        nbt = nbt({
            'pos'->_:2,
            'point'->_:0,
            'dimension'->stone:'dimension' || 'overworld',
            'display'->{
                'Lore'->[escape_nbt(str('"%d, %d, %d"', _:0))],
                'Name'->escape_nbt( str('"%s"', stone:'name'))
            }
        });
        try(
            if(stone:'icon'=='air', throw('Dont set inventory slot to air'));
            inventory_set(screen, _i, 1, stone:'icon', nbt);
        , 'exception', 
            inventory_set(screen, _i, 1, 'lodestone', nbt);
        );
    );
);

_write_player_waypoints(uuid) -> (
    _write_pos_map_file(str('players/%s',uuid), global_waypoints:uuid);
    delete(global_waypoints:uuid);
);

_read_player_waypoints(uuid) -> (
    global_waypoints:uuid = _read_pos_map_file(str('players/%s', uuid));
);

_write_waystones() -> (_write_pos_map_file('waystones', global_waystones));

_read_waystones() -> (global_waystones = _read_pos_map_file('waystones') || {});

// keep x,y,z keys intact within json format by converting root map to array and back
_write_pos_map_file(path, map_data) -> (
    write_file(path, 'json', pairs(map_data));
);

_read_pos_map_file(path) -> (
    data = read_file(path, 'json');
    if( data, m(...data), null );
);

_get_name_from_nbt(nbt) -> (
    // turn '"Foo"' or '{"text":"Foo"}' into 'Foo'
    name = decode_json(nbt:'display':'Name');
    return(name:'text' || name);
);
