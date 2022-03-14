// This app makes lodestones into waystones
// When a player clicks a waystone it displays a list of
// waypoints the player has visited.

global_settings = {
    'dimensional_crossing'->false,
    'enabled_dimensions'->['overworld'],
    'item_cost'->'end_crystal',
    'clear_waypoints_on_death'->true,
    'exp_cost'->0,
};

__config()->{
    'command_permission'->'ops',
    'commands'->{
        ''->'open_waystones_screen',
    }
};


global_waystones = {};
global_waypoints = {};


__on_start() -> (
    _read_waystones();
);
__on_close() -> (
    _write_waystones();
);

_app_message(p, m) -> (
    display_title(p, 'actionbar', format('wb '+m));
);

__on_player_places_block(p, item, hand, block)->(
    if(item:0=='lodestone',
        pos = pos(block);
        floor = str(block(pos:0, pos:1 - 1, pos:2));
        global_waystones:pos = {
            'icon'->floor,
            'dimension'->current_dimension(),
            'name'->_get_name_from_nbt(item:2) || floor+' Waypoint' 
        };
        _app_message(p, str('Summoned %s Waystone', global_waystones:pos:'name'));
        _write_waystones();
    );
);

__on_player_breaks_block(p, block)->(
    if(block~'lodestone',
        pos = pos(block);
        _app_message(p, str('Removed %s Waystone', global_waystones:pos:'name'));
        delete(global_waystones:pos); 
    );
);

_has_required_item(p) -> (
    if(!global_settings:'item_cost', return(true));
    slot = inventory_find(p, global_settings:'item_cost');
    if(slot,
        item = inventory_get(p, slot);
        inventory_set(p, slot, item:1 - 1);
        true;
    ,
        _app_message(p, str('The Waystone requires an %s offering', global_settings:'item_cost'));
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


__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
    if(block~'lodestone' && !p~'sneaking',
        dimension = current_dimension();
        if(first(global_settings:'enabled_dimensions', _ == dimension),
            pos = pos(block);
            
            if( !_handle_name_tag(item, pos, p, hand) && hand=='mainhand',
                uuid = p~'uuid';
                _read_player_waypoints(uuid);
                _mark_player_waypoint(pos, p~'pos', uuid); 
                sound( 'minecraft:block.amethyst_block.hit', block );
                _open_waypoint_screen(p, global_waystones:pos, uuid);
                _write_player_waypoints(uuid);
            );
        ,
            _app_message(p, 'Waystones dont\'t work in this dimension');
            sound( 'item.trident.return', block );
        );
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

open_waystones_screen() -> (
    p = player();
    screen = _create_warps_screen(p,'kb All Waystones');
    icons = map(pairs(global_waystones), [_:0, _:1, _:0 + [1,0,0]] );
    _print_icons_to_screen(screen, icons);
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

_open_waypoint_screen(p, waystone, uuid) -> (
    screen = _create_warps_screen(p, str('fb Visited Waypoints', p~'name'));

    icons = [];
    bad_keys = [];

    for(pairs(global_waypoints:uuid),
        if( global_waystones:(_:0), 
            icons += ([_:0,global_waystones:(_:0),_:1]);
        , 
            bad_keys += _:0;
        );
    );

    // Clear player waypoints with missing waystone entires
    for(bad_keys, delete(global_waypoints:uuid:_));

    if(!global_settings:'dimensional_crossing', 
        dimension = current_dimension();
        icons = filter(icons, _:1:'dimension' == dimension);
    );


    _print_icons_to_screen(screen, icons)
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
    // turn '"Foo"' or '{"text":"Foo"}' into 'Foo' If
    name = decode_json(nbt:'display':'Name');
    return(name:'text' || name);
);
