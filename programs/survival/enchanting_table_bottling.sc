global_xp_cost = 8;

__on_player_right_clicks_block(player, item, hand, block, f, h) -> (
	if(
		player~'gamemode_id'%2==0 && //survival or adventure mode
		item:0 == 'glass_bottle' &&
		item:2 == null && //doesn't have nbt
		block == 'enchanting_table' &&
		player~'sneaking' && //they didn't open the UI
		player~'xp' >= global_xp_cost, //they ahve enough xp

		modify(player, 'add_xp', -global_xp_cost);
		slot = if(hand=='mainhand', player~'selected_slot', -1);
		inventory_set(player, slot, item:1 - 1);

		run( str( 'give %s minecraft:experience_bottle', player))
	)
);