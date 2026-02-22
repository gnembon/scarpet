// This addon adds 7 randomized potion trades to the Cleric. 
// It makes Haste, Dolphins Grace, Levitation, Wither, Blindness, Luck, 
// and Mining Fatigue available in survival as potions.
// Each effect can appear as a regular, splash, or lingering potion.
// Once a cleric becomes a Journeyman they can developer 0-3 potion trades. 

// credit goes to Firigion and the renewable_elytra_trade.sc script
// credit also goes to _Taser_Monkey_ who provided the nbt data for each custom potion.


// Radomly Choose Between these Potion types.
global_potion_types = ['splash_potion','lingering_potion','potion'];

// Pick a Price Between 4 and 8 emeralds
global_potion_emerald_cost = [4,8];

// Give each cleric 0-3 new potion trades.
global_max_potion_trades = 3;

// The level a cleric must be inorder to develop potion trades.
// levels go from 1-5 novice though Master
// I do not recommend setting this to 1, because 
// this code does not handle cycling villager trades. 
global_potion_master_level = 3;

// Nbt data for each available trade.
global_potion_trades = [
	// Dolphins Grace
	'{
		sell: {id: "minecraft:%s", Count:1, tag:{
			Potion:"minecraft:water", CustomPotionEffects:[{Duration:9600, Id: 30, Amplifier: 3}], 
			CustomPotionColor: 5592575, display: {Name:\'{ "text":"Potion of Grace"}\'}}
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
			Potion:"minecraft:water", CustomPotionEffects:[{Duration:9600, Id: 3, Amplifier: 1}], 
			CustomPotionColor: 16755200, display: {Name:\'{ "text":"Potion of Haste"}\'}}
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
			Potion:"minecraft:water", CustomPotionEffects:[{Duration:300, Id: 25, Amplifier: 3}], 
			CustomPotionColor: 16777215, display: {Name:\'{ "text":"Potion of Levitation"}\'}}
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
			Potion:"minecraft:water", CustomPotionEffects:[{Duration:170, Id: 20, Amplifier: 3}], 
			CustomPotionColor: 0, display: {Name:\'{ "text":"Potion of Decay"}\'}}
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
			Potion:"minecraft:water", CustomPotionEffects:[{Duration:2800, Id: 4, Amplifier: 0}], 
			CustomPotionColor: 5592405, display: {Name:\'{ "text":"Potion of Mining Fatigue"}\'}}
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
			Potion:"minecraft:water", CustomPotionEffects:[{Duration:330, Id: 15, Amplifier: 0}], 
			CustomPotionColor: 70000, display: {Name:\'{ "text":"Potion of Blindness"}\'}}
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

// potentially override chosen max trades to prevent duplicates
global_max_potion_trades = min(global_max_potion_trades,length(global_potion_trades) * length(global_potion_types));

_add_trades(cleric, p, nbt) -> (
	trades = {};
	effects = copy(global_potion_trades);
	// add 0-max number of potion trades
	loop(rand(1 + global_max_potion_trades),
		i = _;
		mapped_effects = map(effects, _ ~ '(?<=Potion of )\\w*');
		trade = nbt(str(
			// select a random trade from the list above
			count = {...mapped_effects};
			// count how many of each potion have been chosen
			for(
				filter(
					pairs(trades),
					mapped_effects ~ (_:1:'potion') != null
				),
				count:(_:1:'potion') += 1;
			);
			// delete options that will produce duplicates
			for(
				filter(
					pairs(count),
					_:1 >= length(global_potion_types)
				),
				if(mapped_effects ~ (_:0) != null,
					delete(effects,mapped_effects ~ (_:0));
				);
			);
			trade_choice = effects:rand(length(effects));
			// note potion effect choice for later
			trades:_ = {
				['potion', trade_choice ~ '(?<=Potion of )\\w*'],
				['type', null]
			};
			// here we actually give the potion effect to str()
			trade_choice,
			types = copy(global_potion_types);
			// if same potion effect, delete potion type options to prevent duplicates
			for(filter(pairs(trades),
					_:0 < i &&
					_:1:'potion' == trades:i:'potion' &&
					types ~ (_:1:'type') != null
				),
				delete(types,types ~ (_:1:'type'));
			);
			// Randomize the potion type with the leftover options
			type_choice = types:rand(length(types));
			// note potion type choice for later
			trades:_:'type' = type_choice;
			// here we actually give the potion type to str()
			type_choice,
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
