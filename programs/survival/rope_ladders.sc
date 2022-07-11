///
// Rope Ladders
// by BisUmTo
// (Carpet Mod 1.4.9)
//
// Right clicking on a ladder with an other one, will extend the existing one down.

// 'Updated' by opsaaaaa for 1.19
// added break unsuported ladder fearue

__config() -> {'stay_loaded'->true};



__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
  if(_rope_should_start(p, item, block),
    if( _attempt_place_ladder(_loop_to_ladder_base(block), block) &&
        p~'gamemode' != 'creative',

      _useup_ladder(p, item, hand);
    );
  );
);

__on_player_breaks_block(p, ladder) -> (
  if(ladder=='ladder',
    _break_unsuported_ladders(_under_ladder(ladder));
  );
);


_break_unsuported_ladders(ladder) -> (
  if(ladder == 'ladder',
    if(!_support_is_solid(ladder),
      destroy(ladder);
      schedule(1,'_break_unsuported_ladders', _under_ladder(ladder));
    );
  );
);


_rope_should_start(p, item, block) -> (
    p~'gamemode' != 'spectator' &&
    item && item:0 == 'ladder' &&
    block~'ladder' &&
    if(g=='adventure', nbt:'CanPlaceOn'~'"minecraft:ladder"', true)
);


_loop_to_ladder_base(block) -> (
    b = _under_ladder(block);

    if(b == 'ladder' && block_state(b,'facing') == block_state(block, 'facing'),

      return(_loop_to_ladder_base(b));

    );
    return(b)
);


_attempt_place_ladder(base, root) -> (
  if(air(base) || base == 'water', 
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


//--- Get Nearby Blocks ---//

_support_is_solid(block) -> (
  solid(pos_offset(block, block_state(block, 'facing'), -1));
);

_under_ladder(block) -> (
  block(pos_offset(block, 'down', 1));
);



