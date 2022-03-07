// This app makes lodestones into waystones
// When a player clicks a waystone it 

global_waystones = {};

global_waypoints = {};

__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
    if(block~'lodestone',
        print(item:0);
        pos = pos(block);

        _handle_new_waystone(pos); 

        
        if( !_handle_name_tag(item, pos, p, hand) && hand=='mainhand',
            _read_player_waypoints(p);
            _mark_player_waypoint(p, pos); 
            _open_waypoints_screen(p);
        );
        

    );
);


_handle_new_waystone(pos) -> (
    if(!global_waystones:pos, 
        icon = block(pos:0, pos:1 - 1, pos:2);
        if(icon~'air', icon = block('lodestone'));
        global_waystones:pos = {'icon'->[str(icon),1]};
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

_mark_player_waypoint(p, positions) -> (
    global_waypoints:(p~'uuid'):pos = map(p~'pos', floor(_) + 0.5);
);

_open_waypoint_screen(p) -> (
    // create_screen(player, type, name, callback?)
);

// Proper tp 
// p = player(); 
// pos = [800,66,-9920]; 
// Load chunks for 5 ticks and tp player in 3
// add_chunk_ticket(pos, 'teleport', 2); 
// schedule( 3, _(p, pos)->(modify(p, 'pos', pos)), p, pos)

// _tp(p, pos) -> modify(p, 'pos', pos); 
// schedule( 3, '_tp', p, pos)
// teleport modify(p, 'pos', map(p~'pos', floor(_) + 0.5))

_write_player_waypoints(p) -> (
    _write_pos_map_file(str('players/%s', p~'uuid'), global_waypoints:p~'uuid');
    delete(global_waypoints:p~'uuid');
);

_read_player_waypoints(p) -> (
    global_waypoints:p~'uuid' = _read_pos_map_file(str('players/%s', p~'uuid'));
);

_write_waystones() -> (_write_pos_map_file('waystones', global_waystones));

_read_waystones() -> (global_waystones = _read_pos_map_file('waystones'));

// keep x,y,z keys intact within json format by converting root map to array and back
_write_pos_map_file(path, map_data) -> (
    write_file(path, 'json', pairs(map_data));
);

_read_pos_map_file(path) -> (
    data = read_file(path, 'json');
    if( data, m(...data), {} );
);

_get_name_from_nbt(nbt) -> (
    // turn '"Foo"' or '{"text":"Foo"}' into 'Foo' If
    name = decode_json(nbt:'display':'Name');
    return(name:'text' || name);
);
