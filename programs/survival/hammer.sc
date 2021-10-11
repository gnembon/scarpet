//!scarpet v1.5

__get_block_range(position, face) -> 
(
	l(x,y,z) = position;
	block_range = if (
		face == 'up' || face == 'down', rect(x,y,z,global_radius,0,global_radius ),
		face == 'north' || face == 'south', rect(x,y,z,global_radius,global_radius,0),
		face == 'east'  || face == 'west', rect(x,y,z,0,global_radius,global_radius)
	);
	return(filter(block_range, __harvestable(_)))
);
__harvestable(block) -> !(air(block) || (block == 'lava') || (block == 'water'));

global_radius = 1;
global_breakrange = null;

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if (hand != 'mainhand' || !item_tuple || item_tuple:0 !='stone_pickaxe', return());
	global_radius = (global_radius + 1)%3;
	diameter = 2*global_radius+1;
	print('hammer mode '+diameter+'x'+diameter);
	if(global_radius,
		for(__get_block_range(pos(block), face), 
			particle('block '+block(_), _, 50, 0, 0.7)
		)
	)
);

__on_player_clicks_block(player, block, face) -> if (global_radius,
	global_breakrange = null;
	item_mainhand = player ~ 'holds';
	if (!item_mainhand || !(item_mainhand:0 == 'stone_pickaxe'), return());
	global_breakrange = __get_block_range(pos(block), face);
	for(global_breakrange, particle('block '+block(_), _, 100, 0, 0.7))
);

__on_player_breaks_block(player, block) -> 
(
	block_range = global_breakrange;
	global_breakrange = null;
	if (!global_radius || !block_range, return());
	item_mainhand = player ~ 'holds';
	if(!item_mainhand || !(item_mainhand:0 == 'stone_pickaxe'), return());
	for(block_range, harvest(player, _); signal_event('player_breaks_block', player, player, _))
)
