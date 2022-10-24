//A script to find all blocks of a specific type in a cubic area defined by a radius
//locate prints the first 100, and you can tp to those locations
//hist gives a distribution over defined y values
//By: Ghoulboy

__config()->{
	'commands'->{
		''->_()->print('Root call'),
		'locate <radius_int> <block>'->['locate',null],
		'locate <radius_int> <block> <pos>'->'locate',
		'hist <radius_int> <block>'->['hist',null],
		'hist <radius_int> <block> <pos>'->'hist',
	}
};

locate(r,block,c_pos)->(
	[cx, cy, cz] = c_pos||pos(player());
	i=0;
	scan([cx, cy, cz], [r, r, r],
		if(_==block,
			if(i<100,
			    pos = pos(_);
				print(player(), format('gi '+pos,str('!/script run modify(p,\'pos\',%s)',str(pos))))
			);
			i+=1
		)
	);
	print(player(), format('gi Block: '+block+' was found '+i+' times'))
);

hist(r,block,c_pos)->(
	[cx, cy, cz] = c_pos||pos(player());
	blocks={};
	scan([cx,cy,cz],[r,r,r],
		if(_==block,blocks:_y+=1)
	);
	for(blocks,
		if(blocks:_<40,
			print(_+': '+'*'*blocks:_+'	'+blocks:_+' times'),
			print(_+': '+'*'*30+'	'+blocks:_+' times')
		)
	)
)
