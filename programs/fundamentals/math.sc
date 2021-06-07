
sum(list) -> reduce(list, _a+_, 0);

int(num) -> if (num < 0, ceil(num), floor(num));

//Only integer is accepted, and max is 32 binary digits
bin(num) -> if (num % 1 == 0 && num <= 2147483647 && num >= -2147483648,
	result = '';
	if (num < 0,
		num += 1;
		loop (32, result = 1 + num % 2 + result; num = int(num / 2));
		result = 1 + result;
	, //else	
		loop (32, result = num % 2 + result; num = int(num / 2))
	);
	result
, //else	
	'invalid number'
);

global_INT2HEX = l('0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F');

hex(num) -> if (num % 1 == 0 && num <= 2147483647 && num >= -2147483648,
	result = '';
	if (num < 0,
		num += 1;
		loop (7, result = get(global_INT2HEX, 16 + num % 16) + result; num = int(num / 16));
		result = 'F' + result;
	, // else
		loop (8, result = get(global_INT2HEX, num % 16) + result; num = int(num / 16))
	);
	result
, // else	
	'invalid number'
);

manhattan(vec1, vec2) -> reduce(vec1 - vec2, _a + abs(_), 0);

distance_sq(vec1, vec2) -> reduce(vec1 - vec2, _a + _*_, 0);

distance(vec1, vec2) -> sqrt(reduce(vec1 - vec2, _a + _*_, 0));

dot(vec1, vec2) -> reduce(vec1 * vec2, _a + _, 0);

norm(vec) -> sqrt(reduce(vec, _a + _*_, 0));

normalize(vec) -> vec / sqrt(reduce(vec, _a + _*_, 0));

cross(v, w) -> [v:1*w:2 - v:2*w:1, v:2*w:0 - v:0*w:2, v:0*w:1 - v:1*w:0];

outer_prod(v, w) -> map(w, v*_);

corss_matrix(vec) -> (
	[x, y, z] = vec;
	[[0, -z, y], [z, 0, -x], [-y, x, 0]]
);

// the following methods assume a square matrix indexed by row (i.e, mat:0 is the first row)
mat_times_vec(matrix, vec) -> map(matrix * map(vec, vec), reduce(_, _a+_, 0));

mat_times_mat(mat1, mat2) -> (
	mat2 = transpose(mat2);
	map(mat1,
		i = _i;
		map(mat2,
			j = _i;
			dot(mat1:i, mat2:j)
		)
	)
);

transpose(mat) -> map(mat, i = _i; map(_, mat:_i:i) );

highest_common_factor(num1,num2) -> (
	q=1;
	while(q!=0,num1*num2,
		q=max(num1,num2)-min(num1,num2);
		if(num1>num2,
			num1=q,
			num2=q
		);
	);
	max(num1,num2)
);

lowest_common_multiple(num1,num2) -> num1*num2/highest_common_factor(num1,num2);

// computes HCF for a list of numbers
highest_common_factor_list(list)-> reduce(list, highest_common_factor(_,_a),list:0);

// computes LCM for a list of numbers
lowest_common_multiple_list(list)-> reduce(list, lowest_common_multiple(_,_a), list:0);

// convert yaw, pitch as given by MC into a normalized vector
direction(yaw, pitch) -> [-sin(yaw)*cos(pitch), -sin(pitch), cos(pitch)*cos(yaw)];
