/script run 
	sum(list) -> reduce(list, _a+_, 0);
	
	int(num) -> if (num < 0, ceil(num), floor(num));
	
	'sorry, only integer is accepted, and max is 32 binary digits';
	bin(num) -> if (num % 1 == 0 && num <= 2147483647 && num >= -2147483648,
		result = '';
		if (num < 0,
			num += 1;
			for (range(32), result = 1 + num % 2 + result; num = int(num / 2), num == 0);
			result = 1 + result,
			
			for (range(32), result = num % 2 + result; num = int(num / 2))
		);
		result,
		
		'invalid number'
	);
	
	global_INT2HEX = l('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F');
	
	hex(num) -> if (num % 1 == 0 && num <= 2147483647 && num >= -2147483648,
		result = '';
		if (num < 0,
			num += 1;
			for (range(7), result = element(global_INT2HEX, 16 + num % 16) + result; num = int(num / 16), num == 0);
			result = 'F' + result,
			
			for (range(8), result = element(global_INT2HEX, num % 16) + result; num = int(num / 16))
		);
		result,
		
		'invalid number'
	);
	
	manhattan(vec1, vec2) -> sum(map(vec1 - vec2, abs(_)));
	
	distance_sq(vec1, vec2) -> sum(map(vec1 - vec2, _*_));
	
	distance(vec1, vec2) -> sqrt(sum(map(vec1 - vec2, _*_)));
	
	dot(vec1, vec2) -> sum(vec1 * vec2);