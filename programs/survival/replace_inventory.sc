// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_uses_item(player, item_tuple, hand) -> if (hand == 'mainhand', _check_hand(player));
__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> 
	if (hand == 'mainhand' && item_tuple, _check_hand(player));
__on_player_breaks_block(player, block) -> _check_hand(player);
__on_player_interacts_with_entity(player, entity, hand) ->  _check_hand(player);
__on_player_releases_item(player, item_tuple, hand) -> if (hand == 'mainhand', _check_hand(player));
__on_player_finishes_using_item(player, item_tuple, hand) -> if (hand == 'mainhand', _check_hand(player));
__on_player_attacks_entity(player, entity) -> _check_hand(player);

_check_hand(player) -> schedule(0, '_check_end_tick', player);

_check_end_tick(player) -> 
(
	if (player ~ 'holds', return());
	slot=player~'selected_slot';
	above = inventory_get(player, slot+27);
	if (!above, return());
	if (!(above:0 ~ 'shulker_box' ), return());
	first_item = above:2:'BlockEntityTag.Items[0]';
	if (!first_item, return());
	inventory_set(player, slot, first_item:'Count', first_item:'id', first_item:'tag');
	delete(above:2, 'BlockEntityTag.Items[0]');
	inventory_set(player, slot+27, above:1, above:0, above:2)
)
