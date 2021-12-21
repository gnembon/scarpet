__config() ->
(
	{
		['scope','player'],
		['stay_loaded',true],
		
		['commands',
			{
				['','__guide'],
				['<mode>','__change']
			}
		],

		['arguments',
			{
				['mode',
					{
						['type','term'],
						['options',
							['off','air','solid']
						],
						['suggest',
							['off','air','solid']
						]
					}
				]
			}
		]
	}
);

//setup the script's data if nothing is there already
//it's an empty map, so players without data will have a null mode
__on_start() ->
(
	if(!load_app_data(),
		store_app_data({});
	)
);

//prints script explanation when command is called with no arguments
__guide() ->
(
	print(player(),
		join('\n',
			[
				'Reorients player when travelling through a nether portal',
				' /portalorient off   - does not change player orientation',
				' /portalorient air   - face side with more air blocks',
				' /portalorient solid - face side with fewer solid blocks'
			]
		)
	)
);

//set a new value for the player's mode from the command
__change(mode) ->
(
	data = load_app_data();
	data:str(player()) = mode;
	store_app_data(data);
);

//when a player goes through a portal, change their yaw if they don't have
//	it turned off and travelled to or from the nether
__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) ->
(
	//get player and their current mode
	p = player;
	mode = load_app_data():str(p);

	//only trigger under certain conditions
	if(mode != 'off' && mode != null && (from_dimension == 'the_nether' || to_dimension == 'the_nether'),
		//round the player's position
		center = map(to_pos,floor(_));

		//get the portal axis
		if(block_state(block(center)):'axis' == 'x',
			axis = 0,
			axis = 2;
		);

		//get the corners of the portal and construct an offset to scan each side
		offset = [0,0,0];
		offset:(2 - axis) = 1;
		corners =  __corners(center,offset);

		//scan each side of the portal for blocks based on the player's chosen mode
		//	air -> face the side with more air blocks
		//	solid -> face the side with less solid blocks
		sidelist = l[];
		loop(2,
			sign = 2 * _ - 1;
			sidelist += volume(corners:0 + sign * offset,corners:1 + sign * offset,
				if(mode == 'air',
					air(_),
					mode == 'solid',
					1 - solid(_);
				);
			);
		);

		//compare the two sides to determine which direction the player should face
		if(sidelist:0 > sidelist:1,
			yaw = -45 * axis + 180;
			body_yaw = yaw,
			sidelist:1 > sidelist:0,
			yaw = -45 * axis;
			body_yaw = yaw,
			//if the two sides had matching numbers of valid blocks, don't change anything
			yaw = p ~ 'yaw';
			body_yaw = p ~ 'body_yaw'
		);

		//modify the player's head and body yaw
		modify(p,'yaw',yaw);
		modify(p,'body_yaw',body_yaw);
	)
);

//tracks portal blocks along a diagonal until it encounters an edge,
//	and then travels along the edge to find the corner
//this is faster than using scan() on the portal(20 blocks up, down, left, and right) to
//	find the corners because it checks less than 50 blocks instead of 1000+ blocks
//according to profile_expr(), it's about 10x faster,
//this is also faster than scanning a single row up, down, left, and right because using
//	the diagonal checks two directions at once
//according to profile_expr(), it's about 2.5x faster
__corners(center,offset) ->
(
	corners = [];
	//use the vector normal to the portal plane to construct a vector along the portal plane 
	corneroffset = [1,1,1] - offset;
	//we'll need to check along each direction of the diagonal to find both corners
	loop(2,
		//-1 for negative direction, +1 for positive direction
		listdirection = 2 * _ - 1;
		//generate a list of blocks for checking later
		//do it in order to prevent need for sorting
		pos = copy(center);
		//don't construct lists with rect() because this is much faster and no sorting is needed
		check = map([range(21)],block(_ * listdirection * corneroffset + pos));

		//first find the edges
		edge = __lastportal(check,copy(corneroffset),listdirection);
		//we know the edge, but what direction do we go in?
		//we should check which direction was the problem
		verticalbool = block(edge + listdirection * [0,1,0]) != 'nether_portal';
		horizontalbool = block(edge + listdirection * (corneroffset - [0,1,0])) != 'nether_portal';
		if(verticalbool && horizontalbool,
			//no blocks left that are a nether portal
			//stop
			direction = [0,0,0],
			verticalbool,
			//block above or below is not a nether portal
			//start checking horizontally
			direction = corneroffset - [0,1,0],
			horizontalbool,
			//block to left or right is not a nether portal
			//start checking vertically
			direction = [0,1,0]
		);

		//then find the corners
		pos = copy(edge);
		edges = [];
		//again, don't construct lists with rect() because this is much faster and no sorting is needed
		//this function is 2x faster when this list is made loop() instead of rect()
		if(direction != [0,0,0],
			//length(check) - check ~ edge to prevent checking too many blocks
			edges = map([range(length(check) - check ~ edge)],block(_ * listdirection * direction + pos)),
			//if we're already at the corner after the first check, we only need one block
			edges = [block(pos)];
		);

		//find which blocks along the edges are actually the corners
		corners:_ = __lastportal(edges,null,null);
	);
	corners
);

//find last portal block before non portal block from a list of blocks
//also catches edge case of two intersecting portals
__lastportal(blocklist,direction,sign) ->
(
	//can't check the next value if there's only one so just return what we were given
	if(length(blocklist) == 1,
		return(pos(blocklist:0));
	);
	//we won't care about the vertical component when using this
	direction:1 = 0;
	pos(
		first(blocklist,
			//if the next block in the list is not a nether portal, we're at the edge of the portal
			//we could check for obsidian but this way the script doesn't mind if you removed it
			nextbool = blocklist:(_i + 1) != 'nether_portal';
			//if we are travelling along a diagonal, we might be at the intersection of two corners
			//in this case, we should check that the next block vertically or horizontally is a nether portal
			//this is especially important if your resulting position was a corner of the portal
			adjacent = block(pos(_) + (sign || 1) * direction) == 'nether_portal' || block(pos(_) + (sign || 1) * [0,1,0]) == 'nether_portal';
			//if we were moving sideways, we can ignore this aspect and just require the next block in the list
			//	to be a nether portal
			nextbool || (direction && !adjacent)
		);
	)
);
