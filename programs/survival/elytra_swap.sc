// By right-clicking with an elytra or any chestplate this script will swap the items
// By: IceWolf23

global_swappable_items = {'netherite_chestplate','diamond_chestplate','iron_chestplate','golden_chestplate','chainmail_chestplate','leather_chestplate','elytra'};


__on_player_uses_item(player, item_tuple, hand) -> (
	if ( hand == 'mainhand' && inventory_get(player, 38) != null && has(global_swappable_items, item_tuple:0),
		playerArmorDressed = inventory_get(player, 38);
		handSlot = query(player, 'selected_slot');
		
		inventory_set(player, handSlot, 1, playerArmorDressed:0, playerArmorDressed:2);
		inventory_set(player, 38, 1, item_tuple:0, item_tuple:2);
	)
);
