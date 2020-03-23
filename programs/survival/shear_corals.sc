//Allows player to shear coral block of any type (even dead) to get between 0-5 fans or corals
//By Ghoulboy

__on_player_breaks_block(p
layer, block) ->(
	coraltype=block-'_coral_block';//Cos if it doesnt have _coral_block in its name, its not a coral type, dead or otherwise, and nothing will change in it
	if(coraltype!=block&&player~'holds':0=='shears'&&player~'gamemode'=='survival',
		itempos=pos(block)+l(0.5,0.5,0.5);
		loop(rand(6),
			if(rand(2),
				itemtype=coraltype+'_coral',
				itemtype=coraltype+'_coral_fan'
			);
			run('summon minecraft:item '+(str(itempos)-'['-','-','-']')+' {PickupDelay:10,Item:{id:"minecraft:'+itemtype+'",Count:1b}}');
		);
	);
);
