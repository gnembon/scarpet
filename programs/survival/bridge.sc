global_bridge_item = 'golden_sword';

__can_bridge(entity) -> 
(
	if (entity ~ 'gamemode' == 'adventure', return(false));
	if (!(entity ~ 'sneaking'), return(false));
	holds_mainhand = query(entity, 'holds', 'mainhand');
	holds_offhand = query(entity, 'holds', 'offhand');
	if (!holds_mainhand || !holds_offhand, return(false));
	if (!(holds_mainhand:0 ~ global_bridge_item), return(false));
	true
);

__get_direction_vector(face) ->
(
	if (face == 'up', return(l(0, 1, 0)));
	if (face == 'down', return(l(0, -1, 0)));
	if (face == 'north', return(l(0, 0, -1)));
	if (face == 'south', return(l(0, 0, 1)));
	if (face == 'east', return(l(1, 0, 0)));
	if (face == 'west', return(l(-1, 0, 0)));
	l(0, 0, 0)
);

__remove_offhand(player) ->
(
	holds_offhand = query(player, 'holds', 'offhand');
	inventory_set(player, -1, holds_offhand:1 - 1)
);

__place_block(player, pos, dir, num) ->
(
	holds_offhand = query(player, 'holds', 'offhand');
	if (!holds_offhand, return());
	if (num <= 0, return());
	l(x, y, z) = pos;
	if(!place_item(holds_offhand:0, x, y, z), return());
	__remove_offhand(player);
	schedule(5, '__place_block', player, pos + dir, dir, num - 1)
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if (hand != 'mainhand', return());
	if(__can_bridge(player), (
		dir = __get_direction_vector(face);
		__place_block(player, pos(block) + dir, dir, 5)
	))
);
