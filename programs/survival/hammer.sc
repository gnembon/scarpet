//!scarpet v1.5

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__command()->(
	global_active = !global_active;
	if(global_active,
		display_title(player(), 'actionbar', format('yi Turned on hammer')),
		display_title(player(), 'actionbar', format('yi Turned off hammer'))
	);
	null
);

__get_block_range(position, face) ->  (
	l(x,y,z) = position;
	block_range = if (
		face == 'up' || face == 'down', rect(x,y,z,global_radius,0,global_radius ),
		face == 'north' || face == 'south', rect(x,y,z,global_radius,global_radius,0),
		face == 'east'  || face == 'west', rect(x,y,z,0,global_radius,global_radius)
	);
	return(filter(block_range, __harvestable(_)))
);
__harvestable(block) -> !(air(block) || (block == 'lava') || (block == 'water'));

global_active = false;
global_radius = 1;
global_breakrange = null;

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
	if(global_active && hand == 'mainhand' && item_tuple && item_tuple:0 =='stone_pickaxe',
		global_radius = (global_radius + 1)%3;
		diameter = 2*global_radius+1;
		display_title(player, 'actionbar', format('y hammer mode '+diameter+'x'+diameter));
		if(global_radius,
			for(__get_block_range(pos(block), face), 
				particle('block '+block(_), _, 50, 0, 0.7)
			)
		)
	);
);

__on_player_clicks_block(player, block, face) -> (
	if (global_radius && global_active,
		global_breakrange = null;
		
		item_mainhand = player ~ 'holds';
		if (!item_mainhand || !(item_mainhand:0 == 'stone_pickaxe'), return());
		global_breakrange = __get_block_range(pos(block), face);
		for(global_breakrange, particle('block '+block(_), _, 100, 0, 0.7))
	);
);

__on_player_breaks_block(player, block) -> (
	block_range = global_breakrange;
	global_breakrange = null;
	if (global_active && global_radius && block_range,
		item_mainhand = player ~ 'holds';
		if(item_mainhand && item_mainhand:0 == 'stone_pickaxe',
			for(block_range, harvest(player, _))
		)
	)
)
