# Fundamentals
Programs for calculating math and other programming operations.

## `hashmap.sc`
#### By: LucunJi(禄存)
An implementation of hash maps, which predates native scarpet maps and sets. Contains functions to create and 
modify maps, which are stored as lists. This implementation onlny works for numbers, however you can add your 
own `_hash` function  to make it work for all types.

## `hashset.sc`
#### By: LucunJi(禄存)
Same deal as with `hashmap.sc`, except for hash sets. You can currently use native scarpet implementation, but 
these also give a good insight into how hash sets and hash maps work.

## `math.scl`
#### By: gnembom
A bunch of useful maths functions, such as `sum()`, `bin()` and `hex()`, as well as the distance functions 
from the math.scl built into carpet mod.

## `min_heap.sc`
#### By: LucunJi(禄存)
An implementation of min heaps in scarpet. This approach shows very well how min heaps work. Heaps are stored 
as lists, and the functions in the library can be used to manipulate them as if they were hash set objects.

## `max_heap.scl`
#### By: Ghoulboy
An implementation of max heaps using `classes.scl`. This implementation works very differently to 
`min_heap.scl`, as here the heap is stored as a map, which represents the object. To use it, you need to 
import the `max_heap()` function from here as well as (at the very least `call_function` from `classes.scl`. 
You can use it by calling the `'insert'` and `'remove'` methods on the object.
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
a parent specifies a field or a method then this class will imherit that unless it specifies that same field 
or method. Note, the last argument takes in the parent classes, not their names, because classes.scl does not 
store classes, it just allows to manipulate them.

To initialize an object, use the `new_object()` function. The first argument is the class variable (returned from the `new_class()` function). The next few arguments are the constructor of the class. This must be declared as an `__init__` method (see below) with any args you wish, except `classes.scl` will throw an error if it is not present. A class can also inherit an `__init__` method from its parent class. The `__init__` method will be called when running the `new_object()` function.

### Declaring a method
When creating a method, as mentioned previously it must take in at least one argument which is the object 
(here referred to as `self`). The method can then do whatever it wants with its arguments. It must then return 
either the `self` object, or a list with `self` as the first argument and a return value as the second. This 
allows a programmer to do `return_value = call_function(object, method, args)` and get a value. Returning the 
object (along with any modifications you have made to it) is important because that way the object will remain 
modified after you're done with it.

To access a class' fields, you just do the same as with a normal map (`object:'field_name'`). This doesn't 
really allow for private fields, however methods will be considered private if they have a `_` prefix. This 
will make `call_function` throw an error if the programmer tries to call them. If you need to call a private 
method within a class, use `call(object:'private_method_name', object, args)`. Note that unlike with 
`call_function`, this will not give neat return values, instead it will either return the object or a list of the object and return value.

### `Object` class
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


