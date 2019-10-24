global_bridge_item = 'golden_sword';
global_bridge_lengths = l(2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 50, 200);

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

__remove_offhand(player) ->
(
	holds_offhand = query(player, 'holds', 'offhand');
	inventory_set(player, -1, holds_offhand:1 - 1)
);

__distance(v1, v2) -> sqrt(reduce(v1-v2,_*_+_a,0));

__get_nbt_value_in_slot(player, slot, tag_name) ->
(
	item_nbt = inventory_get(player, slot):2;
	if (item_nbt == null, return(m()));
	nbt(item_nbt):tag_name
);

__set_nbt_in_slot(player, slot, tag_name, tag_value) ->
(
	l(item_name, count, item_nbt) = inventory_get(player, slot);
	item_nbt = nbt(item_nbt);
	if (item_nbt == null, item_nbt = m());
	put(item_nbt, tag_name, tag_value);
	inventory_set(player, slot, count, item_name, item_nbt)
);

__disp_num(player, num) -> (
	run('tellraw ' + player ~ 'name' + ' [{"text":"Current Bridge Length: ","bold":true,"color":"gold"},{"text":"' + num + '","bold":true,"color":"yellow"}]')
);

__place_block(player, posi, face, num) ->
(
	holds_offhand = query(player, 'holds', 'offhand');
	if (!holds_offhand, return());
	if (num <= 0, return());
	l(x, y, z) = posi;
	if (!place_item(holds_offhand:0, x, y, z), return());
	if (player ~ 'gamemode' != 'creative', __remove_offhand(player));
	// don't want player to get stuck when bridging up
	if (__distance(pos(player), posi) < 1 && face == 'up', modify(player, 'move', 0, 1, 0));
	schedule(5, '__place_block', player, pos_offset(posi, face, 1), face, num - 1)
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if (hand != 'mainhand', return());
	if (__can_bridge(player), (
		slot = player ~ 'selected_slot';
		bridge_length = __get_nbt_value_in_slot(player, slot, 'BridgeLength');
		if (bridge_length == null, bridge_length = 0);
		__place_block(player, pos_offset(pos(block), face, 1), face, global_bridge_lengths:bridge_length)
	))
);

__on_player_clicks_block(player, block, face) ->
(
	if (!(query(player, 'holds', 'mainhand'):0 ~ global_bridge_item), return());
	slot = player ~ 'selected_slot';
	if (__get_nbt_value_in_slot(player, slot, 'BridgeLength') == null, __set_nbt_in_slot(player, slot, 'BridgeLength', nbt('0')));
	bridge_length = __get_nbt_value_in_slot(player, slot, 'BridgeLength') + 1;
	if (bridge_length == length(global_bridge_lengths), bridge_length = 0);
	__set_nbt_in_slot(player, slot, 'BridgeLength', bridge_length);
	__disp_num(player, global_bridge_lengths:bridge_length) 
);
