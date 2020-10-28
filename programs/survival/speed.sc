//Speed display
//By: Ghoulboy

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

import('math','_euclidean');

//Funcs

__setup_player(player)->(
    global_prevpos:player=pos(player);
    global_pos:player=pos(player);
    entity_event(player,'on_tick','__display')
);

__display(player)->(
    if(tick_time()%global_refresh_rate==0,
        global_pos:player=pos(player);

        speed=_euclidean(global_pos:player,global_prevpos:player)*20/global_refresh_rate;
        scoreboard('Speed',player,roundmath(speed*1000,1));

        global_prevpos:player=pos(player)
    )
);

__on_player_connects(player)->__setup_player(player);

roundmath(num,precision)->return(round(num/precision)*precision);

//Initial code

scoreboard_add('Speed');
scoreboard_display('sidebar','Speed');
global_prevpos={};
global_pos={};
global_refresh_rate=20;


print('Divide number on the side of the screen by 1000 to get actual speed in m/s!');

for(player('*'),
    __setup_player(_)
);
