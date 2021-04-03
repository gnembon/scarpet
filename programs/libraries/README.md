# Libraries
Programs for adding helpful features not in native scarpet

## Libraries in alphabetical order with creator:

### compass_persist.sc:
#### By Xendergo
```
Adds an item_consumed() function.
This tells compass.sc when your script is about to remove an item, so the compass script can replenish it if necessary.
Nothing will break if you don't remove the item.
Make sure this is called before the item is removed.
You could also use signal_event('compass:item_consumed', player(), slot_number)

The first argument is the player whose inventory you're removing from, if the player is left out, it defaults to the return value of player().
The other arguments are the slots you removing from.
A slot can be a number, 'mainhand', or 'offhand'. You can specify as many slots as you want.
```

### pretty_print.sc:
#### By Xendergo
```
Adds a pretty_print() function which is equivalent to print(), but color codes things by type. Helpful for distinguishing between null & 'null', or maps, nbts, and stringified nbts
pretty_print() can take as many arguments as you want and it will print all of them seperated by spaces
If the first argument is a player or list of players, it will print only to the players specified

The colors used are:
null - purple
number - blue
string - orange
list - yellow
map - red
iterator - Darker yellow
function - magenta
task - grey
block - green
entity - Darker red
nbt - cyan
text - Unchanged, usually formatted already
```