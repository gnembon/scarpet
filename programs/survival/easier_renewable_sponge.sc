__on_player_attacks_entity(player,entity)->(
	if(global_fishez ~ entity,
		schedule(0,'__add_dead_fish',entity);//if not the fish's health may not yet be calculated, and I cant check if its dead or not
	);
);
global_puff=0;
global_trop=0;
global_salmon=0;
global_cod=0;
global_fishez = l('','Pufferfish', 'Tropical Fish', 'Salmon','Cod');


__add_dead_fish(entity)->(
	if(entity ~ 'health'==0,
		if (entity=='Pufferfish',
			global_puff+=1;
		);
		if (entity=='Tropical Fish',
			global_trop+=1;
		);
		if (entity=='Salmon'&& query(entity,'health')==0,
			global_salmon+=1;
		);
		if (entity=='Cod'&& query(entity,'health')==0,
			global_cod+=1;
		);
		if(global_puff>0 && global_trop >0 && global_salmon>0 && global_cod>0&&rand(50)==0,
			run('summon minecraft:item '+(str(map(pos(entity), str('%.2f',_)))-'['-']'-','-',')+' {Item:{id:"minecraft:wet_sponge",Count:1b}}');//Why isn't there an easier way to do this?
			global_puff +=-1;
			global_trop += -1;
			global_salmon +=-1;
			global_cod +=-1;
		);
	);
);
