__config()->{
    'scope'->'global',
    'stay_loaded'->true,
    'requires' -> {
        'carpet' -> '>=1.4.173',
        'minecraft' -> '>=1.21',
    }
};
create_datapack('invupd',
    {
        'readme.txt' -> ['this data pack is created by scarpet','please dont touch it'],
        'data' -> {
            'chyx' ->{
                'advancement'->{
                    'xd.json'-> {
                        'rewards' -> {'function' -> 'chyx:invupd'},
                        'criteria' -> {
                            'example' -> {
                                'trigger' -> 'minecraft:inventory_changed',
                                'conditions' -> {}
                            }
                        }
                    }
                },
                'function'->{
                    'invupd.mcfunction' -> 'script run signal_event(\'invupd\', null, player())\nadvancement revoke @s only chyx:xd'
                }
            }
        }
    }
);
global_nope=nbt('{nope:nopeChYx'+rand(1)+'nope}');
global_nope_barrier=nbt('{id:"minecraft:barrier",components:{custom_data:'+global_nope+'}}');
global_nope_structure_void=nbt('{id:"minecraft:structure_void",components:{custom_data:'+global_nope+'}}');

global_slotmap=[
    {'inventory' -> 'equipment', 'inventory_slot' -> 5, 'screen' -> 7}, // offhand
    {'inventory' -> 'equipment', 'inventory_slot' -> 4, 'screen' -> 1}, // head
    {'inventory' -> 'equipment', 'inventory_slot' -> 3, 'screen' -> 2}, // chest
    {'inventory' -> 'equipment', 'inventory_slot' -> 2, 'screen' -> 3}, // legs
    {'inventory' -> 'equipment', 'inventory_slot' -> 1, 'screen' -> 4}, // feet
    ...map(range(9),{'inventory' -> null, 'inventory_slot' -> _, 'screen' -> 45+_}), // hotbar
    ...map(range(27),{'inventory' -> null, 'inventory_slot' -> 9+_, 'screen' -> 18+_}) // inventory
];


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
            drop_item(screen,-1);
            global_fakeplayersscreen:fakeplayer = null;
            return()
        ));
        if(data:'slot'==null,return());
        if(action=='pickup' && 9<=data:'slot' && data:'slot'<18,
            // CARPET BUG 1.4.66 - Modify not working for fake players:
            // modify(fakeplayer,'selected_slot', data:'slot'-9);
            // FIX:
            run('player ' + fakeplayer + ' hotbar ' + (data:'slot'-8))
            // END FIX
        );
        if(inventory_get(screen, data:'slot'):2:'components':'minecraft:custom_data' == global_nope,
            return('cancel')
        );
        if(action=='slot_update' && 0<=data:'slot' && data:'slot'<54,
            screentoplayer(fakeplayer,screen)
        )
    ));

    global_fakeplayersscreen:fakeplayer=screen;

    loop(54,inventory_set(screen,_, 1, 'minecraft:structure_void',global_nope_structure_void));
    inventory_set(screen,fakeplayer~'selected_slot'+9, 1, 'minecraft:barrier',global_nope_barrier);
    playertoscreen(fakeplayer,screen)
);

playertoscreen(fakeplayer,screen)->(
    for(global_slotmap,(
        itemtup=inventory_get(_:'inventory', fakeplayer, _:'inventory_slot');
        if(itemtup==null,itemtup=['apple',0,null]);
        [item,count,nbt]=itemtup;
        
        inventory_set(screen, _:'screen', count, item, nbt);
    ))
);
screentoplayer(fakeplayer,screen)->(
    for(global_slotmap,(
        itemtup=inventory_get(screen, _:'screen');
        if(itemtup==null,itemtup=['apple',0,null]);
        [item,count,nbt]=itemtup;
        
        inventory_set(_:'inventory', fakeplayer, _:'inventory_slot', count, item, nbt);
    ));
);

handle_event('invupd',_(fakeplayer)->(
    screen=global_fakeplayersscreen:fakeplayer;
    if(screen,playertoscreen(fakeplayer,screen));
));

// CARPET BUG 1.4.66 - Event not working for fake players:
// __on_player_switches_slot(fakeplayer, from, to)-> if(player ~ 'player_type' == 'fake',
// FIX:
global_fakeplayers_selected_slot = {};
__on_tick() ->
for(filter(player('all'), _~'player_type' == 'fake'),
    selected_slot = _ ~ 'selected_slot';
    if((old_slot = global_fakeplayers_selected_slot:_) != null &&
        old_slot != selected_slot,
        __fakeplayer_switches_slot(_, old_slot, selected_slot)
    );
    global_fakeplayers_selected_slot:_ = selected_slot
);

__fakeplayer_switches_slot(fakeplayer, from, to)->(
// END FIX
    screen=global_fakeplayersscreen:fakeplayer;
    if(screen,(
        inventory_set(screen,from+9, 1, 'minecraft:structure_void',global_nope_structure_void);
        inventory_set(screen,to  +9, 1, 'minecraft:barrier',global_nope_barrier);
    ))
);

__on_player_disconnects(fakeplayer, reason)->(
    screen=global_fakeplayersscreen:fakeplayer;
    if(screen,(
        drop_item(screen,-1);
        close_screen(screen);
    ))
)
