__config()->{
    'scope'->'global',
    'stay_loaded'->true
};
create_datapack('invupd', 
    {
        'readme.txt' -> ['this data pack is created by scarpet','please dont touch it'],
        'data' -> {
            'chyx/advancements/xd.json'-> {
                'rewards' -> {'function' -> 'chyx:invupd'},
                'criteria' -> {
                    'example' -> {
                        'trigger' -> 'minecraft:inventory_changed',
                        'conditions' -> {}
                    }
                }
            },
            'chyx/functions/invupd.mcfunction' -> 'script run signal_event(\'invupd\', null, player())\nadvancement revoke @s only chyx:xd'
        }
    }
);
global_nope=nbt('{nope:nopeChYx'+rand(1)+'nope}');

global_slotmap=[[-1,7],[-2,1],[-3,2],[-4,3],[-5,4],...map(range(9),[_,45+_]),...map(range(27),[9+_,18+_])];


global_fakeplayersscreen={};

__on_player_interacts_with_entity(creativeplayer, fakeplayer, hand)->(
    if(
        hand != 'mainhand', return(),
        fakeplayer~'player_type' != 'fake', return(),
        creativeplayer~'player_type' == 'fake', return(),
        //creativeplayer~'gamemode' != 'creative', return(),
        global_fakeplayersscreen:fakeplayer, return()
    );
    

    screen=create_screen(creativeplayer,'generic_9x6',fakeplayer~'name',_(screen, player, action,data,outer(fakeplayer))->(
        
        if(action=='close',(
                    //screentoplayer(fakeplayer,screen);
                    drop_item(screen,-1);
                    close_screen(screen);//end_portal/die
                    return('cancel')
        ));
        if(action=='slot_update'
        &&0<=data:'slot'
        &&data:'slot'<54
        ,screentoplayer(fakeplayer,screen);return());
        if(data:'slot'==null,return());
        if(inventory_get(screen, data:'slot'):2==global_nope,return('cancel'));
        
        
    ));

    global_fakeplayersscreen:fakeplayer=screen;

    loop(54,inventory_set(screen,_, 1, 'minecraft:structure_void',global_nope));
    //inventory_set(screen,fakeplayer~'selected_slot'+9, 1, 'minecraft:barrier',global_nope);
    playertoscreen(fakeplayer,screen);

);

playertoscreen(fakeplayer,screen)->(
    for(global_slotmap,(
        [playerslot,screenslot]=_;
        itemtup=inventory_get(fakeplayer, playerslot);
        if(itemtup==null,itemtup=['apple',0,null]);
        [item,count,nbt]=itemtup;
        
        inventory_set(screen,screenslot, count, item, nbt);
    ))
);
screentoplayer(fakeplayer,screen)->(
    for(global_slotmap,(
        [playerslot,screenslot]=_;
        itemtup=inventory_get(screen, screenslot);
        if(itemtup==null,itemtup=['apple',0,null]);
        [item,count,nbt]=itemtup;
        
        inventory_set(fakeplayer,playerslot, count, item, nbt);
    ));
);

handle_event('invupd',_(fakeplayer)->(
    screen=global_fakeplayersscreen:fakeplayer;
    if(screen,playertoscreen(fakeplayer,screen));
));


//__on_player_switches_slot(fakeplayer, from, to)->(
//    screen=global_fakeplayersscreen:fakeplayer;
//    if(screen,(
//        inventory_set(screen,from+9, 1, 'minecraft:structure_void',global_nope);
//        inventory_set(screen,to  +9, 1, 'minecraft:barrier',global_nope);
//    ))
//);


__on_player_disconnects(fakeplayer, reason)->(
    screen=global_fakeplayersscreen:fakeplayer;
    if(screen,(
        drop_item(screen,-1);
        close_screen(screen);
    ))
)
