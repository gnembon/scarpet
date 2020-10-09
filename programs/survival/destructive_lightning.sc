
//script that makes lightning slightly more destructive

__command() -> (
    print(format('b \nDestructive Lighting'));
    print('--------------------------------');
    print('To enable destructive lightning:');
    print(format(' /destructive_lightning start\n', '^ Click to start', '!/destructive_lightning start'));
    print('To disable destructive lightning:');
    print('Reload this script\n')
);


start() -> (
    print('Started Destructive Lightning');
    last = 1; //uuid of last bolt
    loop(inf,
        game_tick(50);
        e = entity_list('lightning_bolt'); //finds all lightning bolts
        for(e,
            look = l(0.0, -1.0, 0.0);
            fireball_pos = (_ ~ 'pos')+look;  //finds lightning bolt position
            if (last != query(_, 'uuid'),  //only summons one fireball per bolt
                spawn('tnt', (_ ~ 'pos'));
                game_tick(50);
                fireball = spawn('fireball', fireball_pos,
                str('{power:[%.2f,%.2f,%.2f],direction:[0.0,0.0,0.0]},ExplosionPower:5',look/5)); //summons fireball
            last = query(_, 'uuid');
            );
        );
    );
);