//A script to find all blocks of a specific type in a cubic area defined by a radius
//locate prints the first 100, and you can tp to those locations
//hist gives a distribution over defined y values
//By: Ghoulboy
__command()->(print(''));

locate(r,cx,cy,cz,block)->(
	i=0;
	pos=str(l(cx,cy,cz))-'['-']'-','-',';
	scan(cx, cy, cz, r, r, r,
		if(_==block,
			if(i<100,
				pos=str(l(_x,_y,_z))-'['-']'-','-',';
				run('tellraw @p {"text":"'+pos+'","clickEvent":{"action":"run_command","value":"/tp @s '+pos+'"}}')
			);
			i+=1
		)
	); 
	print('Block: '+block+' was found '+i+' times')
);

hist(r,cx,cy,cz,block)->(
	blocks=m();
	y_max=0;
	y_min=255;
	scan(cx,cy,cz,r,r,r,
		if(_==block,
			blocks:_y+=1;
			if(y_min>_y,y_min=_y);
			if(y_max<_y,y_max=_y)
		)
	);
	for(range(y_min,y_max),
		if(blocks:_<40,
			print(_+': '+'*'*blocks:_+'	'+blocks:_+' times'),
			print(_+': '+blocks:_+' times')
		)
	)
)
