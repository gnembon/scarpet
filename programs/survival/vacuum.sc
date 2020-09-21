//first shown on Xisumavoid's YouTube channel
//https://youtu.be/FMu8T8KriQY

__config() ->
(
	return(
		m(
			l('scope','player'),
			l('stay_loaded',true)
		)
	);
);

__move_items() ->
(
	//loop across player inventory, hotbar, offhand
	loop(41,
	
		//avoid armor slots
		if(_ < 36 || _ == 40,
			boxData = inventory_get(player(), _),
			boxData = null;
		);
		
		//weed out non-shulkers right away
		//no need to try to get data that won't exist
		if(boxData:0 ~ 'shulker_box',
			shulkerNbt = boxData:2;
			
			//The string '(?<=")((?!text)(?!:).*?)[^"]+(?=")' is a
			//regular expression used to find the name of the box.
			//It searches the 'Name' tag ( {"text":"name"} )
			//for a non-greedy result between two quotes
			//that is not 'text' or the colon ':'.
			//This was patched together from stackoverflow answers
			//so improvements are appreciated.
			shulkerName = shulkerNbt:'display':'Name'~'(?<=")((?!text)(?!:).*?)[^"]+(?=")';
			
			//check to see if the item is a single non-empty item named 'Vacuum'
			if(shulkerName == 'Vacuum' && 
				boxData:1 == 1 && 
				shulkerNbt:'BlockEntityTag.Items' != null,
					
				shulkerSlot = _;
				shulkerColor = boxData:0;
				
				//get shulker inventory as a list
				shulkerInventory = shulkerNbt:'BlockEntityTag.Items[]';
				
				if(type(shulkerInventory) == 'nbt',
					shulkerInventory = l(shulkerInventory);
				);
					
				for(shulkerInventory,
				
					//set _i to a variable for use in later loops
					currentIteration = _i;
					
					itemName = str(shulkerInventory:_i:'id');
					itemNbt = shulkerInventory:_i:'tag';
					if(itemName != 'null',
						playerSlot = inventory_find(player(),itemName);
						prevCount = shulkerInventory:currentIteration:'Count';
						
						//loop until we get all occurrences of the item
						//or the current slot fills
						while(playerSlot != null &&
							prevCount != stack_limit(itemName),41,
							
							playerSlotData = inventory_get(player(),playerSlot);
							
							//check to ensure nbt matches
							if(playerSlotData:2 == itemNbt,
								itemCount = playerSlotData:1;
								if(stack_limit(itemName) < prevCount + itemCount,
									moveAmount = stack_limit(itemName) - prevCount,
									moveAmount = itemCount
								);
								totalCount = prevCount + moveAmount;
								
								//set shulker inventory slot in list
								shulkerInventory:currentIteration:'Count' = str(totalCount + 'b');
								
								//remove item before putting in box to prevent duplication
								inventory_remove(player(),itemName,moveAmount);
								
								//reset slot and count as at least one of them changed
								prevCount = shulkerInventory:currentIteration:'Count';
								playerSlot = inventory_find(player(),itemName);
							)
						)
					)
				);
				
				//set new list value in shulker nbt
				shulkerNbt:'BlockEntityTag.Items' = shulkerInventory;
				
				//overwrite old box with new box
				inventory_set(player(),shulkerSlot,1,shulkerColor,shulkerNbt);
				
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
