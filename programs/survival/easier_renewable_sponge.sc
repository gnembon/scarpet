//killing one of each type of fish gives a random chance to get sponge
//By: Ghoulboy

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

global_debug=false;//for debugging purposes only

__on_player_attacks_entity(player,entity)->(
	if(global_fishez : str(entity)!=null,
		schedule(0,'__add_dead_fish',entity)
	)
);

global_fishez = {
    'Pufferfish'->0,
    'Tropical Fish'->0,
    'Salmon'->0,
    'Cod'->0
};

__add_dead_fish(entity)->(
	if(entity ~ 'health'==0,
	    global_fishez:str(entity)+=1;
	);

    if(for(global_fishez,global_fishez:_ >=1)>=4 && (!rand(50)||global_debug==true),
        spawn('item',pos(entity),str('{Item:{id:"minecraft:%ssponge",Count:1b}}',if(block(pos(entity))=='water','wet_','')));
        for(global_fishez,global_fishez:_=global_fishez:_-1)
    )
)
