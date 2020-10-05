//Bridge maker, right click with golden sword and blocks in offhand to start placing.
//Video: https://youtu.be/6QxQXymqRoA
//By: Ghoulboy and Aplet123

__config() ->

(

	m(

		l('stay_loaded', true),

		l('scope', 'player') //So that you can't accidentlly load it in global mode and mess it up

		)

);
//Global variables

global_bridge_tool='golden_sword';

global_is_survival=false;

global_shift=false;

//Events

__on_player_uses_item(player, item_tuple, hand)->(
    if(hand=='mainhand' && item_tuple:0==global_bridge_tool,
        bridging_block=query(player,'holds','offhand'):0;
        if(!bridging_block,return(print('No bridging block selected.')));
        global_shift=player~'sneaking';
        limit=if(global_shift,query(player,'holds','offhand'):1,
            limit=0;
            for(inventory_get(player),if(_:0==bridging_block,limit+=_:1));
            limit
        );
        facing=if(player~'facing'!='up',
            player~'facing',//Don't want to start placing blocks upwards or we'll suffocate.
            query(player,'facing',1)
        );
        global_is_survival=!(player~'gamemode_id' % 2);
        __start_placing(pos_offset(pos(player)-l(0,1,0),facing),bridging_block,limit,facing)
    )
);

//Other functions

__start_placing(pos,item,limit,facing)->(
    if(limit==0,return(print('Finished placing bridge!')));
    player=player();
    if(place_item(item,pos,facing),
        if(global_is_survival,
            if(global_shift,//Cos then ur only calculating offhand items
                inventory_set(player,40,query(player,'holds','offhand'):1-1),
                inventory_remove(player,item)
            )
        )
    );
    schedule(1,'__start_placing',pos_offset(pos,facing),item,limit-1,facing)
)
