// changes despawn rule for monster mobs. 
// makes mobs who hold items despawnable as opposed to vanilla where they will never despawn.
// mobs can only be made despawnable when they are given a custom name using nametags

__command()->null;

__config()->{
    'stay_loaded' -> true;
};

__change_persistence() -> (
    for ( entity_list('monster'),
        if (_ ~ 'persistence' && _ ~ 'holds' && ! _ ~ 'custom_name',
            modify(_, 'persistence', false);
        )
    )
);

global_counter = 0;

__on_tick()->(
    if (global_counter == 0,  schedule(0, '__change_persistence'));
    global_counter = (global_counter + 1) % 20;
);
