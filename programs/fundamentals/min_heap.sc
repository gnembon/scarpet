
// Author:	LucunJi(禄存玑)
// Reference:	https://blog.csdn.net/lighthear/article/details/79945528
//			https://www.cnblogs.com/jwongo/p/datastructure-heap.html
// Thanks:	gnembon
//
// Heap is designed for appending and popping out min values quickly.
// However, it is slow to search in. An additional HashMap can be used to enhance searching speed.
// This is a min-heap, in which the root is always the min value. A max-heap can be created with some small modifications.
//
// For each node except root node(that is -1), its parent index is floor((index - 1) / 2)
// the left child\'s index * 2 + 1, and right child\'s is index * 2 + 2, if exists.
//
// get methods

heap_get(heap_name, index) -> get(var(heap_name), index);

heap_get_left_index(index) -> index * 2 + 1;

heap_get_right_index(index) -> index * 2 + 2;

heap_get_parent_index(index) -> if (index == 0, -1, floor((index - 1) / 2));

//overwrite it for your own script!
_compare_node(node1, node2) -> node1 - node2;

//heap operations
heap_siftup(heap_name, index) -> (
	temp = heap_get(heap_name, index);
	parent_index = heap_get_parent_index(index);
	//Loop of size 63 should be large enough for most heap sizes. 
	//The actual loops size should be floor(log(length(var(heap_name))) / log(2) + 1)
	while (index > 0 && _compare_node(temp, heap_get(heap_name, parent_index)) < 0, 
		63,
		
		put(var(heap_name), index, heap_get(heap_name, parent_index));
		index = parent_index;
		parent_index = heap_get_parent_index(index)
	);
	put(var(heap_name), index, temp)
);

//append a new node into the heap and then sort it.
heap_add(heap_name, node) -> (
	var(heap_name) += node;
	if (length(var(heap_name)) > 1, heap_siftup(heap_name, length(var(heap_name)) - 1))
);

//sort by sink down a node
heap_siftdown(heap_name, index) -> (
	temp = heap_get(heap_name, index);
	left_index = heap_get_left_index(index);
	right_index = heap_get_right_index(index);
	while (left_index < length(var(heap_name)) && _compare_node(temp, heap_get(heap_name, left_index)) > 0 ||
		right_index < length(var(heap_name)) && _compare_node(temp, heap_get(heap_name, right_index)) > 0, 
		63,
		
		if (_compare_node(heap_get(heap_name, left_index), heap_get(heap_name, right_index)) > 0 && right_index < length(var(heap_name)), 
			put(var(heap_name), index, heap_get(heap_name, right_index));
			index = right_index,
			
			put(var(heap_name), index, heap_get(heap_name, left_index));
			index = left_index
		);
		
		left_index = heap_get_left_index(index);
		right_index = heap_get_right_index(index)
	);
	put(var(heap_name), index, temp)
);

// pop out the min node at the root and then sort the heap.
// returns:	the min node or null if the heap is empty.
heap_pop_min(heap_name) -> (
	if (length(var(heap_name)) == 0,
		null,
		
		min_node = get(var(heap_name), 0);
		if (length(var(heap_name)) == 1, 
			var(heap_name) = l(),
			
			put(var(heap_name), 0, get(var(heap_name), -1));
			var(heap_name) = slice(var(heap_name), 0, length(var(heap_name)) - 1);
			heap_siftdown(heap_name, 0)
		);
		min_node
	)
);

// change a node in heap and then sort it
heap_change_node(heap_name, index, new_node) -> (
	old_node = heap_get(heap_name, index);
	put(var(heap_name), index, new_node);
	if (_compare_node(old_node, new_node) < 0,
		heap_siftdown(heap_name, index),
		
		heap_siftup(heap_name, index)
	)
);

// without using a hashmap or something alike, time of searching in a heap is always slow
// !!! use "~" if just use heap for numbers for much better performance
// returns:	index of first occurrence of a node in heap if found
//			-1 if not found
heap_search(heap_name, node) -> (
	result = -1;
	for (var(heap_name), , if (_compare_node(_, node) == 0, result = _i; true));
	result
);

// remove a node by index and then sort it
heap_remove(heap_name, index) -> (
	if (length(var(heap_name)) == 1,
		var(heap_name) = l(),
		
		old_node = heap_get(heap_name, index);
		put(var(heap_name), index, get(var(heap_name), -1));
		var(heap_name) = slice(var(heap_name), 0, length(var(heap_name)) - 1);
		if (_compare_node(old_node, heap_get(heap_name, 0)) < 0,
			heap_siftdown(heap_name, index),
		
			heap_siftup(heap_name, index)
		)
	)
);

// turns the input list to a sorted heap
heapify(list_name) -> (
	for (range(floor(length(var(list_name)) - 1) / 2, -1, -1),
		heap_siftdown(list_name, _)
	)
);