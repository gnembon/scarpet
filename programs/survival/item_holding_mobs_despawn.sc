// changes despawn rule for monster mobs. 
// makes mobs who hold items despawnable as opposed to vanilla where they will never despawn.
// mobs can only be made persistent when they are given a custom name using nametags
// By: Crec0

__config() -> {
    'stay_loaded' -> true,
    'scope' -> 'global'
};

entity_load_handler('monster', 
    _(e, new) -> entity_event(e, 'on_tick', 
        _(e) -> ( 
            if(e ~ 'persistence' && e ~ 'holds' && ! e ~ 'custom_name', 
                modify(e, 'persistence', false)
            )
        )
    )
);
