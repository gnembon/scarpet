//For gnembon
//Basically, this app allows you to trade with zombie villagers that a) had trades before converting and b) can hold items
//This only allows for trades which take emeralds and spit out something else, as it was OP enough already.
//It also launches items at closest player within 5m radius, regardless of line of sight
//By: Ghoulboy

import('math', '_euclidean_sq', '_vec_length');

__config()->{'scope'->'global'};

global_trade_speed=120;//Trade every 120 ticks, so u have time to retrieve item. Same as piglin bartering, so seemed fair

entity_load_handler('zombie_villager', _(e, new) -> _trade(zombie_villager, null));


_trade(zombie_villager, trade)->(
    if(trade==null,//call this again in case the player messes with the zombie villager's nbt, also cos too lazy to shift elsewhere
        if((trade_list = nbt(zombie_villager~'nbt':'Offers':'Recipes'):'[]')==0, return());//this return call would only be called once, so it's not expensive
        trade=first(trade_list,
            _:'buy':'id'=='minecraft:emerald' &&//only emerald trades
            _:'buyB':'id'=='minecraft:air' &&//don't want second trade item
            _:'maxUses'!=0 //Cos if not it's cheaty, dont work rn
        );
        if(!trade, return())//again, this is a cheap call and only called once
    );

    if(nbt(zombie_villager~'nbt':'HandItems'):'[]':0:'id'=='minecraft:emerald' && (hand_count=nbt(zombie_villager~'nbt':'HandItems'):'[]':0:'Count') >= ((cost=trade:'buy':'Count')),

        modify(zombie_villager,'nbt_merge',str('{HandItems:[{id:"minecraft:emerald",Count:%sb}]}',hand_count-cost));

        item = null;
        in_dimension(zombie_villager,
            item_pos = pos(zombie_villager)+[0, zombie_villager~'eye_height'-0.3, 0]; 
            item = spawn('item',item_pos,str('{Item:{id:"%s",Count:%db, PickupDelay:10}}',trade:'sell':'id',trade:'sell':'Count'));
        );

        nearest_player = null;
        distance_sq_to_nearest_player=25;

        for(entity_area('player', item_pos, [5,5,5]),
            distance_sq = _euclidean_sq(item_pos, pos(_));
            if(distance_sq<distance_sq_to_nearest_player,
                nearest_player = _;
                distance_sq_to_nearest_player = distance_sq
            )
        );

        item_motion = if(nearest_player!=null,
            (pos(nearest_player)-item_pos)/(_vec_length(pos(nearest_player)-item_pos)*10),
            zombie_villager~'look'/10
        );
        modify(item, 'motion', item_motion)
    );

    schedule(global_trade_speed, '_trade', zombie_villager, trade)
);

for(entity_list('zombie_villager'), _trade(_, null));
