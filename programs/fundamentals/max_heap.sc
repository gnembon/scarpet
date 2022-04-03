import('classes', 'new_class', 'new_object', 'call_function');

max_heap = new_class('MaxHeap', {
    '__init__'->_(self, max_size)->(
        self:'max_size' = max_size;
        self:'size' = 0;
        _Heap = [];
        loop(max_size, _Heap+=0);
        self:'_Heap' = _Heap;
        self:'_FRONT' = 0;
        self
    ),
    '_parent'->_(self, pos)->[self, bitwise_shift_right(pos, 1)],
    '_leftChild'->_(self, pos)->[self, bitwise_shift_left(pos, 1)],
    '_rightChild'->_(self, pos)->[self, bitwise_shift_left(pos, 1) + 1],
    '_isLeaf'->_(self, pos)->[self, pos*2 > self:'size'],
    '_swap'->_(self, fpos, spos)->(
        temp = self:'_Heap':fpos;
        self:'_Heap':fpos = self:'_Heap':spos;
        self:'_Heap':spos = temp;
        self
    ),
    '_maxHeapify'->_(self, pos) -> (
        //Getting all this stuff here cos it doesn't change and clutters up the code like crazy
        left = call(self:'_leftChild', self, pos):1;
        right = call(self:'_rightChild', self, pos):1;
        selfpos = self:'_Heap':pos;
        leftchild = self:'_Heap':left;
        rightchild = self:'_Heap':right;
        if(!call(self:'_isLeaf', self, pos):1 && (selfpos < leftchild || selfpos < rightchild),
            heapifypos = if(leftchild>rightchild, left, right);
            call(self:'_swap', self, pos, heapifypos);
            call(self:'_maxHeapify', self, heapifypos)
        );
        self
    ),
    'insert'->_(self, element) -> (
        if(self:'size' < self:'max_size',
            self:'size' += 1;
            self:'_Heap':(self:'size') = element;
            current = self:'size';
            while(self:'_Heap':current > self:'_Heap':(call(self:'_parent', self, current):1), self:'max_size',
                call(self:'_swap', self, current, call(self:'_parent', self, current):1);
                current = call(self:'_parent', self, current):1
            );
        );
        self
    ),
    'remove'->_(self)->(
        popped = self:'_Heap':(self:'_FRONT');
        self:'_Heap':(self:'_FRONT') = self:'_Heap':(self:'size');
        self:'size' += -1;
        call(self:'_maxHeapify', self, self:'_FRONT');
        [self, popped]
    ),
});

//This is an example of using a MaxHeap
//heap = new_object(max_heap, 10);
//print(heap:'_Heap');
//call_function(heap, 'insert', 1);
//print(heap:'_Heap');
//call_function(heap, 'insert', 3);
//print(heap:'_Heap');
//call_function(heap, 'insert', 3);
//print(heap:'_Heap');
//print(call_function(heap, 'remove'));
//print(heap:'_Heap');
//call_function(heap, 'insert', 5);
//print(heap:'_Heap');
//call_function(heap, 'insert', 6);
//print(heap:'_Heap');
//print(call_function(heap, 'remove'));
//print(heap:'_Heap');
//
//print(heap);