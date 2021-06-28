// Bees have an annoying habit of drowning themselves, so yeah
__config()->{'scope' -> 'global'};

global_incrementer = 0;
global_bees = {};

add_entity_event(bee) -> (
    entity_event(bee, 'on_removed', _(entity, isNew, i) -> (
        delete(global_bees, i);
    ), copy(global_incrementer));
);

for (entity_selector('@e[type=bee]'),
    put(global_bees, global_incrementer, _);

    add_entity_event(_);

    global_incrementer += 1;
);

begin()->(
    for (global_bees,
        e = global_bees : _;
        health = e ~ 'health';
        if (health < 10,
            modify(e, 'health', health+1);
        );
    );
    schedule(800, 'begin');
);

entity_load_handler('bee', _(bee, isNew) -> (
    put(global_bees, global_incrementer, bee);
    
    add_entity_event(bee);

    global_incrementer += 1;
));

begin();