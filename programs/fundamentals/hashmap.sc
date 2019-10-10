
// Author:		LucunJi(禄存)
// Reference:	https://www.cnblogs.com/skywang12345/p/3310835.html
// Thanks:		gnembon

//////////////////////////////////////////////////////
// This is now a programming excercise as scarpet v1.5 
// natively suports map objects via m() function
//////////////////////////////////////////////////////

// HashMap enables users to get value by a key quickly
// it requires you to define a key's hash-code generator method

// A list is used for items with the same hash-code, and each item is a [<key>, <value>] pair.



// Overwrite it for your own script!
// a valid hash-code should be a non-negative integer
_hash(key) -> floor(abs(key % 1024));
	
// also overwrite it
_equals(key1, key2) -> key1 == key2;
	
//returns:	a new hashmap with a user-defined length
hashmap_new(map_length) -> (
	list = l(); 
	loop (map_length, list += l())
);
	
//get the value by key
// returns:	null if the key does not exists
hashmap_get(map_name, key) -> (
	hashcode = _hash(key);
		
	if (hashcode >= length(var(map_name)),
		null,
			
		// null is treated as false
		item = first(get(var(map_name), hashcode), _equals(get(_, 0), key));
		if (item != null, get(item, 1), null)
	)
);
	
//returns:	true if the key is found in hashMap
//			false if the key is not found
hashmap_haskey(map_name, key) -> (
	hashcode = _hash(key);
	hashcode < length(var(map_name)) && first(get(var(map_name), hashcode), _equals(get(_, 0), key)) != null
);

//hashmap is not good at searching for value without a key,
// just like when you're opening your home's door...
//returns:	true if the key is found in hashMap
//			false if the key is not found
hashmap_hasvalue(map_name, value) -> (
	first(var(map_name), 
		i = _; 
		first(i, get(_, 1) == value) != null
	) != null
);

//put a key and a value to hashmap
// returns:	true if adds a new key-value pair,
//			false if an existed key-value pair is updated to a new value
hashmap_put(map_name, key, value) -> (
	hashcode = _hash(key);
	pair = l(key, value);
	
	//expand hashmap if cannot kep the new key
	if (hashcode >= length(var(map_name)),
		loop (hashcode - length(var(map_name)) - 1, var(map_name) += l());
		var(map_name) += l(pair),
		
		list = get(var(map_name), hashcode);
		
		//use if() in first() to give true value to break the loop
		if ( first(list, if (_equals(get(_, 0), key), put(list, _i, l(key, value); true))) == null,
			list += pair;
			put(var(map_name), hashcode, list)
		)
	)
);

//delete an item by its key
// returns:	true if the key is found and deleted
//			false if the key is not found
hashmap_remove(map_name, key) -> (
	hashcode = _hash(key);
	list = get(var(map_name), hashcode);
	
	if (hashcode < length(var(map_name)),
		first(list, 
			if (_equals(get(_, 0), key), 
				put(list, _i, get(list, -1));
				list = slice(list, 0, length(list) - 1);
				put(var(map_name), hashcode, list);
				true
			)
		) != null
	)
);

//returns: 	a list of all the values in hashmap';
hashmap_values(map_name) -> (
	list = l();
	for (var(map_name), i = _; for(i, list += get(_, 1)));
	list
);