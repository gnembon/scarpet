
// Author:		LucunJi(禄存)
// Reference:	https://www.cnblogs.com/skywang12345/p/3310835.html
// Thanks:		gnembon'

//////////////////////////////////////////////////////
// This is now a programming excercise as scarpet v1.5 
// natively suports map objects via m() function
//////////////////////////////////////////////////////

// HashSet is a simplified version of HashMap, it enables users to get a item by its hashcode quickly
// it requires you to define an item\'s hash-code generator method

// A list is used for items with the same hash-code



// overwrite it for your own script!
// a valid hash-code should be a non-negative integer
_hash(item) -> floor(abs(item % 1024));

// also overwrite it
_equals(item1, item2) -> item1 == item2;

// returns:	a new hashset with a user-defined length
hashset_new(map_length) -> (
	list = l(); 
	loop (map_length, list += l())
);

// returns:	true if the item is found in hashset
//			false if the item is not found
hashset_hasitem(map_name, item) -> (
	hashcode = _hash(item);
	hashcode < length(var(map_name)) && first(get(var(map_name), hashcode), _equals(_, item)) != null
);

// put an item to hashset
// returns:	true if adds a new item,
//			false if an existed item is updated to a new item
hashset_put(map_name, item) -> (
	hashcode = _hash(item);
	
	//expand hashset if cannot keep the new item
	if (hashcode >= length(var(map_name)),
		loop (hashcode - length(var(map_name)) - 1, var(map_name) += l());
		var(map_name) += l(item),
		
		list = get(var(map_name), hashcode);
		//use if() in first() to give true value to break the loop
		if ( first(list, if (_equals(_, item), put(list, _i, item; true))) == null,
			list += item;
			put(var(map_name), hashcode, list)
		)
	)
);

// delete an item
// returns:	true if the item is found and deleted
//			false if the item is not found
hashset_remove(map_name, item) -> (
	hashcode = _hash(item);
	list = get(var(map_name), hashcode);
	
	if (hashcode < length(var(map_name)),
		first(list, 
			if (_ == item, 
				put(list, _i, get(list, -1));
				list = slice(list, 0, length(list) - 1);
				put(var(map_name), hashcode, list);
				true
			)
		) != null
	)
);

// returns: 	a list of all the items in hashset
hashset_items(map_name) -> (
	list = l();
	for (var(map_name), i = _; for(i, list += _));
	list
);