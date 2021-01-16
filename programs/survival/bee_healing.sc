// Bees have an annoying habit of drowning themselves, so yeah
__config()->{'stay_loaded' -> true, 'scope' -> 'global'};

begin()->(
    bees = entity_selector('@e[type=bee]');
    for (bees, 
        health = _~'health';
        if (health < 10,
            modify(_, 'health', health+1);
        );
    );
    schedule(800, 'begin');
);

begin();