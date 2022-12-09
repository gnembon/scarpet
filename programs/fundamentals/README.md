# Fundamentals
Programs for calculating math and other programming operations.

## `hashmap.sc`
#### By: LucunJi(禄存)
An implementation of hash maps, which predates native scarpet maps and sets. Contains functions to create and 
modify maps, which are stored as lists. This implementation only works for numbers, however you can add your 
own `_hash` function  to make it work for all types.

## `hashset.sc`
#### By: LucunJi(禄存)
Same deal as with `hashmap.sc`, except for hash sets. You can currently use native scarpet implementation, but 
these also give a good insight into how hash sets and hash maps work.

## `math.scl`
#### By: gnembon
A bunch of useful maths functions, such as `sum()`, `bin()` and `hex()`, as well as the distance functions 
from the math.scl built into carpet mod.

## `min_heap.sc`
#### By: LucunJi(禄存)
An implementation of min heaps in scarpet. This approach shows very well how min heaps work. Heaps are stored 
as lists, and the functions in the library can be used to manipulate them as if they were min heap objects.
You can simply input a list of numbers, and it will treat it as a min heap.

## `max_heap.scl`
#### By: Ghoulboy
An implementation of max heaps using `classes.scl`. This implementation works very differently to 
`min_heap.scl`, as here the heap is stored as a map, which represents the object.
To use it, you must import the `max_heap(max_size)` and `call_function()` functions from here.
The `max_size` parameters determines the maximum size of the resultant max heap.
You can use it by calling the `'insert'` and `'remove'` methods on the object.
Examples of usage can be found in the comments at the bottom of `max_heap.scl`.
For more information on how these work, see below for `classes.scl`.

## `classes.scl`
#### By: Ghoulboy
This is a library which allows the user to add classes into the game. Much like with Python, it works by 
making maps (or dicts) act as classes. Fields are regular map entries, and methods are entries with a string 
key and an anonymous function value. The important part is that the function always has at least 1 argument, 
which is going to be the object itself, so that the function can handle it (see `self` in Python).

### Declaring a class
To register a new class, use `new_class()`. The first argument is the name of the class, which will later be 
stored in the `__name__` field automatically by `classes.scl`. The second argument is the map representing the 
class, its fields and its methods. The last argument is a vararg list of the parent classes of this class. If 
a parent specifies a field or a method then this class will inherit that unless it specifies (thereby overwriting)
that same field or method.
Note that the last argument takes in the list of parent classes, not their names, because classes.scl does not
store classes, it just allows to manipulate them.
Note also that this may change in the future, but would likely require changes in scarpet itself.

To initialize an object, use the `new_object()` function. The first argument is the class variable (returned from 
the `new_class()` function). The next few arguments are the constructor of the class. This must be declared as an 
`__init__` method (see below) with any args you wish, and `classes.scl` will throw an error if it is not 
present. A class can inherit an `__init__` method from its parent class. The `__init__` method will be 
called when running the `new_object()` function.

If a class is not meant to be initialised, then you can initialise it as an interface. To do this, import 
`global_interface_class` from `classes.scl` and put it as a parent class, and don't specify a constructor. If a 
class chooses to implement your interface and doesn't implement a method, it will give an error message 
describing which method they didn't override and where it came from.

### Declaring a method
When creating a method, as mentioned previously it must take in at least one argument which is the object 
(here referred to as `self`). The method can then do whatever it wants with its arguments. It must then return 
either the `self` object, or a list with `self` as the first argument and a return value as the second. This 
allows a programmer to do `return_value = call_function(object, method, args)` and get a value. Returning the 
object (along with any modifications you have made to it) is important because that way the object will remain 
modified after you're done with it.

To create an abstract method within an interface, simply make the function return null, which is how classes.scl
detects if an inheritor class has failed to overwrite the method. Note, however, that this detection will not happen
at compiletime, instead it will happen at runtime when the programmer tries to run the abstract function, and program
will crash loudly. Unfortunately, there is no real way to check for them, so recommendation is always to thoroughly
check that all abstract methods have been declared.

To access a class' fields, you just do the same as with a normal map (`object:'field_name'`). This doesn't 
really allow for private fields, however methods will be considered private if they have a `_` prefix. This 
will make `call_function` throw an error if the programmer tries to call them. If you need to call a private 
method within a class, use `call(object:'private_method_name', object, args)`. Note that unlike with 
`call_function`, this will not give neat return values, instead it will either return the object or a list of the object and return value.

### Built-in classes

#### `Object` class

Classes also have some built-in methods. Much like with Java, they all inherit from the `Object` class. This 
gives them the following methods:
 - `str` : Returns a string representation of the object
 - `hash` : Returns a hash of the object (calculated by default using native scarpet `hash()` function)
 - `number` : Returns a numeric representation of the object (by default length of `str` representation)
 - `class` : Returns the name of this class (also available as `object:'__name__'` field)
 - `equals` : Compares two objects to check if they are equal (by default using scarpet `==` operator)
 - `clone` : Returns an identical copy of this object which is it's own separate object (using underused scarpet 
    `copy()` function)
 - `bool` : Returns a boolean representation of hte object (by default checking whether object's `number` 
    representation is not 0)
 - `compare` : Takes in another object and returns a positive number if this one is greater, negative if it's 
    less and 0 if they are equal (by default by subtracting this object's `number` representation with the other 
    object's `number` representation)
 - `length` : Returns the length of this object (by default the length of its `str` representation)
 - `nbt` : Returns a nbt representation of this object (by default an nbt of `str` representation)
 - `json` : Returns a json representation of this object (by default a json of `str` representation)

#### `Interface` class

Stored internally as `global_interface_class` variable, importing and inheriting it will allow a class to be
declared abstract, meaning an `__init__` method is not necessary, and methods can be left with `null` return
value, meaning the implementer must fill them in.


#### `Iterator` class

Stored internally as `global_iterator_class` variable, importing and inheriting it will give a class a `'for_each'`
method. Note that it is by default an interface and inherits from `Interface` class, The programmer has to define
a `'next'` and `'has_next'`, at which point running the `'for_each'` function will a lambda function will allow to
iterate over whatever the user specifies.

