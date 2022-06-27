// cart_breaker.sc 
// by kergadon aka nsluhrs
// Breaks minecarts such as chest minecarts or hopper minecarts also boats with chests
__config()->{'stay_loaded'->true};

__on_player_attacks_entity(player, entity) ->(
  mainhand=query(player,'holds','mainhand');
  // checks that an axe is held and player has right clicked
  if(query(player, 'sneaking') && mainhand:0 ~ '_axe',
    entity_type=entity~'type';
    if(entity_type~ '_minecart' || entity_type~ '_chest_boat',
      // drops all items in inventory 
      loop(inventory_size(entity), drop_item(entity, _));
      ep = pos(entity);
      // removes cart
      modify(entity,'remove');
      if(entity_type~'_minecart',
        // makes it so dropped items fall a short distance      
        ep:1 =ep:1+0.5;
        // cart case
        spawn('item', ep, {'Item' -> {'id' -> 'minecart', 'Count' -> 1}});
        spawn('item', ep, {'Item' -> {'id' -> entity~'type'~'(.+)_', 'Count' -> 1}}),
        // boat chest
        boat_type=query(entity,'nbt','Type');
        spawn('item', ep, {'Item' -> {'id' -> boat_type+'_boat', 'Count' -> 1}});
        spawn('item', ep, {'Item' -> {'id' -> 'chest', 'Count' -> 1}});
      );
    );
  );
);