// This app will make leatherworkers get a trade to sell elytra.
// The conditions for the trade to appear and the trade tiself are fully customizable.
// The trade appears once the conditions are met and you interact with the villager.

// Custommize conditions

global_in_the_end = false; // does the villager need to be in the end for the trade to appear?
global_min_level = 1; // minimum level for the trade to appear
global_hold_elytra = true; // does the player need to hold an elytra while interacting with the leatherworker to make the trade appear?
global_consume_elytra = true; // is the elytra consumed when right clicking the villager with it to add the trade?
global_chances = 100; // percent chance for the trade to appear when all other conditions are met
global_consume_if_failed = false; // should the elyta be consumed even if adding the trade failed due to random chance (set in global_chances)


// Customize the trade

global_trade = encode_nbt(
	{
		'sell' -> {'id' -> 'minecraft:elytra', 'Count'->1},
		'buy' -> {'id' -> 'minecraft:emerald', 'Count' -> 40},
		'buyB' -> {'id' -> 'minecraft:phantom_membrane', 'Count' -> 5},
		'uses' -> 0,
		'demand' -> -20,
		'priceMutiplier' -> 0.0,
		'specialPrice' -> 0,
		'rewardExp' -> 1, // boolean (gives/doesn't give xp)
		'xp' -> 1, // count
	}
);

//////////
// Stop looking, none of your buisness down here
//////////

// Customize the conditions
_add_trade(e, p, nbt) -> (
	if(
		parse_nbt(nbt:'Tags')~'HasRenewableElytraTrade' == null && //already has trade
		(!global_in_the_end || e~'dimension'=='the_end') && //villager needs to be in the end
		nbt:'VillagerData':'level'>=global_min_level && // minimum level at which the trade appears
		(!global_hold_elytra || p~'holds':0 == 'elytra' ) //does the player need to hold an elytra?
		,

		if(r = (rand(100)<global_chances),
			run(str('/data modify entity %s Offers.Recipes append value %s', e~'command_name', global_trade));
		);
		
		if(	
			global_consume_elytra && // should consume elytra?
			(!global_hold_elytra || p~'holds':0 == 'elytra' ) && // did the player right click with an elytra and doing so is required?
			(r || global_consume_if_failed) && // should consume elytra even if trade adding failed?
			p~'gamemode'!='creative'
			,
				inventory_set(p, p~'selected_slot', 0); //consume elytra
				sound('item.armor.equip_elytra', e~'pos')
		);

		modify(e, 'tag', 'HasRenewableElytraTrade'); // give tag even if it failed by random chance
	);
);

__on_player_interacts_with_entity(player, entity, hand) -> (
	if(entity~'type'=='villager',
		nbt = entity~'nbt';
		if(nbt:'VillagerData':'profession'=='minecraft:leatherworker', _add_trade(entity, player, nbt));
	)
);
