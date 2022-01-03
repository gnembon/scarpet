__config() -> {
        'stay_loaded' -> true};
global_list = {};
__command()->(if(!global_list : (player()~'name'), global_list:(player()~'name')=1;print(player(),'refill ON');, global_list:(player()~'name') = 0;print(player(),'refill OFF');) ;null);

__on_player_uses_item(player, item_tuple, hand) -> 
	refill(player, hand, item_tuple);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
	refill(player, hand, item_tuple);

__on_player_places_block(player, item_tuple, hand, block)->
if (item = player ~ 'holds', refill(player, 'mainhand', item));

__on_player_breaks_block(player, block) -> 
	if (item = player ~ 'holds', refill(player, 'mainhand', item));

__on_player_interacts_with_entity(player, entity, hand) -> 
	if (item = query(player,'holds',hand), refill(player, hand, item));

__on_player_releases_item(player, item_tuple, hand) -> 
	refill(player, hand, item_tuple);

__on_player_finishes_using_item(player, item_tuple, hand) -> 
	refill(player, hand, item_tuple);

__on_player_attacks_entity(player, entity) -> 
(
	// weapon may break, or you may run out of bows in the offhand, possibly
	// can you even attack with the offhand? don't know
	for(l('mainhand', 'offhand'),
		if (item = query(player, 'holds', _), 
			refill(player, _, item)
		)	
	)
);

refill(player, hand, item_tuple) ->
(
	if(player~'player_type' != 'fake' && player~'player_type' !='shadow'&& !global_list : (player()~'name'), return());
	if (!item_tuple, return());
	if (item_tuple:0~'shulker_box', return());
	slot = if(hand=='offhand',40, player ~ 'selected_slot');
	schedule(0, '__refill_endtick', player, item_tuple, slot)
);

__refill_endtick(player, old_item, slot) ->
(
	if(inventory_get(player,slot), return()); // player still has items on that slot - no need to pick new ones
	item_name = old_item:0;
	long_item_name = 'minecraft:'+item_name;
	// first internal inventory slots
	// loose items
	for(range(35, -1, -1), sbox_slot = _;
		if(slot == _, continue(););
		if ( ((inventory_get(player, sbox_slot):0) ~ 'shulker_box') && ((inventory_get(player, sbox_slot):1) == 1) , // found a shulkerbox	
			shulker_box = inventory_get(player, sbox_slot);
			sbox_tag = shulker_box:2;
			path = __recursive_search(sbox_tag, long_item_name, []);
			accessor = __parse_accessor(path);
			if (!accessor, continue(););
			itemFound = get(sbox_tag, accessor);
			inventory_set(player,slot, itemFound:'Count', item_name, itemFound:'tag');
			delete(shulker_box:2, accessor);
			inventory_set(player, sbox_slot, shulker_box:1, shulker_box:0, shulker_box:2);
			return();
		);
	);
	for(range(35, -1, -1), 
		if(slot == _, continue(););
		if ((inv_item = inventory_get(player, _)):0 == item_name, // we found it
			inventory_set(player,slot,inv_item:1, inv_item:0, inv_item:2);
			inventory_set(player, _, null);
			return()		
		)
	);
	// now search in sboxes
);

//expect original shulkerbox tag coming
//get first match or shulker box and return it
__recursive_search(current_tag,long_item_name, accessor) ->
(
	sbox_items = current_tag : 'BlockEntityTag.Items[]';
	if (sbox_items,
		if(type(sbox_items) != 'list', sbox_items = l(sbox_items));
		for (sbox_items,
			if (_:'id' == long_item_name,
				if (length(accessor) == 0, retVal = [_i], retVal = slice(accessor, 0, -1);put(retVal, null, _i););
				return(retVal);
			);
			if (_:'id' ~ 'shulker_box',
				if(length(accessor) == 0, new_accessor = [_i], new_accessor = slice(accessor, 0, -1); put(new_accessor, null, _i));
				result = __recursive_search(_:'tag', long_item_name, new_accessor);
				if(result != false,
					return(result);
				)
			);
		)
	);
	return (false);
);

__parse_accessor(accessor) ->
(
	if(!accessor || length(accessor) == 0, return(false));
	path = '';
	for (accessor, 
		path = path + '.tag.BlockEntityTag.Items['+_+  ']';
	);
	return (slice(path, 5, -1));
)
