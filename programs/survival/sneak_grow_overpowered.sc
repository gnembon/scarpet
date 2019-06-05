_crouch_grow_tick() -> 
(
    for(filter(player('*'), _ ~ 'sneaking'),
        l(x, y, z) = pos(_);
        scan(x, y, z, 8, 8, 8,
            random_tick(_)
        )
    )
)