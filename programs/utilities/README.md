# Utilities

Utilities for stuff like showing technical aspects of the game, often to help with farm design testing and showcase or to help check game/player interactions.
i.e., showing chunk loading, and measuring mob lifetimes.

## List of utility apps

Some apps are not fully documented yet, [feel free to contribute by improving the information about the apps here, or even add your own ones!](https://github.com/gnembon/scarpet/edit/master/programs/utilities/README.md).

### [block_counter.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/block_counter.sc)
#### By gnembon

An app to count the amount of each different block in a volume, even showing graphs.

### [camera.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/camera.sc)
#### By gnembon

A mirror of the camera app bundled with Carpet. Allows preparing and navigating cinematic paths with various different options.

### [coord_export.sc](https://github.com/gnembon/scarpet/commits/master/programs/utilities/coord_export.sc)
#### By aria1th

Returns structures' coordinates without generation or loading, reliable than external amidst and very precise.

### [flower.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/flower.sc)
#### By manyrandomthings

Places the flower pattern that bonemeal creates when in flower forests.

### [give_head.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/give_head.sc)
#### By BisUmTo

Adds /give_head command to easly get player_heads.

### [gm.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/gm.sc)
#### By BisUmTo

Adds a simple command that allows deopped players to change their own gamemode.

### [item_counter.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/item_counter.sc)
#### By gnembon

Counts items.

_Missing a proper description!_

### [keepalive.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/keepalive.sc)
#### By gnembon

Makes fake players rejoin the game when restarting the server (or when exiting and opening a singleplayer world).

### [lifetimes.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/lifetimes.sc)
#### By Ghoulboy78

Records the lifetimes of mobs in order to be able to retrieve that information later.

### [loaded_chunks_display.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/loaded_chunks_display.sc)
#### By gnembon

A very outdated version of the built-in `chunk_display` built-in app. Instead of using holograms makes a temporary block
for the display.

_TODO: Update it!_

### [nether_ceiling_backup.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/nether_ceiling_backup.sc)
#### By gnembon

A utility to create a backup of your nether ceiling in order to be able to restore it later. Very useful for getting the 
new nether biomes when updating to 1.16+ without loosing all the stuff on top of the ceiling.

### [reinforcement_zones.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/reinforcement_zones.sc)
### By gnembon

An app that lets you visualize the zones where zombie reinforcements will spawn by right clicking a zombie-like mob with a stick.

### [showstopper.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/showstopper.sc)
#### By altrisi

An app that notifies you or even freezes the game when ticks take too long. Useful to know what happens when your contraption starts lagging
the world, or to find those moments while tick warping, without having to check the tps continuously.

Has multiple modes, including:
- `overNsingle`: Reacts when a single tick takes over N mspt
- `overN`: Reacts when the average of the selected number of ticks in the interval takes over N mspt
- `overPercentage`: Reacts when the average of the selected number of ticks in the interval takes at least the given percentage more then the last 100 tick average

More configuration options are available with the app's command, with config being saved between restarts.

### [sidebar.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/sidebar.sc)
#### By BisUmTo

Adds a simple command that allows deopped players to change the scoreboard on the sidebar.

### [stat.sc](https://github.com/gnembon/scarpet/blob/master/programs/utilities/stat.sc)
#### By CommandLeo

CommandLeo's Statistics Display script. Makes tracking and displaying a lot of statistics really simple.

_Deserves a more detailed description!_
