// when player right clicks with an empty hand on stained glass blob of blocks
// they disappear for a moment and reappear soon

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) ->
(
	if (hand != 'mainhand', return());
	if (item_tuple, return());
	if (!(block ~ 'stained_glass'), return());
	sound('block.lever.click',pos(block), 1, 1.2);
	sound('block.beacon.activate',pos(block), 1, 2);
	__open_sesame(block, pos(block), 32)
);

__open_sesame(block_type, position, ttl) ->
(
	if (ttl < 1, return(0));
	for (neighbours(position), 
		if(_ == block_type,
			schedule(ceil(rand(8)), '__open_sesame', block_type, pos(_), ttl-1)
		)
	);
	set(position, 'air');
	sound('block.beacon.ambient',position, rand(1)+0.5, rand(1)+0.5);
	particle('portal', position);
	particle('smoke', position);
	schedule(80+ceil(rand(40)), '__close_sesame', block_type, position)
);

__close_sesame(block_type, position) -> 
( 
	if (block_type == block(position), return());
	set(position, block_type); 'entity.enderman.teleport';
	for(neighbours(block_type), update(_));
	sound('block.beacon.deactivate',position, rand(1), rand(1)+1);
	particle('cloud', position)
)




