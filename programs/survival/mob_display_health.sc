// When a player hits any entity, the Mob Type is displayed with the remaining life (in HP) in the actionbar
// By: IceWolf23

__on_player_deals_damage(player, amount, entity) -> (
	entity_hp = entity ~ 'Health';
	_entityHealth = max(0, entity_hp-floor(amount));
	
	text_string = (format('w '+entity)+format('g  » ')+format('r '+_entityHealth+' ❤'));
	display_title(player, 'actionbar', text_string);
);
