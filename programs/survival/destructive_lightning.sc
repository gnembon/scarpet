
//script that makes lightning slightly more destructive

__config() -> m( l('scope', 'global'));


__command() -> (
    print(format('b \nDestructive Lighting'));
    print('--------------------------------');
    print('To enable destructive lightning:');
    print(format(' /destructive_lightning start\n', '^ Click to start', '!/destructive_lightning start'));
    print('To disable destructive lightning:');
    print(format(' /destructive_lightning stop\n', '^ Click to stop', '!/destructive_lightning stop'))
);


__on_tick() -> (
    if(global_started == 1,
    e = entity_list('lightning_bolt'); //finds all lightning bolts
        for(e,
            look = l(0.0, -1.0, 0.0);
            fireball_pos = (_ ~ 'pos')+look;  //finds lightning bolt position
            if (global_last != query(_, 'uuid'),  //only summons one fireball per bolt
                spawn('tnt', (_ ~ 'pos'));
                fireball = spawn('fireball', fireball_pos,
                str('{power:[%.2f,%.2f,%.2f],direction:[0.0,0.0,0.0]},ExplosionPower:5',look/5)); //summons fireball
            global_last = query(_, 'uuid');
            );
        );
    );
);


start() -> (
    print('Enabled Destructive Lightning');
    global_last = 1; //uuid of last bolt
    global_started = 1;
);

stop() -> (
    print('Disabled Destructive Lightning');
    global_started = 0;
);
