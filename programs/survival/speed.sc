//Speed display
//By: Ghoulboy

// stay loaded
__config() -> {
   'stay_loaded'->true
};

import('math', '_euclidean', '_round');

__command() -> if(global_active = !global_active,
    display_title(player(), 'actionbar', 'Speed display active');
    global_pos = pos(player());
    display_speed(),
    display_title(player(), 'actionbar', 'Speed display inactive')
);

global_active = false;
global_refresh_rate = 5; //refresh once every 5 ticks
global_pos = null;
scoreboard_add('Speed');
scoreboard_display('sidebar','Speed');
display_title(player(), 'actionbar','Divide number on the side of the screen by 1000 to get actual speed in m/s!');

display_speed()->(
    distance = _euclidean(global_pos, pos(player()));
    speed = distance * 20 / global_refresh_rate;
    //display_title(player(), 'title', '', 0, global_refresh_rate, global_refresh_rate/2+1);
    //display_title(player(), 'subtitle', _round(speed, 0.001), 0, global_refresh_rate, global_refresh_rate/2+1);
    scoreboard('Speed',player,_round(speed*1000,1));
    global_pos = pos(player());
    if(global_active,
        schedule(global_refresh_rate, 'display_speed')
    )
);

__on_server_shuts_down()-> scoreboard_remove('Speed');
