//Allows player to shear coral block of any type (even dead) to get between 0-5 fans or corals
//By Ghoulboy

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_breaks_block(player, block) ->(
	coraltype=block-'_coral_block';//Could do block ~'_coral_block', but I use var later to give player a new coral
	if(coraltype!=block&&player~'holds':0=='shears'&&!player~'gamemode_id'%2,
		itempos=pos(block)+[0.5,0.5,0.5];
		loop(rand(6),
			if(rand(2),
				itemtype=coraltype+'_coral',
				itemtype=coraltype+'_coral_fan'
			);
			spawn('item',itempos,'{PickupDelay:10,Item:{id:"minecraft:'+itemtype+'",Count:1b}}')
		)
	)
)
