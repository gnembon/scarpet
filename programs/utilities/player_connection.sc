//gnembon/fabric-carpet #294
//By: Ghoulboy

//Global variables
global_players=l();

global_new_players=l();

global_left_players=l();

//Events
__on_tick()->(
    players=entity_selector('@e[type=player]');
    for(players,//Cos if not it will only check in that dimension
        if(global_players~_==null,//Checks for new players. When it's loaded, will print all currently logged on players.
            print('Welcome, '+_);
            global_players:length(global_players)=_;
            put(global_new_players,null,_),
            delete(global_new_players:(global_new_players~_))
        )
    );

    for(global_players,//Cos if not it will only check in that dimension
        if(players~_==null,//Checks for players left.
            print('Good bye, '+_);
            delete(global_players:(global_players~_));
            put(global_left_players,null,_),
            delete(global_left_players:(global_left_players~_))
        )
    );
)
