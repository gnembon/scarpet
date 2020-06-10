// functions starting with __ means it is calculating function, which does not returns result for you
//Returns structures' coordinates without generation or loading, reliable than external amidst and very precise
//origin inputs should be specified with chunk coordinates, but outputs are block coordinates


__circle_biome(origin, dist, biomename, include_soul_sand) ->
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

// If all biomes in range are biomename(mainly warped forest) or soul sand valley& include_soul_sand, return 1. Else, return 0.

__get_structure(xz) -> 
(
	l(x,z) = xz;
	return (keys(structure_eligibility(l(x,0,z))));
);

// check structure names without directly loading chunk.


__check_warped_fortress(xz, dist, include_soul_sand) ->
(
	l(x,z) = xz;
	l(alpha, omega)= (get((structure_eligibility(l(x,0,z),'fortress',512)), 'box'));
	mid = (alpha + omega)/2 ;
	l(cx, cy, cz) = mid / 16;
	l(cx, cy, cz) = l(floor(cx), floor(cy), floor(cz));
	return (__circle_biome(l(cx,cz), dist, 'warped_forest', include_soul_sand));
);

// Check coordinate whether it can has fortress, then size of it, and midpoint, and finally check its surrounding biome.

__end_ship(xyz) -> 
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

__find_purpur(xz) -> 
(
	l(x,z) = xz;
	return ((structure_eligibility(l(x,128, z),'end_city'))==1)
);

//Check if coordinate can have end_city


__has_ship(xz) ->
(
	return (
		if(__find_purpur(xz),
			l(x,z) = xz;
			ship = 0;
			ecpdata = get((structure_eligibility(l(x, 62, z), 'end_city', 256)),'pieces');
			ecplocs = l();
			first(ecpdata,
				 if( 
				__end_ship( (get(_,2) - get(_,3) ) ),  ship = 1
				)  
			);
		ship,0)
	)
);

// Merged function to check if coordinate can have end ship








//functions for player


amidst_ship(origin, dist) ->
(
	listed = l();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));
		if(__has_ship(p),
			listed+= p)
	);
	return(listed)	
);
// Returns end cities coordinate which has end ship, from origin within distance(chunk)
// usage: /script in coord_export run amidst_ship( l(0,0), 100) = from chunk origin 0,0, checks 100 chunk distance (+- 1600, +-1600)

amidst_nether(origin, dist) ->
(
	Dict = m(l('ruined_portal',l()),l('fortress',l()),l('bastion_remnant',l()));
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));
		coord = p;
		for(__get_structure(coord),
			if(has(Dict,lower(_)), 
				put( get(Dict, _), null,  coord, 'insert')

			);
		)
	);
	return(Dict)
);
// Returns nether structures'coordinates(ruined portal, fortress, bastion remnant), from origin within distance(chunk)
// usage: /script in coord_export run amidst_nether( l(0,0), 16)  = from chunk origin 0,0, checks 16 chunk distance 

amidst(origin, dist) ->
(
	Dict = m();
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1)); 
		coord = p;
		for(__get_structure(coord),
			if(has(Dict,lower(_)), 
				put( get(Dict, _), null,  coord, 'insert'),
				put( Dict, lower(_), l(coord))
			);
		)
	);
	return(Dict)
);
// Returns structures'coordinates, from origin within distance(chunk)
// usage /script in coord_export run amidst (l(0,0), 100) = from chunk origin 0,0, checks 100 chunk distance


amidst_warped_fortress(origin, dist, finds, checkrange, include_soul_sand) ->
(
	Dict = m(l('fortress',l()));
	found = 0;
	l(ox, oz) = origin;
	for(rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));p=l(get(p,0),get(p,-1));
		coord = p;
		for(__get_structure(coord),
			if(lower(_)=='fortress'&& __check_warped_fortress(coord, checkrange, include_soul_sand), 
				put( get(Dict, _), null,  coord, 'insert');found+=1;if(found>= finds, return (Dict) )

			);
		)
	);
	return(Dict)


);

// Returns Fortress surrounded with warped forest or soul sand valley, finds = fortresses to find, checkrange = surrounding range of biomes(8+ asserts maximum efficiency)
// coordinates input should be chunk positions, dist = chunk range(1 chunk = 16 block range), finds = number to find, checkrange = to vaild range(8 = 128 block)
// If you want to find fortress with warped forest biome only, surrounded by 6 chunk distances, in range of 1000 chunk, from origin (0,0), and find only one anywhere then
// usage : script in coord_export run amidst_warped_fortress (l(0,0), 1000, 1, 6 ,0) = from (0,0) check 1000 range find 1 surrounded by 6 chunk distance with only warped forest
// if you set include_soul_sand 1 then it means you will allow soul sand valley & warped forest in checkrange.


amidst_gold_farm(origin, dist, finds, checkrange) ->
(
	listed = l();
	found = 0;
	l(ox, oz) = origin;
	for (rect(ox, 0, oz,dist, 0, dist),p = pos(_)*16;p=l(get(p,0),get(p,-1));
		if(__circle_biome(p/16, checkrange, 'nether_wastes', false), listed += p;found+=1;if(found > finds, return(listed)))
	);
	return (listed)
);

// Finds places just as 1.15 and before, where nether wastes only exists in checkrange. 
// usage: script in coord_export invoke amidst_gold_farm l(0,0) 1000 1 7 = Find from origin (0,0), for 1000 distances, find one and stop, assert it is surrounded by 7 chunk distances
