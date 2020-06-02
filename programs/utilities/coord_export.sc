rect_iter(origin, dist) ->
(
	l(x,z) = origin;
	dist = abs(dist);
	res = l();
	l(xmin, xmax) = l(x-dist, x+dist+1);
	l(zmin, zmax) = l(z-dist, z+dist+1);
	c_for(ax=xmin, ax<xmax, ax+=1,
		c_for(az=zmin, az< zmax, az+=1,
			res+=l(16*ax, 16*az)
		)
	);
	return(res)
);


// returns square list to iterate for checking one points per chunk. bigger values consume much RAM. Will fix when iterator(or generator) is implemented.


circle_iter(origin, dist) ->
(
	l(x,z) = origin;
	dist = abs(dist);
	res = l();
	l(xmin, xmax) = l(x-dist, x+dist+1);
	l(zmin, zmax) = l(z-dist, z+dist+1);
	c_for(ax=xmin, ax<xmax, ax+=1,
		c_for(az=zmin, az< zmax, az+=1,
			if( (ax - x)^2 + (az - z) ^2 <= dist ^2,res+=l(16*ax, 16*az)
			)			
		)
	);
	return(res)
);

// similar to rect_iter, but returns if points are in range(circular check)

circle_biome(origin, dist, biomename, include_soul_sand) ->
(
	l(x,z) = origin;
	dist = abs(dist);
	l(xmin, xmax) = l(x-dist, x+dist+1);
	l(zmin, zmax) = l(z-dist, z+dist+1);
	c_for(ax=xmin, ax<xmax, ax+=1,
		c_for(az=zmin, az< zmax, az+=1,
			if( (ax - x)^2 + (az - z) ^2 <= dist ^2 && !( lower(biome(l(16*ax, 0, 16*az)))==lower(biomename) || (include_soul_sand && lower(biome(l(16*ax, 0, 16*az)))==lower('soul_sand_valley'))  ),return(0)
			)			
		)
	);
	return(1)
);

// If all biomes in range are biomename(mainly warped forest) or soul sand valley, return 1. Else, return 0.

get_structure(xz) -> 
(
	l(x,z) = xz;
	return (keys(structure_eligibility(l(x,0,z))));
);

// check structure names without directly loading chunk.


check_warped_fortress(xz, dist) ->
(
	l(x,z) = xz;
	l(alpha, omega)= (get((structure_eligibility(l(x,0,z),'fortress',512)), 'box'));
	mid = (alpha + omega)/2 ;
	l(cx, cy, cz) = mid / 16;
	l(cx, cy, cz) = l(floor(cx), floor(cy), floor(cz));
	return (circle_biome(l(cx,cz), dist, 'warped_forest', 1));
);

// Check coordinate whether it can has fortress, then size of it, and midpoint, and finally check its surrounding biome.

end_ship(xyz) -> 
(
	nxyz = l();
	for ( xyz, 
		nxyz += abs(_)
	);
	l(a,b,c) = nxyz;
	absxyz = m(a,b,c);
	return (has(absxyz , 28)&&has( absxyz , 23)&&has( absxyz , 12))
);

// Check end city component is End ship component.

find_purpur(xz) -> 
(
	l(x,z) = xz;
	return ((structure_eligibility(l(x,128, z),'end_city'))==1)
);

//Check if coordinate can have end_city


has_ship(xz) ->
(
	return (
		if(find_purpur(xz),
			l(x,z) = xz;
			ship = 0;
			ecpdata = get((structure_eligibility(l(x, 62, z), 'end_city', 256)),'pieces');
			ecplocs = l();
			first(ecpdata,
				 if( 
				end_ship( (get(_,2) - get(_,3) ) ),  ship = 1
				)  
			);
		ship,0)
	)
);

// Merged function to check if coordinate can have end ship

amidst_ship(origin, dist) ->
(
	listed = l();
	for(circle_iter(origin,dist),
		if(has_ship(_),
			listed+= _)
	);
	return(listed)	
);
// Returns end cities coordinate which has end ship, from origin within distance(chunk)

amidst_nether(origin, dist) ->
(
	Dict = m(l('ruined_portal',l()),l('fortress',l()),l('bastion_remnant',l()));
	for(rect_iter(origin,dist), 
		coord = _;
		for(get_structure(coord),
			if(has(Dict,lower(_)), 
				put( get(Dict, _), null,  coord, 'insert')
				
			);
		)
	);
	return(Dict)
);
// Returns structures'coordinates(especially ruined portal, fortress, bastion remnant), from origin within distance(chunk)

amidst(origin, dist) ->
(
	Dict = m();
	for(rect_iter(origin,dist), 
		coord = _;
		for(get_structure(coord),
			if(has(Dict,lower(_)), 
				put( get(Dict, _), null,  coord, 'insert'),
				put( Dict, lower(_), l(coord))
			);
		)
	);
	return(Dict)
);
// Returns structures'coordinates, from origin within distance(chunk)


amidst_warped_fortress(origin, dist, finds, checkrange) ->
(
	Dict = m(l('fortress',l()));
	found = 0;
	for(rect_iter(origin,dist), 
		coord = _;
		for(get_structure(coord),
			if(lower(_)=='fortress'&& check_warped_fortress(coord, checkrange), 
				put( get(Dict, _), null,  coord, 'insert');found+=1;if(found>= finds, return (Dict) )
				
			);
		)
	);
	return(Dict)


);

//Returns Fortress surrounded with warped forest or soul sand valley, finds = fortresses to find, checkrange = surrounding range of biomes(8+ asserts maximum efficiency)

// coordinates input should be chunk positions, dist = chunk range(1 chunk = 16 block range), finds = number to find, checkrange = to vaild range(8 = 128 block)

amidst_gold_farm(origin, dist, finds, checkrange) ->
(
	listed = l();
	found = 0;
	for (circle_iter(origin, dist),
		coord = _;if(circle_biome(_/16, checkrange, 'nether_wastes', false), listed += _;found+=1;if(found > finds, return(listed)))
	);
	return (listed)
)

		


