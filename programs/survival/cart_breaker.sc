// cart_breaker.sc 
// by kergadon aka nsluhrs
// Breaks minecarts such as chest minecarts or hopper minecarts also boats with chests
__config()->{'stay_loaded'->true};

__on_player_attacks_entity(player, entity) ->(
  mainhand=query(player,'holds','mainhand');
  // checks that an axe is held and player has right clicked
  if(query(player, 'sneaking') && mainhand:0 ~ '_axe',
    entity_type=entity~'type';
    //checks if entity is seprable
    if(entity_type~ '_minecart' || entity_type == 'chest_boat',
      // drops all items in inventory 
      loop(inventory_size(entity), drop_item(entity, _));
      ep = pos(entity);
      // removes cart
      modify(entity,'remove');
      if(entity_type~ '_minecart' ,
        // makes it so dropped items fall a short distance      
        ep:1 =ep:1+0.5;
        // cart case
        spawn('item', ep, {'Item' -> {'id' -> 'minecart', 'Count' -> 1}, 'Motion' -> get_drop_motion()});
        spawn('item', ep, {'Item' -> {'id' -> entity_type~'(.+)_', 'Count' -> 1}, 'Motion' -> get_drop_motion()}),
        // chest_boat
        boat_type=query(entity,'nbt','Type');
        spawn('item', ep, {'Item' -> {'id' -> boat_type+'_boat', 'Count' -> 1}, 'Motion' -> get_drop_motion()});
        spawn('item', ep, {'Item' -> {'id' -> 'chest', 'Count' -> 1}, 'Motion' -> get_drop_motion()});
      );
    );
  );
);
get_drop_motion() -> (
  //generates random motion similar to normal dropped items
  motion=[rand(0.2)-0.1,0.2,rand(0.2)-0.1];
  motion
);