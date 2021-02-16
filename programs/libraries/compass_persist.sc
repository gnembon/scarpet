item_consumed(...args) -> (
  if (type(args: 0) == 'entity' && args: 0 ~ 'type' == 'player',
    player = args: 0;
    slots = slice(args, 1);
  ,
    player = player();
    slots = args;
  );

  slots = map(slots,
    if (type(_) == 'number',
      _
    , _ == 'mainhand',
      player ~ 'selected_slot'
    , _ == 'offhand',
      -1
    );
  );

  for (slots,
    signal_event('compass:item_consumed', player, _);
  );
);