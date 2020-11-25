// Allow a Totem of Undying anywhere in the inventory to activate

__on_player_takes_damage(player, amount, source, source_entity) -> (
	totem = 'totem_of_undying';
	if(player~'health' <= amount
	&& source != 'outOfWorld'
	&& player~'holds':0 != totem
	&& query(player, 'holds', 'offhand'):0 != totem
	&& inventory_remove(player, totem),
		// borrow the offhand slot
		if(stack = inventory_set(player, -1, 1, totem),
			schedule(0, _(outer(player), outer(stack)) -> (
				inventory_set(player, -1, stack:1, stack:0, stack:2)
			))
		, //else (to clear the GUI glitch)
			schedule(0, _(player) -> (inventory_set(player, -1, 0)), player)
		)
	)
)
