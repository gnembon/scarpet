// allows instamining deepslate if the player has an efficiency 5 netherite pick and haste 2
// works even if the player doesn't have carpet mod on their client

__config() -> {
  'stay_loaded' -> true,
};

// list of blocks that can be instamined
// remove the comment below to allow cobbled deepslate, or add other blocks
global_instamine_blocks = [
  //'cobbled_deepslate',
  'deepslate'
];

// tools that can be use to instamine the list of blocks above
// remove the comment below to allow diamond pickaxes, or add other pickaxes
global_required_pickaxes = [
  //'diamond_pickaxe',
  'netherite_pickaxe'
];

__on_player_clicks_block(player, block, face) -> (
  held_item = player ~ 'holds';
  // check if player is holding item
  if(held_item != null,
    // check if block can be instamined and correct tool is being used
    if(global_instamine_blocks ~ str(block) != null && global_required_pickaxes ~ (held_item:0) != null,
      // check if player has haste 2 and efficiency 5
      if(__has_haste_2(player) && __has_efficiency_5(held_item:2),
        harvest(player, block);
      );
    );
  );
);

__has_haste_2(player) -> (
  // get player haste effect
  haste_effect = query(player, 'effect', 'haste');

  // check if effect is not null and is level 2 or more
  haste_effect != null && haste_effect:0 >= 1;
);

__has_efficiency_5(nbt) -> (
  // get enchantments list
  enchants_list = parse_nbt(nbt):'Enchantments';

  // check if enchants exist
  if(enchants_list != null,
    // find enchant that matches and check if not null
    first(enchants_list,
      // check if enchant is efficiency and is level 5 or more
      _:'id' == 'minecraft:efficiency' && _:'lvl' >= 5;
    ) != null,
  //else
    false
  );
);
