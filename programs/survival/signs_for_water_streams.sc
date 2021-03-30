// signs_for_water_streams by indoorjetpacks

// Sneak click while placing signs to place it without getting the 'Edit sign message' prompt. Sneak click already skips GUI/interactions for other blocks, why not signs also? Makes sense to me.

// Useful for placing water/lava flows if you make a lot of them. Lookin' at you, SciCraft server.

// written for scarpet 1.6 in carpet mod 1.3.0. Both from the amazing gnembon (although...how'd you get this far without knowing that?)

// tested in Carpet 1.4.24

// https://github.com/gnembon/fabric-carpet/releases

// namespaces for items and placed signs differ, so map each wall_sign to it's item equivalent

__config() -> {
	'stay_loaded' -> true
};

global_item_to_block = {
	'acacia_sign'-> 'acacia_wall_sign',
	'birch_sign'-> 'birch_wall_sign',
	'dark_oak_sign'-> 'dark_oak_wall_sign',
	'jungle_sign'-> 'jungle_wall_sign',
	'oak_sign'-> 'oak_wall_sign',
	'spruce_sign'-> 'spruce_wall_sign',
	'crimson_sign'-> 'crimson_wall_sign',
	'warped_sign'-> 'warped_wall_sign'
};

global_replaceable = ['air', 'bubble_column', 'cave_air', 'crimson_roots', 'dead_bush', 'fern', 'fire', 'grass', 'large_fern', 'lava','nether_sprout','sea_grass', 'soul_fire', 'structure_void', 'tall_grass', 'vines', 'void_air', 'warped_roots', 'water'];

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->(
	if(//check if player has a sign item + convert it to the wall placed ID name for the set(), check if player sneaks, and make sure the block isn't a storage block
		
		(block_to_place = global_item_to_block:(item_tuple:0)) && player ~ 'sneaking' && inventory_size(pos(block))==null &&
		
		face != 'up' && face != 'down', //make sure the player clicked on the side of a block, not the top or looking upward.

		offset_by_one = block(pos_offset(block,face));

		//without the offset, the block you clicked on will be turned into a sign.

		//make sure there's a replaceable block there, otherwise you could auto-destroy blocks with this.

		if( global_replaceable ~ offset_by_one != null,
			
			set(offset_by_one, block_to_place, 'facing', face);
			
			for(diamond(offset_by_one), update(_));
		)
	)
)
