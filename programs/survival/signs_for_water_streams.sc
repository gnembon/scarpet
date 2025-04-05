// signs_for_water_streams by indoorjetpacks
// updated for scarpet 1.7 by MeeniMc
// Sneak click while placing signs to place it without getting the 'Edit sign message' prompt. Sneak click already skips GUI/interactions for other blocks, why not signs also? Makes sense to me.
// Useful for placing water/lava flows if you make a lot of them. Lookin' at you, SciCraft server.
// tested in Carpet 1.4.24
// https://github.com/gnembon/fabric-carpet/releases
//written for scarpet 1.7 in carpet mod 1.4.15+. Both from the amazing gnembon (although...how'd you get this far without knowing that?)
// namespaces for items and placed signs differ, so map each wall_sign to it's item equivalent

__config() -> {
	'stay_loaded' -> true,
	'scope'->'global'
};
// Namespaces for items and placed signs differ, so map each wall_sign to it's item equivalent.
//
// This version uses regex matching to detect sign items, so it has a chance of supporting datapacks/mods out-of-the-box.
// This may however cause problems where non-sign items are detected as signs and exhibit odd behavior in modded MC.
// The original code based on an explicit whitelist remains available in comments and can be reactivated below.
global_item_to_block = {
  'acacia_sign'-> 'acacia_wall_sign',
  'birch_sign'-> 'birch_wall_sign',
  'dark_oak_sign'-> 'dark_oak_wall_sign',
  'jungle_sign'-> 'jungle_wall_sign',
  'oak_sign'-> 'oak_wall_sign',
  'spruce_sign'-> 'spruce_wall_sign',
  'crimson_sign'-> 'crimson_wall_sign',
  'warped_sign'-> 'warped_wall_sign',
  'mangrove_sign'-> 'mangrove_wall_sign'
};

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->(
  l(item,count,nbt) = item_tuple || l ('None', 0, null);

  // Check if player has a sign item + convert it to the wall placed ID name for the set(), check if player sneaks, and make sure the block isn't a storage-type block.
  // Also excludes brewing stands, furnaces, etc - should cover most blocks. can add others if needed.

  //if( has(global_item_to_block:(item_tuple:0)) // commented out, swap it in to use enumerated sign items from the map above.
  if( (wood = item_tuple:0~'(.*)_sign$')
  &&   player ~ 'sneaking'
  &&   (inventory_size(pos(block))==null),
    // Make sure the player clicked on the side of a block, not the top or looking upward.
    if( face != 'up' && face != 'down',
      clicked_block_pos = map(pos(block), str('%.2f',_));
      offset_by_one = pos_offset(block,face,1); // Without the offset, the block you clicked on will be turned into a sign.
      // Make sure there's a replaceable block there, otherwise you could auto-destroy blocks with this.
      replaceable_materials = m('air', 'water', 'lava', 'vegetation', 'sea_grass');
      if( has(replaceable_materials:(material(offset_by_one))),
        sign = wood+'_wall_sign';
        set(offset_by_one, sign, 'facing', face); // place the sign
		for(diamond(offset_by_one), update(_));
        // Use one from the hand, but only if not in creative
        if( !player~'gamemode_id'%2,
          inventory_set(player, if(hand=='mainhand', player~'selected_slot', 40), count-1)
        )
      )
    )
  )
)
