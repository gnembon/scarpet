// Bees have an annoying habit of drowning themselves, so yeah
__config()->{'scope' -> 'global'};

global_bees = entity_selector('@e[type=bee]');

begin()->(
    for (global_bees,
        if (_ ~ 'removed',
            delete(global_bees, _i);
        ,
            health = _~'health';
            if (health < 10,
                modify(_, 'health', health+1);
            );
        );
    );
    schedule(800, 'begin');
);

entity_load_handler('bee', _(bee) -> (
    put(global_bees, null, bee);
));

begin();