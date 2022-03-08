// This app makes lodestones into waystones
// When a player clicks a waystone it 

global_waystones = {};
global_waypoints = {};

global_settings = {
    'dimensional_crossing'->false,
    'dimensions'->['overworld'],
    'required_item'->null,
    'exp_cost'->0,
};

__on_start() -> (
    _read_waystones();
);
__on_close() -> (
    _write_waystones();
);

__on_player_places_block(p, item, hand, block)->(
    if(item:0=='lodestone',
        pos = pos(block);
        global_waystones:pos = {
            'icon'->str(block(pos:0, pos:1 - 1, pos:2)),
            'name'->_get_name_from_nbt(item:2) || 'Unnamed'    
        };
        display_title(p, 'actionbar', format(str('wb Created %s Waystone',global_waystones:pos:'name' )));
        _write_waystones();
    );
);

__on_player_breaks_block(p, block)->(
    if(block~'lodestone',
        pos = pos(block);
        display_title(p, 'actionbar', format(str('wb Removed %s Waystone',global_waystones:pos:'name' )));
        delete(global_waystones:pos); 
    );
);

_warp_player(p, pos, dimension)->(
    in_dimension(dimension, 
        add_chunk_ticket(pos, 'teleport', 2); 
    );
    schedule( 3, _(p, pos, dimension)->(
        in_dimension(dimension, modify(p, 'pos', pos))
    ), p, pos, dimension);
);


__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
    if(block~'lodestone',
        pos = pos(block);
        
        if( !_handle_name_tag(item, pos, p, hand) && hand=='mainhand',
            uuid = p~'uuid';
            _read_player_waypoints(uuid);
            _mark_player_waypoint(pos, p~'pos', uuid); 
            _open_waypoint_screen(p, global_waystones:pos, uuid);
            _write_player_waypoints(uuid);
        );
    );
);

_handle_name_tag(item, pos, p, hand) -> (
    if(item:0=='name_tag' && item:2:'display',
        global_waystones:pos:'icon':2 = item:2;
        global_waystones:pos:'name' = _get_name_from_nbt(item:2);
        slot = if(hand=='mainhand', p~'selected_slot', -1);
        inventory_set(p, slot, item:1 - 1);
        display_title(p, 'actionbar', format('w Waypoint named '+global_waystones:pos:'name'));
    true, false);
);

_mark_player_waypoint(stone_pos, point_pos, uuid) -> (
    if(!global_waypoints:uuid, global_waypoints:uuid = {}); 
    global_waypoints:uuid:stone_pos = map(point_pos, floor(_) + 0.5);
);

_open_waypoint_screen(p, waystone, uuid) -> (


    screen = create_screen(p, 'generic_9x6', format(str('kb %s\'s Waypoints', p~'name')), _(screen, p, action, data) -> (
        if(data:'slot' > 53, return());
        if(action=='pickup',
            btn = inventory_get(screen, data:'slot');
            _warp_player(p, parse_nbt(btn:2:'pos'), 'overworld');
            close_screen(screen)
        );
        'cancel'
    ));

    points = [];
    bad_keys = [];
    for(pairs(global_waypoints:uuid),
        if( global_waystones:(_:0), 
            points += ([_:0,global_waystones:(_:0),_:1]);
        , 
            bad_keys += _:0;
        );
    );
    for(bad_keys, delete(global_waypoints:uuid:_));

  
    
    for(slice(sort_key(points, _:1:'name'),0, min(length(points),53)),
        stone = _:1;
        nbt = nbt({
            'pos'->_:2,
            'display'->{
                'Lore'->[escape_nbt(str('"%d, %d, %d"', _:0))],
                'Name'->escape_nbt( str('"%s"', stone:'name'))
            }
        });
        try(
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
    // turn '"Foo"' or '{"text":"Foo"}' into 'Foo' If
    name = decode_json(nbt:'display':'Name');
    return(name:'text' || name);
);
