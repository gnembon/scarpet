
// Rope Ladders
// by BisUmTo and Opsaaaaa
// (Carpet Mod 1.4.79)
//
// Right clicking on a ladder with another one, will extend the existing one down.
// Easy dismantle while holding shift destroys connected ladders
// Easy pickup will teleport nearby ladder items to the player
// Sky ropes allows you to extend ropes into the sky while holding shift



global_settings = {
  'easy_dismantle'->true,
  'easy_pickup'->true,
  'sky_ropes'->true,
  'pickup_range'->24,
  'height_limit'->384,
  'water_ladders'->true
};


__config() -> {'stay_loaded'->true};


//--- Events ---//

__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
  if(_rope_should_start(p, item, block),
    if((_attempt_place_ladder(_loop_to_ladder_end(block,'down'), block) ||
        if(_should_sky_rope(p,block),
          _attempt_place_ladder(_loop_to_ladder_end(block, 'up'), block)
        )
      ) &&
      p~'gamemode' != 'creative'
    ,// do
      _useup_ladder(p, hand);
      sound('block.ladder.place', block);
    );
  );
);

__on_player_breaks_block(p, block) -> (
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

_useup_ladder(p, hand) -> (
  slot = _get_placing_slot(p, hand, 'ladder');
  item = inventory_get(p, slot);
  inventory_set(
    p,
    slot,
    item:1 - 1,
    item:0,
    item:2
  );
);

_get_placing_slot(p, hand, item_name) -> if(
  hand=='mainhand' &&
  query(p,'holds','mainhand'):0 == item_name,// if
    p~'selected_slot'
  ,query(p,'holds','offhand'):0 == item_name, // elif
    -1
  ,// else
    inventory_find(p, item_name);
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

_should_sky_rope(p, ladder) -> (
  global_settings:'sky_ropes' &&
  (p~'sneaking' || solid(_under_ladder(ladder)))
);


//--- Get Nearby Blocks ---//

_loop_to_ladder_end(ladder, direction) -> (
    connected = _connected_ladder(ladder, direction);

    while(
      connected == 'ladder' &&
      block_state(connected,'facing') == block_state(ladder, 'facing'),
      global_settings:'height_limit',

      connected = _connected_ladder(connected, direction);
    );

    return(connected)
);

_above_ladder(ladder) -> _connected_ladder(ladder, 'up');

_under_ladder(ladder) -> _connected_ladder(ladder, 'down');

_connected_ladder(ladder, direction) -> (
  block(pos_offset(ladder, direction, 1));
);



