_crouch_grow_tick() -> 
(
    for(filter(player('*'), _ ~ 'sneaking'),
        if (tick_time()%4 == 0,
            l(x, y, z) = pos(_);
            scan(x, y, z, 8, 8, 8,
                if (material(_) == 'plant',
                    particle('happy_villager', _, 2, 0.4);
                    random_tick(_);
                )
            )
        )
    )
)
