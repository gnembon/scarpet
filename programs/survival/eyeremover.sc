__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if(player~'pose' == 'crouching' && block_state(block,'eye') == 'true' && hand == 'mainhand' && set(pos(block),'end_portal_frame[eye=false,facing=' + block_state(block,'facing') + ']'),
		spawn('item',pos(player) + l(0,0.5,0),'{Item:{Count:1b,id:"minecraft:ender_eye"},PickupDelay:0}');
		sound('block.end_portal_frame.fill', pos(block), 1, 0.5);
		scan(pos(block),l(3,0,3),if(_ == 'end_portal',set(pos(_),'air')));
	);
);
