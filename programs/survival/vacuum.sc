//first shown on Xisumavoid's YouTube channel
//https://youtu.be/FMu8T8KriQY
__config() ->
(
	return(m(l('scope','player'),l('stay_loaded',true)))
);

__move_items() ->
(
	loop(41,
		//check to see if the item is a single shulker box named 'Vacuum'
		if(inventory_get(player(), _):0 ~ 'shulker_box' && parse_nbt(parse_nbt(inventory_get(player(),_):2):'display':'Name'):'text' == 'Vacuum' && inventory_get(player(),_):1 == 1,
			shulkerSlot = _;
			shulkerColor = inventory_get(player(),_):0;
			shulkerNbt = inventory_get(player(),_):2;
			//get shulker inventory as a list
			shulkerInventory = shulkerNbt:'BlockEntityTag.Items[]';
			loop(27,
				itemName = str(shulkerInventory:_:'id');
				itemNbt = shulkerInventory:_:'tag';
				prevCount = shulkerInventory:_:'Count';
				prevSlot = _;
				if(itemName != 'null' && prevCount != stack_limit(itemName),
				  	//only works on items already in inventory
          				itemSlot = inventory_find(player(),itemName);
					if(itemSlot != null && inventory_get(player(),itemSlot):2 == itemNbt,
						itemCount = inventory_get(player(),itemSlot):1;
						if(stack_limit(itemName) < prevCount + itemCount,
							moveAmount = stack_limit(itemName) - prevCount,
							moveAmount = itemCount
						);
						totalCount = prevCount + moveAmount;
						//set shulker inventory slot in list
						shulkerInventory:prevSlot:'Count' = str(totalCount + 'b');
						//set new list value in shulker nbt
						shulkerNbt:'BlockEntityTag.Items' = shulkerInventory;
						//remove item before putting in box to prevent duplication
						inventory_remove(player(),itemName,moveAmount);
						inventory_set(player(),shulkerSlot,1,shulkerColor,shulkerNbt);
					)
				)
			)
		)
	)
);

__on_statistic(player,category,event,value) -> 
(
	if(category == 'picked_up',
		__move_items();
	);
)
