// Allow a Totem of Undying anywhere in the inventory to activate

__on_player_takes_damage(player, amount, source, source_entity) -> (
	totem = 'totem_of_undying';
	if(query(player, 'health') <= amount
	&& source != 'outOfWorld'
	&& query(player, 'holds', 'mainhand'):0 != totem
	&& query(player, 'holds', 'offhand'):0 != totem
	&& inventory_find(player, totem) != null,
		// player offhand has an index of 40 instead of -1 at this moment
		// nothing in offhand
		if(inventory_get(player, 40) == null,
			inventory_set(player, 40, 1, totem);
			schedule(0, _(player) -> (
				if(inventory_get(player, 40) == null, inventory_remove(player, 'totem_of_undying'));
				inventory_set(player, 40, 0)
			), player);
			return()
		);
		// borrow the offhand slot when there's something in offhand
		if(stack = inventory_set(player, 40, 1, totem),
			schedule(0, _(outer(player), outer(stack)) -> (
				if(inventory_get(player, 40) == null, inventory_remove(player, 'totem_of_undying'));
				inventory_set(player, 40, stack:1, stack:0, stack:2)
			));
		)
	)
)
