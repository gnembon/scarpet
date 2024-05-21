// This addon adds 7 randomized potion trades to the Cleric. 
// It makes Haste, Dolphins Grace, Levitation, Wither, Blindness, Luck, 
// and Mining Fatigue available in survival as potions.
// Each effect can appear as a regular, splash, or lingering potion.
// Once a cleric becomes a Journeyman they can obtain 0-3 potion trades. 

// credit goes to Firigion and the renewable_elytra_trade.sc script
// credit also goes to _Taser_Monkey_ who provided the nbt data for each custom potion.


// Randomly choose between these Potion types.
global_potion_types = ['splash_potion','lingering_potion','potion'];

// Pick a price between 4 and 8 emeralds
global_potion_emerald_cost = [4,8];

// Give each cleric 0-3 new potion trades.
global_max_potion_trades = 3;

// The level a cleric must be in order to develop potion trades.
// levels go from 1-5 Novice though Master
// I do not recommend setting this to 1, because 
// this code does not handle cycling villager trades. 
global_potion_master_level = 3;

// NBT data for each available trade.
global_potion_trades = [
	// Dolphins Grace
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", custom_potion_effects:[{duration:9600, id: "minecraft:dolphins_grace", amplifier: 3}], 
			CustomPotionColor: 5592575, display: {Name:\'{ "text":"Potion of Grace", "italic":false}\'}}
		},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:tropical_fish", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}',
	// Luck
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{Potion: "minecraft:luck"}},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:glow_berries", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}',
	// Haste
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", custom_potion_effects:[{duration:9600, id: "minecraft:haste", amplifier: 1}], 
			CustomPotionColor: 16755200, display: {Name:\'{ "text":"Potion of Haste", "italic":false}\'}}
		},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:raw_gold", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}',
	// Levitation
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", custom_potion_effects:[{duration:300, id: "minecraft:levitation", amplifier: 3}], 
			CustomPotionColor: 16777215, display: {Name:\'{ "text":"Potion of Levitation", "italic":false}\'}}
		},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:phantom_membrane", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}',
	// Decay
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", custom_potion_effects:[{duration:170, id: "minecraft:wither", amplifier: 3}], 
			CustomPotionColor: 0, display: {Name:\'{ "text":"Potion of Decay", "italic":false}\'}}
		},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:wither_rose", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}',
	// Mining Fatigue
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", custom_potion_effects:[{duration:2800, id: "minecraft:mining_fatigue", amplifier: 0}], 
			CustomPotionColor: 5592405, display: {Name:\'{ "text":"Potion of Mining Fatigue", "italic":false}\'}}
		},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:soul_sand", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}',
	// Blindness
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", custom_potion_effects:[{duration:330, id: "minecraft:blindness", amplifier: 0}], 
			CustomPotionColor: 70000, display: {Name:\'{ "text":"Potion of Blindness", "italic":false}\'}}
		},
		buy: {id: "minecraft:emerald", Count: %d},
		buyB: {id: "minecraft:ender_eye", Count: 1},
		uses: 0,
		demand: -20,
		priceMutiplier: 0.0,
		specialPrice: 0,
		rewardExp: 1,
		xp: 1
	}'
	
];

_add_trades(cleric, p, nbt) -> (
	// add 0-max number of potion trades
	loop(rand(1 + global_max_potion_trades),

		trade = nbt(str(
			// select a random trade from the list above
			global_potion_trades:rand(length(global_potion_trades)),
			// Randomize the potion type 
			global_potion_types:rand(length(global_potion_types)),
			// randomize the cost of each trade
			rand((global_potion_emerald_cost:1) - global_potion_emerald_cost:0) + global_potion_emerald_cost:0
		));

		// add the new potion trade to the end of the clerics trades
    	put(nbt, 'Offers.Recipes', trade, -1 );
	);
	
	modify(cleric, 'nbt_merge', nbt);
	// Dont add any more potions to a potion master
	modify(cleric, 'tag', 'PotionMaster:IsPotionMaster'); 

);

__on_player_interacts_with_entity(player, entity, hand) -> (
	if(entity~'type'=='villager',
		nbt = entity~'nbt';
		// Check if the Villager is a cleric has a high enough level and is not already a potion master
		if(nbt:'VillagerData':'profession'=='minecraft:cleric' && 
			nbt:'VillagerData':'level' >= global_potion_master_level &&
			parse_nbt(nbt:'Tags')~'PotionMaster:IsPotionMaster' == null
			, 
			_add_trades(entity, player, nbt)
		);
	)
);

