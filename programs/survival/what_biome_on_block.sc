// shows biomes blockwise. useful for marking out rivers, beaches, soul sand valley and stuff.
// now works across diemsions (yes even in the_end)
__config()->{
    'stay_loaded'->true,
    'commands' -> {
        '' -> _() -> print(format('r Missing arguments. Correct syntax /showbiomes <toggle>')),
        '<toggle>' -> l('__toggle_markings'),
    },  
    'arguments' -> {
        'toggle' -> {'type'->'text','options' -> l('on', 'off')},
    }
};

// change this global_radius to improve fps if you on a potato. 4 works decently
global_radius = 4;
global_font_size = 6;

// turn this to true if the script crashes your multiplayer world
global_carpet_crash_fix = false;

//don't change this. 
global_active_players = l();

__make_markers(data) -> (
    l(posx, posy, posz)  = pos(data:0);
    scan(posx, posy, posz, -global_radius, 0, global_radius,
        l(x, y, z) = pos(_);

        draw_shape(
            'box', 2,
            'color', 0x00000000,
            'fill', if(global_carpet_crash_fix, 0x808080FF, biome(_, data:2)) - 0x0000009F, 
            'from', l(x, y + 0.05, z), 
            'to', l(x + 1, y - 0.05, z + 1),
        );

        draw_shape(
            'label', 2,
            'color', -1,
            'text', biome(_),
            'pos', l(x + 0.5, y, z + 0.5),
            'size', global_font_size,
        );
    );
);

__add_player(player) -> (
    player_data = l(player, false, __parse_dim(player ~ 'dimension'));
    global_active_players += player_data;
    return(player_data);
);

__parse_dim(dimension) -> (
    if(
        dimension == 'overworld', return('foliage_color'), 
        dimension == 'the_nether', return('fog_color'), 
        dimension == 'the_end', return('sky_color')
    );
);

__toggle_markings(toggle) -> (
    p = player();
    player_data = first(global_active_players, _:0 == p);
    if(!player_data,
        player_data = __add_player(p)
    );
    player_data:1 = toggle == 'on';
);

__on_player_connects(player) -> (
    __add_player(player);
);

__on_player_disconnects(player, reason) -> (
    delete(global_active_players, player);
);

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension)->(
    first(global_active_players, _:0 == player):2 = __parse_dim(to_dimension);
);

__on_tick()-> (
    for(global_active_players, if (_:1, schedule(0, '__make_markers', _)));
);