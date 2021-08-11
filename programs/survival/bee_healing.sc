// Bees have an annoying habit of drowning themselves, so yeah
__config()->{
    'stay_loaded' -> true,
    'scope'->'global'
};

track_health(bee, new) -> if (!bee ~ 'removed',
    health = bee ~ 'health';
    if(health < 10, modify(bee, 'health', health + 1));
    schedule(800, 'track_health', bee, false)
);

for(entity_selector('@e[type=bee]'),
    track(_, false);
);

entity_load_handler('bee', 'track_health');
