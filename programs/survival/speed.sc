roundmath(num,precision)->return (round(num/precision)*precision);

//scoreboard_add('Speed');
scoreboard_display('sidebar','Speed');
global_prevpos=l(0,0,0);
global_pos=l(0,0,0);
print('Divide number on the side of the screen by 100 to get actual speed in m/s!');
__display(player)->(
    global_pos=pos(player);
    speed=(speed+speed(global_pos,global_prevpos))/2;
    if(global_true,print(roundmath(speed*20,0.01)));
    scoreboard('Speed',player,roundmath(speed*2000,0.01));
    global_prevpos=pos(player);
);

for(entity_selector('@e[type=player]'),
    global_prevpos=pos(p);
    global_pos=pos(p);
    entity_event(_,'on_tick','__display')
);

speed(pos1,pos2)->(
    l(dx,dy,dz)=pos1-pos2;
    return(sqrt(dx*dx+dy*dy+dz*dz))
);

global_true=false;