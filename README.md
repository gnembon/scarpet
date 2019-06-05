# scarpet
Public Repository of scarpet programs for Minecraft

## What's new in v1.3
 - Added 1.14 specific structures to plop for 1.14 (fabric-carpet)
 - added `destroy` and `harvest` functions for other valid methods of breaking blocks
 - added `schedule` to delay execution of user defined functions for later ticks
 - scarpet now allows to intercept most important user actions related events. Check `utilities\event_test.sc` and load via `/script load event_test` to test event handling.

## What's new in v1.2
 - added put(list, index, values....) and reworked how lists are handled
 - fixed issue with running 'run' command in functions
 - scripts can now be loaded from files in /scripts/ folder inside world directory. These also can be edited normally, without extra $ to indicate new lines.
 - each file represents a package and is run independently from others.
 - /script load/unload <package>
 - /script in <package> run/invoke etc...
 - script modules loaded from scripts / bundled with carpetmod can make new commands. details - later.

## What's new in v1.1
 - `_` in loop functions can be used to change content of sublists
 - minor performance improvements
 - scripting engine will reset with world loading (changing worlds in singleplayer)
 - typos with gragon eggs
 - player movement is much smoother when controlled with `modify` function
 - `loaded` and `loaded_ep` won't load the points, lol
 - if executed as `/execute as @p script run... ` print will output to players chat, not system log
 - no need to disable commandBlockOutput - scripts will no longer output results of intermediate results.

## What's new in v1.0
 - Everything (check the docs)