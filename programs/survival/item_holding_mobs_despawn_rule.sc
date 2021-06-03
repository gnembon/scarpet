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

__on_tick()->(
    if (tick_time() % 20,  schedule(0, '__change_persistence'));
);
