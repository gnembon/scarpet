///
// Rope Ladders
// by BisUmTo and Opsaaaaa
// (Carpet Mod 1.4.9)
//
// Right clicking on a ladder with an other one, will extend the existing one down.

// 'Updated' by opsaaaaa for 1.19
// added break unsupported ladder fearue

global_settings = {
  'easy_dismantle'->true,
  'easy_pickup'->true,
  'pickup_range'->20,
  'height_limit'->384,
  'sky_ropes'->true,
  'water_ladders'->true
};

__config() -> {'stay_loaded'->true};


//--- Events ---//

__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
  if(_rope_should_start(p, item, block),
    if( _attempt_place_ladder(_loop_to_ladder_base(block), block) &&
        p~'gamemode' != 'creative',

      _useup_ladder(p, item, hand);
    );
  );
);

__on_player_breaks_block(p, block) -> (
  print('woop');
  if(_rope_should_dismantle(p, block),
    _dismantle_connected_ladders(block);
    if(global_settings:'easy_pickup',
      schedule(global_settings:'pickup_range','_pickup_loose_ladders', p);
    );
  );
);


//--- Do Someting ---//

_pickup_loose_ladders(p) -> (
  for(filter(
    entity_area('item', player()~'pos', [4,global_settings:'pickup_range',4]),
    _~ 'nbt':'Item':'id'~'ladder'
  ),
    modify(_, 'pos', player()~'pos')
  );
);

_dismantle_connected_ladders(ladder) -> (
  above = _above_ladder(ladder);
  under = _under_ladder(ladder);

  while(above == 'ladder', global_settings:'height_limit',
    schedule(_, '_destroy', above);
    above = _above_ladder(above);
  );
  while(under == 'ladder', global_settings:'height_limit',
    schedule(_, '_destroy', under);
    under = _under_ladder(under);
  );
);

_destroy(block) -> destroy(block);

_attempt_place_ladder(base, root) -> (
  if(air(base) || (global_settings:'water_ladders' && base == 'water'), 
    set(base, root, 'waterlogged', if(base == 'water', 'true', 'false'));
  );
);

_useup_ladder(p, item, hand) -> (
  inventory_set(
    p,
    if(hand=='mainhand',player~'selected_slot',-1), 
    item:1 - 1, 
    item:0,
    item:2
  );
);

//--- Conditionals ---//

_rope_should_start(p, item, block) -> (
  item && item:0 == 'ladder' &&
  block~'ladder' &&
  if(p~'gamemode'=='adventure', item:2:'CanPlaceOn'~'ladder', true)
);

_rope_should_dismantle(p, block) -> (
  global_settings:'easy_dismantle' &&
  block == 'ladder' &&
  p~'sneaking'
);


//--- Get Nearby Blocks ---//

_loop_to_ladder_base(ladder) -> (
    under = _under_ladder(ladder);

    while(
      under == 'ladder' &&
      block_state(under,'facing') == block_state(ladder, 'facing'),
      global_settings:'height_limit',

      under = _under_ladder(under);
    );

    return(under)
);

_support_is_solid(ladder) -> (
  solid(_ladder_support(ladder));
);

_ladder_support(ladder) -> (
  pos_offset(ladder, block_state(ladder, 'facing'), -1)
);

_above_ladder(ladder) -> (
  block(pos_offset(ladder, 'up', 1));
);

_under_ladder(ladder) -> (
  block(pos_offset(ladder, 'down', 1));
);



