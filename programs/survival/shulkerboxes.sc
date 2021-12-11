///
// vacuum and restock
// by gnembon
// (keep ability added by Senth)
///

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__command() -> '
Any shulkerbox with \'vacuum\' in its name
will have vacuum capability

Current vacuum mode:'+global_vacuum+'

use /shulkerboxes toggle_vacuum to change it
   
Any shulkerbox with \'swap\' or \'restock\'
followed with \'same\', \'keep\', \'first\',
\'next\' or \'random\'
will restock player inventory
 - swap: will swap a hotbar stack every time it changes
 - restock: will replace fully used up stacks
 
 - same: will return only matching items
 - keep: as same but will keep 1 item in the shulkerbox
 - first: will always return first item from a box
 - next: will return items in sequence
 - random: will draw a random stack
';

global_vacuum = 'collision';

toggle_vacuum() -> 
(
   global_vacuum = if (global_vacuum == 'pickup', 'collision', 'pickup');
   'toggled vacuum mode to '+global_vacuum
);

__on_player_picks_up_item(player, ingested_item_tuple) -> if (global_vacuum == 'pickup',
(
   [item, count, tag] = ingested_item_tuple;
   // skipping nonstackables, since they won't stack anyways
   if (stack_limit(item) > 1,
      // unless its all messeup up modded, you have to have a sufficient stack in the inventory
      // even if it is split into multiple
      [item_slot, current_count] = __locate_sufficient_stack(player, item, count, tag );
      items_left = __add_item_to_vacuum_sboxes(player, item, count, tag, true);
      count_to_remove = count - items_left;
      if (count_to_remove,
         inventory_set(player, item_slot, current_count-count_to_remove);
      );
   );
));

__on_player_collides_with_entity(player, entity) -> if (global_vacuum == 'collision',
(
   if (entity~'pickup_delay' == 0,
      [item, count, tag] = entity~'item';
      if ( stack_limit(item) > 1,
         items_left = __add_item_to_vacuum_sboxes(player, item, count, tag, false);
         count_to_remove = count - items_left;
         if (count_to_remove,
            sound('entity.item.pickup', pos(player), 0.2 , (rand(1)-rand(1))*1.4+2.0, 'player');
            if (items_left == 0,
               // we mocked adding, now play animations and will add that at the end
               modify(entity, 'pickup_delay', 20);
               max_age = entity~'age'+3;
               entity_event(entity, 'on_tick', '__item_animation', max_age, player);
            ,
               actual_left = __add_item_to_vacuum_sboxes(player, item, count, tag, true);
               modify(entity, 'nbt_merge', '{Item:{Count:'+actual_left+'b}}');
            )
         );
      );
   )
));

__item_animation(e, max_age, player) ->
(
   particle('portal', pos(e)-[0,0.3,0], 10, 0.1, 0);
   if (e~'age' > max_age, 
      [item, count, tag] = e~'item';
      items_left = __add_item_to_vacuum_sboxes(player, item, count, tag, true);
      count_to_remove = count - items_left;
      if (count_to_remove, modify(e, 'nbt_merge', '{Item:{Count:'+(count-count_to_remove)+'b}}'));
      if (items_left == 0, modify(e, 'remove'));
      modify(e, 'pickup_delay', 1);
      entity_event(e, 'on_tick', null);
   ,
      modify(e, 'accelerate', (pos(player)-pos(e))/5);
   );
);


// search for a stack in inventory that has enough item to cover that.
__locate_sufficient_stack(player, search_item, required_count, search_tag ) ->
(
   item_slot = -1;
   while( (item_slot = inventory_find(player, search_item, item_slot+1)) != null, inventory_size(player), 
      [item, count, tag] = inventory_get(player, item_slot);
      if( search_tag == tag && (current_count = count) >= required_count,
         return([item_slot, current_count]);
      )
   );
   // we are expecting to find it, something went wrong
   exit();
);

// ids in tags will not have stripped 'minecraft:' prefix 
__to_fqdn(item) -> if (item ~ ':', item, 'minecraft:'+item);

// searches inventory for vacuum shulkerboxes and attempts to add to their stacks matching items
// if optional do_change is false - it will only mock if it possible to insert, but not change the
// shulkerbox nbts
__add_item_to_vacuum_sboxes(player, search_item, refill_count, search_tag, do_change) ->
(
   item_limit = stack_limit(search_item);
   item_fqdn = __to_fqdn(search_item);
   //searching first in regular inventory, and then the enderchest
   for( [player, 'enderchest_'+player],
      inventory = _;
      // not using inventory_find  here since shulkerboxes have all different ids based on color
      loop(inventory_size(inventory), 
         current_inventory_slot = _;
         // skipping empty slots
         if ( (current_item_tuple = inventory_get(inventory, _)) != null,
            [shulker_item, scount, shulker_tag] = current_item_tuple;
            // only consider shulkerboxes that are stacked to 1, 
            // and have a custom name, which contains 'vacuum' and have non-empty inventory
            if ( shulker_item ~ 'shulker_box$' 
                  && scount == 1 
                  && (nametag = shulker_tag:'display.Name') != null
                  && lower(parse_nbt(nametag):'text') ~ 'vacuum'
                  && (items = shulker_tag:'BlockEntityTag.Items[]') != null ,
               // well, not sure why nbt query for singleton lists return that element, not a list
               // but that's a Mojang thing
               if (type(items)=='nbt', items = [items]);
               for ( items,
                  stack_tag = _;
                  list_position = _i;
                  // searching for matching items with same tag that can accomodate extra items.
                  if ( stack_tag:'id' == item_fqdn 
                        && (initial_count = stack_tag:'Count') < item_limit 
                        && search_tag == stack_tag:'tag',
                     remaining_capacity = item_limit-initial_count;
                     restock_amount = min(remaining_capacity, refill_count);
                     // in that case we restock all or part of the refill item with the current sbox stack
                     if (do_change,
                        put(shulker_tag, 
                           'BlockEntityTag.Items['+list_position+'].Count', 
                           (initial_count+restock_amount)+'b'
                        );
                        inventory_set(inventory, current_inventory_slot, 1, shulker_item, shulker_tag);
                     );
                     // if we were able to accomodate the rest of the stack
                     if (remaining_capacity >= refill_count, return(0) );
                     // continue with the rest of the stack
                     refill_count = refill_count - restock_amount;
                  )
               )
            )
         );
      );
   );
   refill_count;
);


///
// restock
///
__on_player_uses_item(player, item, hand)                   -> __refill(player, hand, item);
__on_player_right_clicks_block(player, item, hand, b, f, h) -> __refill(player, hand, item);
__on_player_releases_item(player, item, hand)               -> __refill(player, hand, item);
__on_player_finishes_using_item(player, item, hand)         -> __refill(player, hand, item);

__on_player_breaks_block(player, block)            -> __check_hand(player, 'mainhand');
__on_player_interacts_with_entity(player, e, hand) -> __check_hand(player, hand);
__on_player_drops_item(player)                     -> __check_hand(player, 'mainhand');
__on_player_drops_stack(player)                    -> __check_hand(player, 'mainhand');

// weapon may break, or you may run out of bows in the offhand
__on_player_attacks_entity(player, e) -> for(['mainhand', 'offhand'], __check_hand(player, _));

__check_hand(player, hand) -> if (item = query(player,'holds',hand), __refill(player, hand, item));
   

global_tick_actions = {};

// makes a note of a potential change in player hotbar slot due to their actions
__refill(player, hand, item) ->
(
   if (item,
      slot = if( hand == 'offhand', 40, player ~ 'selected_slot');
      if (!has(global_tick_actions, slot), global_tick_actions:slot = item);
      schedule(0, '__refill_endtick', player);
   );
);

// checks slots that may have changed that tick due to player actions
__refill_endtick(player) ->
(
   for (global_tick_actions,
      __check_slot_change(player, _, global_tick_actions:_)
   );
   global_tick_actions = {};
);

// restock 
// 1 same / next / random / first     // pick next stack at random or pick next stack from list, or always first, or the same item
// 4 restock / swap      // only from empty stack or everytime it decreases

// default - restock same

//enderpearls/rockets - 'restock same'
//simple block storage - 'restock same'
//mixed foods - 'restock first/random'
//pattern placer - 'swap next'
//random placer - 'swap random' // 2 stacks min for stateless
//lava buckets - 'swap same'

// compares stored item stack from before the actions to check if
// the count of the items decreased
__replaceable(item) ->
(
   item == 'potion' || item ~ '_(bucket|stew|soup|bottle)$' || item == 'crossbow'
);

__check_slot_change(player, slot, previous_item_tuple) ->
(
   [previous_item, previous_count, previous_tag] = previous_item_tuple;
   current_tuple = inventory_get(player, slot);
   [current_item, current_count, current_tag] = if (current_tuple, current_tuple, [previous_item, 0, previous_tag]);
   // actionable for restock - resources decreased.
   // or replaceable item changed
   if ( (current_item == previous_item && current_count < previous_count) 
        || 
        ( __replaceable(previous_item)
            && ( previous_item != current_item || previous_tag != current_tag) 
            && current_count == previous_count && current_count == 1
        ),
      __swap_stack(player, slot, previous_item, current_item, current_count, current_tag);
   )
);

// returns the proper order of inventory slots to check given the hotbar slot that was affected
// will scan
__get_inventory_indices_for_slot(slot, player) ->
(
   start_offset = if (slot==40, 8, slot < 9, slot, 0);
   echest = 'enderchest_'+player;
   indices = [
         [player, 27+start_offset], [player, 18+start_offset], [player, 9+start_offset],
         [echest, 18+start_offset], [echest, 9 +start_offset], [echest, 0+start_offset]
   ];
   for (range(start_offset+1, start_offset+9),
      offset = _ % 9;
      put(indices, null, [ [player, offset],
         [player, 27+offset], [player, 18+offset], [player, 9+offset],
         [echest, 18+offset], [echest, 9 +offset], [echest, 0+offset],
      ], 'extend');
   );
   indices
);

// gets stack and slot info and finds stack in restock / swap shulkerboxes eligible for 
// restock
__swap_stack(player, slot, previous_item, item, count, tag) ->
(
   item_fqdn = __to_fqdn(previous_item);
   // scanning inventory in order
   for (__get_inventory_indices_for_slot(slot, player),
      [inventory, islot] = _;
      if ( (inv_item = inventory_get(inventory, islot)) != null,
         [shulker_item, scount, shulker_tag] = inv_item;
         // only consider shulkerboxes that are stacked to 1, 
         // and have a custom name, which contains restock / swap with appropriate option
         // and replacement count is non-zero for restock boxes
         // and have non-empty inventory
         if ( shulker_item ~ 'shulker_box$' 
               && scount == 1 
               && (nametag = shulker_tag:'display.Name') != null
               && (shulker_type = lower(parse_nbt(nametag):'text') ~ '(restock|swap)\\s+(same|keep|next|random|first)')
               && ([action_type, idx_choice] = shulker_type; (action_type!='restock' || count == 0 ) )
               && (shulker_stacks = shulker_tag:'BlockEntityTag.Items[]') != null ,
            if (type(shulker_stacks)=='nbt', shulker_stacks = [shulker_stacks]);
            sb_item_count = length(shulker_stacks);
            for( shulker_stacks,
					// item matches and not a potion or matching potion effect as well as same and keep
               if( _:'id' == item_fqdn && ( !(previous_item ~ 'potion$') || ( (idx_choice == 'same' || idx_choice == 'keep') && ( _:'tag':'Potion' == tag:'Potion' ) ) ),
                  replacement_index = if (
							// either not a potion or matches potion effect
                     (idx_choice == 'same' || idx_choice == 'keep') && ( !(previous_item ~ 'potion$') || _:'tag':'Potion' == tag:'Potion' ),
                        _i,
                     idx_choice == 'random', 
                        floor(rand(sb_item_count)),
                     idx_choice == 'first', 
                        0
                     ,
                     // else - logic for 'next'
                        sequence = shulker_tag:'tag.RestockSequence';
                        if (sequence == null,
                        // new sbox
                           if (!has(shulker_tag:'tag'), shulker_tag:'tag' = '{}');
                           sequence = 0;
                        ,
                        // old sbox
                           sequence = (sequence+1)%sb_item_count;
                        );
                        shulker_tag:'tag.RestockSequence' = sequence;
                        sequence
                  );
                  // get stack info from the shulker box
                  swapped_item_tag = shulker_stacks:replacement_index;
                  swapped_id = swapped_item_tag:'id';
                  swapped_count = swapped_item_tag:'Count';
                  swapped_tag = swapped_item_tag:'tag';
                  // keep - skip if the stack size is 1
                  if (idx_choice == 'keep' && swapped_count == 1,
                     continue();
                  );
                  // shulker box changes
                  if (count>0, // replace tag
                     // because 'minecraft:stone' gets parsed as a string 'minecraft' as a tag
                     // so we need extra quotes for that
                     swapped_item_tag:'id' = '"'+__to_fqdn(item)+'"';
                     swapped_item_tag:'Count' = count+'b';
                     if (tag, swapped_item_tag:'tag' = tag, delete(swapped_item_tag:'tag'));
                     put(shulker_tag, 'BlockEntityTag.Items['+replacement_index+']', swapped_item_tag, 'replace');
                  , // else if, keep 1 in the shulker box
                  idx_choice == 'keep',
                     swapped_count += -1;
                     swapped_item_tag:'Count' = 1+'b';
                     put(shulker_tag, 'BlockEntityTag.Items['+replacement_index+']', swapped_item_tag, 'replace');
                  , // else remove that item from the list
                     delete(shulker_tag, 'BlockEntityTag.Items['+replacement_index+']');
                  );
                  inventory_set(inventory, islot, 1, shulker_item, shulker_tag);
                  inventory_set(player, slot, swapped_count, swapped_id, swapped_tag);
                  return();
               )
            )
         )
      )      
   )
);
