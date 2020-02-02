//killing one of each type of fish gives a random chance to get sponge
//By: Ghoulboy

global_debug=false;//for debugging purposes only

__on_player_attacks_entity(player,entity)->(
	if(global_fishez ~ entity,
		schedule(0,'__add_dead_fish',entity);//if not the fish's health may not yet be calculated, and I cant check if its dead or not
	);
);

global_fishez = l('','Pufferfish', 'Tropical Fish', 'Salmon','Cod');//The empty space is only so that I can check if the item is in the list cos
global_vars=l('',global_puff,global_trop,global_salmon,global_cod);//I can't use has()

__add_dead_fish(entity)->(
	if(entity ~ 'health'!=0,return());
	poslist=global_fishez ~ entity;
	global_vars:poslist+=1;
	if(global_vars:1>=1 && global_vars:2>=1 && global_vars:3>=1 && global_vars:4>=1,
		if(!rand(50)||global_debug==true,
			run('summon minecraft:item '+(str(map(pos(entity), str('%.2f',_)))-'['-']'-','-',')+' {Item:{id:"minecraft:wet_sponge",Count:1b}}');//Why isn't there an easier way to do this?
		);
		global_vars:1 +=-1;
		global_vars:2 +=-1;
		global_vars:3 +=-1;
		global_vars:4 +=-1;
	);
);
