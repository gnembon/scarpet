// shows biome name on the blocks around the player. useful for marking out rivers, beaches, soul sand valley and stuff.

// if the script crashes your multiplayer world, do one of the two
// 1. update carpet to latest version
// 2. turn this to true 
global_carpet_crash_fix = false;

// change this global_radius to improve fps if you on a potato. 4 works decently
global_radius = 4;

__config() -> {
    'stay_loaded'->true,
    'commands' -> {
        '' -> _() -> print(format('r Missing arguments. Correct syntax /showbiome <toggle>')),
        '<toggle>' -> '__toggle_markings',
        'radius <radius>' -> _(radius) -> global_radius = radius
    },
    'arguments' -> {
        'toggle' -> {'type' -> 'text', 'options' -> ['on', 'off']},
        'radius' -> {'type' -> 'int', 'min' -> 1, 'max' -> 8, 'suggest' -> [2, 4, 8]}
    }
};

global_player = {};
global_dimension_map = {'overworld' -> 'foliage_color', 'the_nether' -> 'fog_color', 'the_end' -> 'sky_color'};

__make_markers() -> (
    [posx, posy, posz]  = pos(player());
    scan(posx, posy, posz, -global_radius, 0, global_radius,
        [x, y, z] = pos(_);
        draw_shape(
            'box', 
            2,
            'color', 0x00000000,
            'fill', if(global_carpet_crash_fix, 0x808080FF, biome(_, global_player:'dim_color')) - 0x0000009F, 
            'from', [x, y + 0.05, z], 
            'to', [x + 1, y - 0.05, z + 1],
        );

        draw_shape(
            'label',  
            2,
            'color', 0xFFFFFFFF,
            'text', biome(_),
            'pos', [x + 0.5, y, z + 0.5],
            'size', 6
        );
    );
    __check_and_render();
);

__add_player(player) -> (
    global_player = {
        'dim_color' -> global_dimension_map:(player ~ 'dimension'), 
        'toggle' -> false
    };
);

__check_and_render() -> (
    if (global_player:'toggle', schedule(0, '__make_markers'));
);

__toggle_markings(toggle) -> (
    if(!global_player, __add_player(player()));
    global_player:'toggle' = toggle == 'on';
    __check_and_render();
);

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> (
    if (global_player, global_player:'dim_color' = global_dimension_map:to_dimension);
);