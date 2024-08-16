// Print the stats of the horse the player is looking at

__command() -> (
	target = query(player(), 'trace', 4.5, 'entities');
	if (has({'horse', 'donkey', 'mule'}, target~'type'),
		print(format(
			' Health: ', 'd ' + str(query(target, 'attribute', 'generic.max_health')),
			' \ Speed: ', 'd ' + str(query(target, 'attribute', 'generic.movement_speed')),
			' \ Jump: ', 'd ' + str(query(target, 'attribute', 'generic.jump_strength'))
		))
	);
	null
);
