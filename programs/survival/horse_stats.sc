// Print the stats of the horse the player is looking at

__command() -> (
	target = query(player(), 'trace', 4.5, 'entities');
	if (has({'horse', 'donkey', 'mule'}, target~'type'),
		print(format(
			' Health: ', 'd ' + str(query(target, 'attribute', 'max_health')),
			' \ Speed: ', 'd ' + str(query(target, 'attribute', 'movement_speed')),
			' \ Jump: ', 'd ' + str(query(target, 'attribute', 'jump_strength'))
		))
	);
	null
);
