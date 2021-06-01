// shows biomes blockwise. useful for marking out rivers, beaches, soul sand valley and stuff.
// now works across diemsions (yes even in the_end)
__config()->{
    'stay_loaded'->true,
    'scope' -> 'global',
    'commands' -> {
        '' -> _() -> print(format('r Missing arguments. Correct syntax /showbiomes <toggle>')),
        '<toggle>' -> l('__toggle_markings'),
        'print' -> _() -> print(global_players)
    },  
    'arguments' -> {
        'toggle' -> {'type'->'text','options' -> l('on', 'off')},
    }
};

// if the script crashes your multiplayer world, do one of the two
// 1. update carpet to latest version
// 2. turn this to true 
global_carpet_crash_fix = false;

// change this global_radius to improve fps if you on a potato. 4 works decently
global_radius = 4;

// change the font size to your liking
global_font_size = 6;

// dont change anything this point onward
global_players = m();
global_dimension_map = m('overworld' -> 'foliage_color', 'the_nether' -> 'fog_color', 'the_end' -> 'sky_color');

__make_markers(player) -> (
    l(posx, posy, posz)  = pos(player);
    scan(posx, posy, posz, -global_radius, 0, global_radius,
        l(x, y, z) = pos(_);
        draw_shape(
            'box', 
            2,
            'color', 0x00000000,
            'fill', if(global_carpet_crash_fix, 0x808080FF, biome(_, global_players:player:'dim_color')) - 0x0000009F, 
            'from', l(x, y + 0.05, z), 
            'to', l(x + 1, y - 0.05, z + 1),
        );

        draw_shape(
            'label', 
            2,
            'color', 0xFFFFFFFF,
            'text', biome(_),
            'pos', l(x + 0.5, y, z + 0.5),
            'size', global_font_size,
        );
    );
);

__add_player(player) -> (
    global_players = global_players + {player -> {'dim_color' -> global_dimension_map:(player ~ 'dimension'), 'toggle' -> false}};
);

__toggle_markings(toggle) -> (
    p = player();
    if(!global_players:p, __add_player(p));
    global_players:p:'toggle' = toggle == 'on';
);

__on_player_connects(player) -> (
    __add_player(player);
);

__on_player_disconnects(player, reason) -> (
    delete(global_players:player);
);

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension)->(
    if (global_players:player, global_players:player:'dim_color' = global_dimension_map:to_dimension);
);

__on_tick() -> (
    for(global_players, if (global_players:_:'toggle', schedule(0, '__make_markers', _)));
);
