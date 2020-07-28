//A script to find all blocks of a specific type in a cubic area defined by a radius
//locate prints the first 100, and you can tp to those locations
//hist gives a distribution over defined y values
//By: Ghoulboy
__command()->(print(''));

locate(r,cx,cy,cz,block)->(
	i=0;
	scan([cx, cy, cz], [r, r, r],
		if(_==block,
			if(i<100,
			    pos = pos(_);
				print(format('gi '+pos,str('!/script run modify(p,\'pos\',%s)',str(pos))))
			);
			i+=1
		)
	);
	'Block: '+block+' was found '+i+' times'
);

hist(r,cx,cy,cz,block)->(
	blocks={};
	scan([cx,cy,cz],[r,r,r],
		if(_==block,blocks:_y+=1)
	);
	for(blocks,
		if(blocks:_<40,
			print(_+': '+'*'*blocks:_+'	'+blocks:_+' times'),
			print(_+': '+blocks:_+' times')
		)
	)
)
