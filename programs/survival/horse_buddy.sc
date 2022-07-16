//
// Horse Buddy Carpet Script
// by opsaaaaa

// Every time I've tried to do horse breeding in minecraft,
// I have either ended up with so many horses its a lag machine or worse,
// I ended up slaughtering an unreasonable about of minecraft horses.
// Nobody likes killing mc horses.
// However player still want decent stats on their steeds.

// So I am attempting to reslove minecrafts horse killing problem by
// allowing players to roll stat adjustments to their horses.

// You can trigger a new roll by continuing to feed the horse the 
// random food its craving.

// you can increase the chances of higher rolls in a few ways.

// one, by remaining loyal to a single steed 
// and 'training' your horse, probably by taking it on a long trip.

// you can sway the odds with different potion effects.
// If a roll is triggered when the horse has a speed effect,
// then the roll will be bias toward making the steed faster.
// same goes for jump boost.
// and regeneration will increase the odds for a health boost.
// strength just increase the odds overall.

// the odd all accumulate


//--- CONFIGURABLEish ---//

global_lang = 'en';

// use to this map to modify the training rolls for various potion effects
// values should range between 0 and 1. 0.5 is neutral.
// Effects can stack, so these values accumulate with multiple effects on the horse.
// I keep fiddling with the numbers, im not sure they are balanced.
// However I am also not sure this concept is balanced...
_potion_groups() -> { 
  'speed' -> {
    'generic.movement_speed' -> 0.65,
    'horse.jump_strength' -> 0.4,
    'generic.max_health' -> 0.4
  },
  'slowness' -> {
    'generic.movement_speed' -> 0.3,
    'horse.jump_strength' -> 0.55,
    'generic.max_health' -> 0.55
  },
  'jump_boost' -> {
    'generic.movement_speed' -> 0.4,
    'horse.jump_strength' -> 0.65,
    'generic.max_health' -> 0.4
  },
  'slow_falling' -> {
    'generic.movement_speed' -> 0.55,
    'horse.jump_strength' -> 0.3,
    'generic.max_health' -> 0.55
  },
  'strength' -> {
    'generic.movement_speed' -> 0.55,
    'horse.jump_strength' -> 0.55,
    'generic.max_health' -> 0.6
  },
  'weakness' -> {
    'generic.movement_speed' -> 0.4,
    'horse.jump_strength' -> 0.4,
    'generic.max_health' -> 0.4
  },
  'regeneration' -> {
    'generic.movement_speed' -> 0.45,
    'horse.jump_strength' -> 0.45,
    'generic.max_health' -> 0.6
  },
  'poison' -> {
    'generic.movement_speed' -> 0.6,
    'horse.jump_strength' -> 0.6,
    'generic.max_health' -> 0.2
  }
};
// Accumulate the potion group stat modifiers.
_effect_stat_mods(effects) -> (
  reduce(effects,
    for(pairs(_potion_groups():_),
      _a:(_:0) = sum(_a:(_:0), _:1,  -0.5);
    );
    _a
  ,
  { 'generic.movement_speed' -> 0.5,
    'horse.jump_strength' -> 0.5,
    'generic.max_health' -> 0.5
  })
);


// min and max clamps for horse stats.
_horse_stat_range() -> ({
  'generic.movement_speed' -> [0.112499997,         0.337499997],
  'horse.jump_strength' ->    [0.4000000059604645,  1.000000006],
  'generic.max_health' ->     [15, 46]
  // 15,32 is vanilla range. 
  // I just want to allow horses to become more durable against surprise creepers.
  // Higher than vanilla jump results in horses taking fall damage.
  // Not much higher than vanilla speed is honestly rediculus, almost unplayable on normal terrain.
});


// keep track of how much training the players horse has done.
// training mosting meaning using the horse normally.
global_training = {
  'uuid' -> null,
  'food' -> 'wheat',
  'generic.movement_speed' -> 0,
  'horse.jump_strength' -> 0,
  'generic.max_health' -> 0
};

// Rest the training when ridding a new horse
// or after rolling for new stats.
_rest_training(uuid) -> (
  global_training = {
    'uuid' -> uuid,
    'food' -> _random_food(),
    'generic.movement_speed' -> 0,
    'horse.jump_strength' -> 0,
    'generic.max_health' -> 0
  };
);


// Roll an amount to add or remove from a given stat
_roll_stat_for(stat, modifier) -> (
  call(
    // Adjust movement speed by 10% of vanilla range
    { 'generic.movement_speed' -> _(x) -> (
      0.225 * 0.2 * ((x) - rand(1))
    ),
    // Adjust jump height by roughly 1/2 blocks
    'horse.jump_strength' -> _(x) -> (
      0.25 * ((x) - rand(1))
    ),
    // Adjust health by 1/2 Hearts
    'generic.max_health' -> _(x) -> (
      3.0 - floor(rand(6)+((x-0.5)*6))
    )
  }:stat, modifier)
);

_clamp(mn, mx, val) -> max(mn, min(mx, val));



//--- FOOD ---//

_random_food() -> keys(_food_strengths()):(floor(rand(6)));

_food_strengths() -> ({
  'wheat'->1,
  'apple'->2,
  'golden_carrot'->6,
  'golden_apple'->64,
  'hay_block'->12
});


//--- ON PLAYER METHODS ---//

__on_player_rides(p, forward, strafe, jumping, sneaking) -> (
  global_training:'horse.jump_strength' += number(jumping);
  global_training:'generic.movement_speed' += ceil(forward) + ceil(strafe);
);

__on_player_interacts_with_entity(p, horse, hand) -> (
  if(_is_a_horse(horse),
    _horse_logic(p, horse);
  );
);


//--- Conditionals ---//

_is_a_horse(horse) -> (
  kind = horse~'type';
  return(kind == 'horse' || kind == 'donkey' || kind == 'mule');
);

//--- LOGIC n stuff ---//

_horse_logic(p, horse) -> (

    // reset training for new horses
    if(horse~'uuid' != global_training:'uuid',
      _rest_training(horse~'uuid');
    );


    item = p~'holds';
    global_training:'generic.max_health' += _food_strengths():(item:0);
    
    // when you feed the demanding horse what it wants
    if(item:0 == global_training:'food',

      // Pick a new craving
      global_training:'food' = _random_food();

      particle('happy_villager', horse~'pos'+[0,1.5,0], 8, 0.5);

      // once the horse is satisfied, re-roll some stats.
      if(global_training:'generic.max_health' > 16,
        _roll_new_stats(horse);
        schedule(10, '_after_hint', p)
      );

    //elif make the horse complain about its food.
    ,_food_strengths():(item:0) || !rand(8),
      schedule(10, '_food_hint', p)

    //elif give random hits about potions and things.
    ,!rand(5), 
      schedule(10, '_random_hint', p)

    );
);

//--- ROLL NEW STATS ---//

_roll_new_stats(horse) -> (
  effect_mod = _effect_stat_mods(reduce(horse~'effect', _a += _:0, []));
  attributes = horse~'attribute';

  // morph training values into useable attribute modifiers.
  // (stat - easy_stat) / hard_stat
  _morph_training_stat('horse.jump_strength', 200, 2000);
  _morph_training_stat('generic.movement_speed', 1000, 18000);
  _morph_training_stat('generic.max_health', 20,  200);

  // build the new horse attributes
  new_attrs = [];
  for(pairs(_horse_stat_range()),
    new_attrs += {
      'Name' -> _:0,
      'Base' -> _clamp( _:1:0, _:1:1, attributes:(_:0) + _roll_stat_for(_:0, 
        effect_mod:(_:0)+_clamp(-0.05,0.6,global_training:(_:0))
      ))
    }
  );

  // merge in the attributes and show some nice particales
  modify(horse, 'nbt_merge', nbt({'Attributes' -> new_attrs}));
  particle( 'glow', horse~'pos'+[0,1.8,0], 32, 1);
  sound('minecraft:entity.zombie_villager.cure',horse~'pos');

  // reset the horses training and remove the effects
  _rest_training(horse~'uuid');
  modify(horse, 'effect');
);

_morph_training_stat(stat, easy_val, hard_val) -> (
  global_training:stat = clamp(-0.05, 0.6,((global_training:stat - easy_val) / hard_val) * 0.5)
);


//--- MESSAGES ---//

_after_hint(p) -> _rand_message(p,'roll.after', 2);
_random_hint(p) -> _rand_message(p,'random.hint', 9);
_food_hint(p) -> (
  _rand_message(p, str('food.%s', global_training:'food'), 2);
);
_message(p,code) -> (
  display_title(p, 'actionbar', format(split('\\|','iy ' + _i18n(code))))
);
_rand_message(p,code,num) -> (
  _message(p,str('%s.%d', code, floor(rand(num + 0.9))));
);


//--- LOCALS ---//

_i18n(code) -> ({
  'en'->{
    'random.hint.0'       -> 'giv\'me jumpy jump magic!',
    'random.hint.1'       -> 'think potions make sparkles different',
    'random.hint.2'       -> 'wana try a fast potion!',
    'random.hint.3'       -> 'dum dum give me... magic potions!',
    'random.hint.4'       -> 'stick together! look its a stick.',
    'random.hint.5'       -> 'squirrel!',
    'random.hint.6'       -> 'Buddy! How do you like it when I call you buddy?',
    'random.hint.7'       -> 'i\'ll train hard, go faster!',
    'random.hint.8'       -> 'i\'m speed!',
    'random.hint.9'       -> 'sparkles make legs feel funny',

    'roll.after.0'        -> 'ride me! I bet im faster! maybe...',
    'roll.after.1'        -> 'legs feel different...',
    'roll.after.2'        -> 'uhh, don\'t feel good',

    'food.apple.0'        -> 'Neigh...apple...Neigh!',
    'food.apple.1'        -> 'red apple yummy',
    'food.apple.2'        -> 'dum dum give me...apple',
    'food.golden_apple.0' -> 'nom nom, apple yellow nom.',
    'food.golden_apple.1' -> 'Fancy Steed! Fancy Apple!',
    'food.golden_apple.2' -> 'ill eat your wallet.',
    'food.golden_carrot.0'-> 'wana...fancy...snowman...nose!',
    'food.golden_carrot.1'-> 'giv\'me da carrot.',
    'food.golden_carrot.2'-> 'dum dum give me...golden carrot',
    'food.hay_block.0'    -> 'hay hey hay hey hay',
    'food.hay_block.1'    -> 'buddy wana square meal',
    'food.hay_block.2'    -> 'so I can talk, now feed me a block!',
    'food.wheat.0'        -> 'grain yum',
    'food.wheat.1'        -> 'hungry for...yella\' dried grass',
    'food.wheat.2'        -> 'ima wheat-eater'
  }
}:global_lang:code);


