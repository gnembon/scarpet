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

//set the changes of the trade appearing in %
global_chances = 100;

// Customize the conditions
_add_trade(e, p) -> (
	nbt = e~'nbt';
	if(
		//e~'dimension'=='the_end' && //villager needs to be in the end
		nbt:'VillagerData':'level'>0 && // minimum level at which the trade appears
		he = (p~'holds':0 == 'elytra') && //does the player need to hold an elytra?
		parse_nbt(nbt:'Tags')~'HasTrade' == null,

		if(r = rand(100)<global_chances,
			run(str('/data modify entity %s Offers.Recipes append value %s', e~'command_name', global_trade));
		);
		
		if(	r && //comment this line if adding a trade should always consume an elytra, regardless of if it succeeds or not
			p~'gamemode'!='creative' && he,
				inventory_set(p, p~'selected_slot', 0); //consume elytra
				sound('item.armor.equip_elytra', e~'pos')
		);

		modify(e, 'tag', 'HasTrade');
	);
);

__on_player_interacts_with_entity(player, entity, hand) -> (
	if(entity=='Leatherworker', _add_trade(entity, player))
);
