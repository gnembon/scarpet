# scarpet
Public Repository of scarpet apps for Minecraft. 

All programs are available as is. They might be incorrect, they might be outdated. If you see some problems, either [open a pull request with a fix](https://github.com/gnembon/scarpet/compare) or [report on the bug tracker for someone else to look into it](https://github.com/gnembon/scarpet/issues)

## Documentation and Examples

[Scarpet API docs](https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/Documentation.md)

## How to get scarpet

[Get scarpet with carpet mod](https://github.com/gnembon/fabric-carpet/releases)

## Editor support

[Notepad++](https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/resources/editors/npp/scarpet.xml)

[Intellij IDEA keyword sets](https://github.com/gnembon/fabric-carpet/blob/master/docs/scarpet/resources/editors/idea/)

[Vscode Autocompletion](https://github.com/imurx/vscode-scarpet)

[Atom syntax highlighting](https://github.com/replaceitem/scarpet-language-atom)

## Changelog

### v1.7
 - support for threaded execution via `task`, `task_value`, `task_join`, `task_count`, `task_completed`, `synchronize` and `task_dock`
 - added `import` support - allows to modularize your apps and use libraries (extension `.scl`)
 - structure API to query and modify game structures via `structures`, `structure_set`, `structure_eligibility`
 - POI API to access and make changes to the POI system via `poi` and `set_poi`
 - global system variables for better interapperability via `system_variable_get` and `system_variable_set`
 - noise generation via `perlin` and `simplex`
 - scoreboard API via `scoreboard`, `scoreboard_add`, `scoreboard_remove`, `scoreboard_display`
 - client facing shapes drawing via `draw_shape`
 - ability for scripts to read, load and write to files of with NBT and plain text. Better organization of scripts files
 - API for handling stats via `statistic`s and event `__on_statistic`
 - added game event handlers
   - `__on_player_interacts_with_block`
   - `__on_player_places_block`
   - `__on_chunk_generated`
   - `__on_lightning`
   - `__on_player_takes_damage`
   - `__on_player_deals_damage`
   - `__on_player_dies`
   - `__on_player_respawns`
   - `__on_player_connects`
   - `__on_player_disconects`
   - `__on_player_chooses_recipe`
   - `__on_player_switches_slot`
 - added `__on_close`
 - events with global scope (like `__on_tick`) can only be triggered for apps with global scope.
 - expanded entity api: `permission_level`, `effect`, `breeding_age`, `pose`, `display_name`, `command_name`, `team`, `gamemode`, `jumping`, `jump`, `silent`, `gravity`, `fire`, `hunger`, `saturation`, `exhaustion`, `add_exhaustion`, `nbt`, `nbt_merge`
 - added stack traces to error messages
 - player enderchest access
 - support for `'\n'` and `'\t'` characters in strings
 - added `seed`, `in_slime_chunk`, `reset_chunk`, `without_updates`, `c_for`, `is_chunk_generated`, `world_time`, `day_time`, `inhabited_time`, `last_tick_times`, `spawn_potential`, `portal_cooldown`, `portal_timer`, `add_chunk_ticket`, `reload_chunk`, `view_distance`, `mob_counts`, `unix_time`, `convert_date`, `parse_nbt`, `encode_nbt`, `recipe_data`
 - better support for `break` and `continue` in functions like `scan` and `volume`
 - added support for all new nether blocks, materials, sounds, etc
 - `set_biome` is now y value sensitive. Use 0 in the end and overworld, and actual Y value for the nether
 - lots of bugfixes...



### v1.6
 - scarpet programs loaded via world scripts folders are now referred to as scarpet apps.
 - functions are now proper function values, and are closures thanks to the redefined `outer` call, so they can hold state. Good alternative to global values.
 - added anonymous functions via `_(args) -> body`
 - scripts now ignore redundant and unnecessary extra semicolons - feel free to spam them at the end of expressions, even if not needed.
 - added `call` to call functions directly (by name or lambda reference)
 - added `copy` for deep copy of nested values. With that - all values are now passed by reference in function arguments
 - added `loaded_status()`, `generation_status()` and `chunk_tickets()` to debug loading issues using scarpet.
 - calling `block()` directly on a block value, like in `scan()` will cause to resolve it. Otherwise block values are lazy until they are needed.
 - apps can now bind to the events on the fly, no need to have callbacks defined only on the first code pass
 - entity events are now separated between apps. There can be multiple apps running the same entity at the same time, not knowing about each other.
 - loops and higher order functions now will not accept the `exit` block, since `continue()` and `break()` functions were introduced to use in loops and higher order functions.
 - restored `plop` function for 1.15
 - scarpet markers are cleaned automatically if an app is not loaded.
 - added `replace(str, regex, repl)`, `replace_first(str, regex, repl)`, `escape_nbt`
 - added new entity options, `ai` and `no_clip`

### v1.5
 - added ability for apps to keep their persistent state using `load_app_data` and `store_app_data` functions
 - added maps via `m()` function
 - added proper nbt support via `nbt()` function
 - all containers (lists, maps and nbts) now have unified api via `get`, `has`, `put` and `delete` functions
 - added L-value behaviour to certain functions and operators, like `get`, `has`, `put`, `delete`, `=`, `+=`
 - new `:` operator - highest priority - to access containers elements, alias for `get` function
 - changed priority of `~` match operator to match `:`
 - numbers have to start because of that with a number, so `.123` won't float anymore, use `0.123` instead
 - added `in_dimension(dimension, expr)` to change the execution context for the subexpression, for instance to place blocks in the nether while running a script in the overworld
 - added carpet mod setting `/carpet scriptsAutoload` to automatically load scarpet packages from disk on server / game start which works even with `/script` command fully disabled. However, scipts to stay loaded need to return proper app config by defining `__config()` function. See `/script load` section for details.
 - added `entity_event` to add functions to be called when certain events happen for an entity
 - added four more general use events: `player_releases_item` (bows, aborting actions, controlled client side), `player_finishes_using_item` (item consumed, eating etc. server controlled), `player_drops_item` for single item dropped, and `player_drops_stack` for dropping entire stack.

### v1.4
 - `top` to get the top block at position now requires full block coordinates, but Y value is ignored.
 - Inventories of blocks and entities got their API as well. Check `stack_limit`, `inventory_size`, `inventory_get`, `inventory_set`, `inventory_find`, `inventory_remove` and `drop_item` functions.
 - scripts loaded from disk (via /script load/unload command) will operate in each player space by default, unless loaded with `shared` option.
 - the newline markers for 'in-commandblock' type scripts are now fully parsed, meaning you can have `$` in strings. `$` markers are supported only with in-game cases.
 - support for comments via `//` in scripts loaded from world files. Comments are only allowed in external file scripts.
 - tab completion for typing programs in commands manually (in /script command)
 - unloading/reloading a module removes all event callbacks from that module
 - `return`, `throw`, `exit`, as well as the 'catch' expression of `try` can be omitted, assuming `null`
 - if with no `else` expression will 'else' to `null`
 - `element(list,index)` has been deprecated, and replaced with `get(value,address)`, working on lists as well as nbt constructs
 - there is a new `nbt` type, that behave like string, but can be queried with get more efficiently
 - primitive types of nbt are now converted to primitive scarpet types when queried. This doesn't apply to compound nbts
 - iterators, like `range`, `rect`, can be reused and moved about in a variable.
 - added `type` function to recognized held variable type.
 - `str` format arguments can now be passed in a single list
 - block values now properly handle block entity data, meaning `set`-ting a chest from another location will retain its content.
 - therefore set can also accept custom BE tag?
 - `place_item` to replicate the action of  a player placing a block of item
 - `set_biome` to sets permanently a custom biome at a position. For those missing a custom command to do that in carpet, just use `/script scan 0 0 0 x1 0 z1 x2 0 z2 set_biome(_,'swamp')` for the same effect
 - `block_properties` lists all available block properties at position
 - `block_data` returns nbt of a block at position
 - `property` makes sure all value comes out lowercased.
 - `spawn` function to spawn entities like /summon command but retaining access to them.
 - `entity ~ 'look'` will return a look vector of an entity. No need to math yaw pitch here.
 - `entity ~ 'selected_slot'` to return current selected slot for players
 - `entity ~ 'facing'` returns a facing order of a player
 - `entity ~ 'trace'` ca be used to ray trace blocks, liquids and entities
 - entity `~` sugar can now support 
 - fixed `modify(e,'custom_name',null)` to actually clear custom name
 - `create_marker` and `remove_all_markers` for easy marking I guess
 - fixed 'double evaluation' caused by print function
 - setting various entities motions and positions should update properly to the clients
 - fixed `thoughts too dope` when trying to compare list to a number

### v1.3
 - Added 1.14 specific structures to plop for 1.14 (fabric-carpet)
 - added `destroy` and `harvest` functions for other valid methods of breaking blocks
 - added `schedule` to delay execution of user defined functions for later ticks
 - scarpet now allows to intercept most important user actions related events. Check `utilities\event_test.sc` and load via `/script load event_test` to test event handling.

### v1.2
 - fixes to lists handling
 - fixed issue with running 'run' command in functions
 - scripts can now be loaded from files in /scripts/ folder inside world directory. These also can be edited normally, without extra $ to indicate new lines.
 - each file represents a package and is run independently from others.
 - /script load/unload <package>
 - /script in <package> run/invoke etc...
 - script modules loaded from scripts / bundled with carpetmod can make new commands. details - later.

### v1.1
 - `put` function to modify lists content
 - `_` in loop functions can be used to change content of sublists
 - minor performance improvements
 - scripting engine will reset with world loading
 -  typos with gragon eggs
 - player movement is much smoother when controlled with `modify` function
 - `loaded` and `loaded_ep` won't load the points, lol
 - if executed as `/execute as @p script run... ` print will output to players chat, not system log
 - no need to disable commandBlockOutput - scripts will no longer output results of intermediate results.
 
 ### v1.0
  - everything
