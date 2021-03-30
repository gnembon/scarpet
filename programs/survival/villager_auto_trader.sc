//Zombie villagers will trade whatever trades they have (cos they keep them now).
//By: Ghoulboy

__config()->{'scope'->'global','stay_loaded'->true};

global_trade_speed=120;//Trade every 120 ticks, so u have time to retrieve item. Same as piglin bartering, so seemed fair

__on_tick()->(
    if(tick_time()%global_trade_speed==2,
        for(filter(entity_selector('@e[type=zombie_villager]'),nbt(_~'nbt':'HandItems'):'[]':0:'id'=='minecraft:emerald'),
            hand_count=nbt(_~'nbt':'HandItems'):'[]':0:'Count';
            if((trade_list = nbt(_~'nbt':'Offers':'Recipes'):'[]')==0,continue());
            if(type(trade_list)!='list',trade_list=[trade_list]);
            trade=first(trade_list,
                _:'buy':'id'=='minecraft:emerald' &&//only emerald trades
                _:'buyB':'id'=='minecraft:air' &&//don't want second trade item
                _:'maxUses'!=0 &&//Cos if not it's cheaty, dont work rn
                (cost=_:'buy':'Count')<=hand_count//Need to have enough emeralds
            );
            if(!trade,continue());

            modify(_,'nbt_merge',str('{HandItems:[{id:"minecraft:emerald",Count:%sb}]}',hand_count-cost));

            spawn('item',pos(_),str('{Item:{id:"%s",Count:%db,PickupDelay:0}}',trade:'sell':'id',trade:'sell':'Count'));
        )
    )
)
