# Survival scarpet apps
Various scripts that modify various game elements, often replicating popular modded features, intended for use in (but not limited to) survival worlds.

## Survival scarpet apps in alphabetical order with creator:

### [angel_block.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/angel_block.sc):
#### By "Pegasus Epsilon" <pegasus@pimpninjas.org>
	Reimplementation of Angel Blocks from RandomThings mod in scarpet 1.4.
	Angel Block allows players to place a block in the middle of the air or water without having to place any support blocks which is especially helpful when trying to build in mid air or water.

### [auto_lighter.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/auto_lighter.sc):
#### By gnembon
	When the player right clicks with a torch looking into the air and not on a block, this will start to send
	out torches and light up the caves in the 128 block sphere around the player. If the player is in
    survival, it will yeet the torches from its inventory.

	App is multiplayer compatible.

### [auto_pickup.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/auto_pickup.sc):
#### By gnembon
	There is a video on his channel about this.
	When you break a block, the item gets tp-ed to player, and left on ground if the inventory is full.
	Also works for inventory blocks, delaying the check until they are done to grab the items off the ground.

### [bee_healing.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/bee_healing.sc):
#### By Xendergo
	Slowly heals bees, so you can have them on an island or near water without them drowning as much

### [bridge.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/bridge.sc):
#### By Aplet123
	When holding golden sword(configurable), sneaking and clicking on a block, the blocks in the offhand will be placed.
	Also comes with useful funcs, __set_nbt_in_slot(player, slot, tag_name, tag_value)
	and __get_nbt_value_in_slot(player, slot, tag_name)
	which can be very useful anywhere.

### [bucketstack.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/bucketstack.sc):
#### By rv3r and Firigion
	Buckets act like honey bottles.
	Set custom stack sizes for any bucket type with /bucketstack <bucket> <stacksize> and the buckets will stack when filled or picked up by a player.
	Stacking requires identical nbt data for buckets of water mobs.
	When using a stack of buckets, receive an empty bucket and continue using the stack as usual, just like honey bottles.
	
	Can't work in creative mode.

### [cam.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/cam.sc):
#### By Gnembon
    As /c /s used to be in the olden 1.15- days, just configurable, safe with anti-cheat mechanisms, saving player landing location in
    app nbt's for future use. The whole shabang.

### [carried_totem.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/carried_totem.sc):
#### By KingOfTheClouds
	A Totem of Undying anywhere in the inventory (except in shulker boxes etc.) can activate as if it were held.

### [compass.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/compass.sc)
#### By Xendergo
	https://youtu.be/q1muVOFOM-k
	Adds a whole bunch of features helpful for Dream style minecraft challenges

  Commands:
	/compass giveCompass   	              Give yourself a compass to track people with
	/compass track <player>               Make your compass point at someone
	/compass timeToTrack <time>           The amount of time between when the compasses automatically update. Setting to onUse means the compass will update when you right click with the compass
	/compass maxCompasses <amt>           The maximum amount of compasses a player can give themselves
	/compass logOnTrack <enabled>         Whether to notify players when someone tracks them with the track or giveCompass command
	/compass noPortalBreaking <enabled>   Whether to prevent breaking nether/end portals
	/compass maxNetherTravel <dist>       The maximum distance players are allowed to travel in the overworld via the nether. If someone makes a portal farther than this amount, the portal breaks when they try to go through it. Units are in overworld blocks

	Also some other stuff to do with respawning with stuff, but this description is already really long

### [destructive_lightning.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/destructive_lightning.sc):
#### By Mdaff386
    Once you start it any lightning that strikes, either natural, summoned or a channeling trident,
    a tnt and fireball will be summoned to create a small crater where the lightning strikes.

### [drop_heads.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/drop_heads.sc):
#### By RubberDuck55
	When a player is killed there is a 33% chance of a head to be dropped

### [easier_renweable_sponge.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/easier_renweable_sponge.sc):
#### By Ghoulboy
	If you kill one of each type of fish(Pufferfish, tropical fish, salmon, cod)
	you have a 1/50 chance of getting a wet sponge.
	Can be used to make renewable sponge without the use of lightning RNG manipulation,
	which in 1.14+ is impossible, hence the name easier_renweable_sponge.

### [eyeremover.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/eyeremover.sc):
#### By rv3r
	Shift right-click on a filled end portal frame to empty it and receive the eye of ender. Note that any
	existing portal will break.

	Only really useful in peaceful.

### [hammer.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/hammer.sc):
#### By gnembonmc
	There is a video on his channel about this.
	If you have a stone pick, right click on block to set the area of effect of the hammer(1x1,3x3,5x5).
	Then you can break multiple blocks at the same time, honouring enchantments like fortune or silk touch.
	Useful for hollowing out large areas.

### [holy_hand_grenades.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/holy_hand_grenades.sc):
#### By gnembonmc
	There is a video on his channel about this.
	Reference to Monty Python and the Holy Grail (R.I.P Terry Jones and Graham Chapman).
	Right clicking with fire charge enchanted with any level of power will shoot it, causing blocks to blow up and fly back as
	falling block entities.
	The higher the power, the further back the blocks go.

### [horse_stats.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/horse_stats.sc):
#### By KingOfTheClouds
	The command /horse_stats prints the health, speed and jump attributes of the horse/donkey/mule the player is looking at.

### [inventory_refill.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/inventory_refill.sc):
#### By gnembonmc
	There is a video on his channel about this.
	Right clicking on an inventory and having partially empty slots in your inventory will refill them from the inventory.
	Left clicking will send your items to any unfilled slots in the inventory.
	(Please add a better descreption as right now it just sounds like a less laggy storage_drawers.sc)

### [lava_sponge.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/lava_sponge.sc):
#### By _GieR
    Make sponge work on lava like water.

### [locate_block.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/locate_block.sc):
#### By Ghoulboy
	This will allow you to see how many blocks of a specific type there are in an area around a point.
	locate will tell you first 100 blocks, as well as how many there are in total, and you can tp to them by clicking in chat.
	hist will print a histogram like in gnembon's ancient debris video, but this will accept any block, and if there are less than 	40 blocks for that y level, it will print a nice looking histogram.

### [magic_doors.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/magic_doors.sc):
#### By gnembonmc
	There is a video on his channel about this.
	If you right click with an empty hand on a bunch of stained glass, it will disappear for a moment and then reappear.
	It's an automated glass sliding door but instead of sliding, the door disappears and allows players to walk through for a short period, and then reappears.

### [milkable_sheeps.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/milkable_sheeps.sc):
#### By Gnottero
	Allows the player to milk sheeps using a bucket.

### [nether_poi.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/nether_poi.sc):
#### By Firigion
	When holding an ender eye, all nether portal points of interest will be shown with a marker.
	Useful when slicing portals, update supressing and debugging stuff.
	Run /nether_poi to toggle on or off for each player. Refresh rate and radius are customizable.

### [no_useless_llamas](https://github.com/gnembon/scarpet/blob/master/programs/survival/no_useless_llama.sc)
### By Opsaaaaa
	Have you ever thought "hay i could use a llama" only to find 5 llamas with only 3 slots?
	This app makes every llama have a full sized inventory. Simply click on the llama with a chest. 


### [nopickup.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/nopickup.sc):
#### By KonaeAkira
	Prevents picking up unwanted items (configurable) like rotten flesh from the ground to save inventory space.
	/nopickup add <item> will add <item> to the blacklist
	/nopickup setlimit <item> <count> sets the maximum allowed number of <item>s in inventory.
	/nopickup remove <item> will remove <item> from the blacklist
	/nopickup list will list all blacklisted items for current player
	/nopickup clear will clear the blacklist, allowing the current player to pick up all items normally
	Blacklists are player-bound and are saved even between server restarts
	Requires carpet fabric-carpet-1.16.4-1.4.16+v201105 or above
	
### [playerme.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/playerme.sc):
#### by BisUmTo
	A wrapper around the /player carpet command. It will only allow the caller to affect themselves
	with the player command. This allows for afking, shadowing and such, but not bots, which a lot of
	servers find useful.

### [portalorient.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/portalorient.sc):
#### By rv3r
	Reorients a player after going through a Nether portal. App settings are per-player and default to not affecting player.
	/portalorient off   - does not change player orientation
	/portalorient air   - makes player face toward side with more air blocks
	/portalorient solid - makes player face toward side with fewer solid blocks
	In the event that each side of the portal has a matching number of valid blocks, does not reorient player.

### [potion_master_cleric.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/potion_master_cleric.sc):
#### By Opsaaaaa
	Adds 7 randomized potion trades to the Cleric. 
	It makes Haste, Dolphins Grace, Levitation, Wither, Blindness, Luck, and Mining Fatigue available in survival as potions.
	Each effect can appear as a regular, splash, or lingering potion.
	Once a cleric becomes a Journeyman they can developer 0-3 potion trades. 

### [prospectors_pick.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/prospectors_pick.sc):
#### By gnembonmc
	There is a video on his channel about this.
	If you hold a gold pick with fortune, it do a number of things:
	If you're on the the surface, it will probe in diamond level and show particles for ores, based on the colour.
	If you're in a cave, it will show a direct line ores around you in a small volume.
	Makes branch mining more interesting for sure.
	Fortune 1 shows only iron, coal and redstone.
	Fortune 3 shows diamond, lapis,gold, redstone and emerald, not iron and coal

### [renewable_elytra_trade.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/renewable_elytra_trade.sc):
#### By Firigion
	Renewable elytra through adding a trade to the otherwise useless leatherworker. The trade appears when interacting
	with a letherworker and all conditions for it to appear are met. Said conditions can be totally customizable in the
	app, including the villager being in the end, needing to right click it with an elytra, the elyta being consumed, minimum
	level of the villager and chances of the trade to fail.

### [replace_hotbar.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/replace_hotbar.sc):
#### By gnembonmc
	There is a video on his channel about this.
	If you run out of an item for any reason, it will be refilled from the hotbar, then shulker boxes in the hotbar.
	Useful for large scale building, or afk sand placing around a monument.

### [replace_inventory.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/replace_inventory.sc):
#### By gnembonmc
	There is a video on his channel about this.
	Allows a players inventory slots to be refilled when they run out with same blocks/items. First from the other slots in the players inventory,
	and then from shulker boxes containing them, if any in inventory.
	This makes it useful for large scale building, or afk sand placing around a monument.
	I guess its just like a better version of replace_hotbar.sc

### [revive_coral.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/revive_coral.sc):
#### By Ghoulboy
	You can right click on a dead coral block with a water bottle to revive it, consuming the water.
	The coral will be reset to normal, meaning that it will die again if you don't mine it.
	It's useful in liaison with /carpet renewableCorals rule, as it can be used to simplify a coral block farm.

### [rope_ladders.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/rope_ladders.sc):
#### By BisUmTo
	Right clicking on a ladder with an other one, will extend the existing one down.
	https://youtu.be/Mgz6XEIcpgc

### [shear_corals.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/shear_corals.sc):
#### By Ghoulboy
	You can shear a coral, dead or alive, and there will be 0-5 drops which can be either coral fans or corals.
	This is not op, as if you don't have silk touch, you will have to do this underwater, and at that stage you 
	will likely be doing this while fighting off drowned, crouching on a magma block.
	In creative it will not drop anything, like for any block.

### [shoot_fireball.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/shoot_fireball.sc)
#### By Xendergo
	Right clicking with a fire charge shoots a regular ghast fireball
	Basically a less op & survival friendly version of holy_hand_grenades
	Killing something with this counts as a player kill, as if you redirected a fireball shot by an actual ghast

### [shulkerboxes.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/shulkerboxes.sc):
#### By gnembon
	Shulkerboxes get vacuum and restock ability.
	Vacuum shulkerboxes will automatically suck in items bypassing player main inventory
	Restock/swap shulkerboxes will automatically cause player to restock/swap stacks from their hotbar with items from the shulkerboxes

### [signs_for_water_streams.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/signs_for_water_streams.sc):
#### By indoorjetpacks
	Hold any bucket in your offhand while placing signs will place the sign without getting the 'Edit sign message' prompt.
	Useful for placing water/lava flows if you make a lot of them. Looking at you, SciCraft server.

### [silk_blockstates.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/silk_blockstates.sc):
#### By BisUmTo
	While sneaking, the silk_touch enchantment saves the Blockstates and the Blockdata of mined blocks.
	It doesn't apply to containers and blacklisted blocks.

### [silk_budding_amethyst.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/silk_budding_amethyst.sc):
#### By "Scott Gudeman" (DragonCcrafted87)
	Conversion of silk_spawners.sc
	Allows to silk touch mine budding_amethyst if holding a diamond or netherite pick with silk touch.

### [silk_spawners.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/silk_spawners.sc):
#### By "Pegasus Epsilon" <pegasus@pimpninjas.org>
	Reimplementation of Silk Spawners mod in scarpet 1.5.
	Allows to silk touch mine spawners if holding a diamond or netherite pick with silk touch.
	Keep entity that you are spawning when you place back down.
	Useful to make a crazy n-tuple spider farm or something like that.

### [simply_harvest.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/simply_harvest.sc):
#### By Gnottero
	Allows the player to right-click on a crop to harvest it. The "Fortune" enchantment affects drops

### [skip_night.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/skip_night.sc):
#### By Firigion
	Allows the player to automatically skip the night witout the use of a bed bot. Can be used in bed
	mode or in command mode (toggeled inside the app, default: bed mode). Bed mode will skip the night
	as long as there's a bed in the overworld with the work "skip" in its name. Local difficulty is not
	reset when skipping the night. See this video for a full showcase: https://youtu.be/FY0PwGE0g68.

### [sleep_ignore_fake_players.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/sleep_ignore_fake_players.sc):
#### By Sanluli36li
	Skip the night and no longer required fake players to sleep.
	Different from `OnePlayerSleeping`, still all real player is required on the server to cause night to pass.

### [smasher.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/smasher.sc):
#### By gnembonmc
	There is a video on his channel about this.
	Allows to smash a cave-like hollow in the ground, with random bits jutting out.
	Doesn't drop items, and breaks all blocks, so don't confuse with veinminer.

### [sneak_grow_overpowered.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/sneak_grow_overpowered.sc):
#### By gnembonmc
	This random ticks plants in a 23*23*17 volume around the player.
	Blocks near the player will be random ticked more often.

### [speed.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/speed.sc):
#### By Ghoulboy
    This app calculates speed of a player.
    It will display a number in the scoreboard. Divide said number by 100 to get actual speed.
    Speed toggle in minihud is obviously much better.

### [storage_drawers.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/storage_drawers.sc):
#### By gnembonmc
	There is a video on his channel about this.
	https://www.youtube.com/watch?v=g7Ku73ElDBs
	Cos im too lazy to write all the shit down.

### [updater.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/updater.sc):
#### By Firigion
	Once loaded, right clicking an observer or budded block with a pice of bamboo will trigger or update it.
	Useful to replace some lost functionality of flint and steal or fireball, without creating a fire block.

### [vacuum.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/vacuum.sc):
#### By rv3r
	Puts inventory items into inventory shulker boxes as long as the item is already present.
	Idea first shown by Xisumavoid https://youtu.be/FMu8T8KriQY

### [veinminer.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/veinminer.sc):
#### By gnembonmc
	There is a video on his channel about this.
	The more the sharpness, the more the veinminer, and it doesn't consume hunger.
	Requires player to be sneaking.
	It's as simple as that.

### [very_basic_overworld_wither_cage_finder.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/very_basic_overworld_wither_cage_finder.sc):
#### By gnembonmc
	There is a video on his channel about this.any trade
	It will search in the given radius of blocks for a 3*3 of bedrock, and nothing else.
	It's very basic, hence it's name.

### [villager_auto_trader.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/villager_auto_trader.sc):
#### By Ghoulboy
	When enabled, zombie villagers which can pick up loot, will change any emeralds in their hand for their first villager trade.
	This only works for trades which take only emeralds as a first item, and nothing for the second.
	NB: Zombie villagers will still be hostile and can despawn, so proper care is stilll required.
	Trades happen every 6 seconds, as with piglin bartering.

### [villager_poi.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/villager_poi.sc):
#### By gnembonmc
	Keeps track of villager poi system?
	(Please enter an explanation, as it quite unclear)

### [vines_instant_mine.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/vines_instant_mine.sc):
#### By BisUmTo
	Shears will instant-mine vines (this is vanilla in 1.17+)

### [vortex_effect.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/vortex_effect.sc):
#### By gnembon
	There is a video on his channel about this
	If you hold an ax with Sharpness, stuff will start flying around you at faster and faster speeds
	Vortex 1 is a useful magnet, and Vortex 3 is a crazy random tornado which no-one wants except for a prank or messing around
	I suppose you could use it with TNT to send it smashing into things as a powerful and volatile weapon

### [waypoints.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/waypoints.sc):
#### By Firigion and boyenn
	Adds a server-side waypoints system, where any player can add and teleport to waypoints that any player
	creates. Permissions to	teleport are highly customizable (see https://youtu.be/OF_hY1sTRYY). It also
	features the ability to render a line in direction of a waypoint (see https://youtu.be/0N2qVahMD7s).

### [world_map.sc](https://github.com/gnembon/scarpet/blob/master/programs/survival/world_map.sc):
#### By gnembon
	https://www.youtube.com/watch?v=TqgyvnjEAn4
	Cos gnembon was too lazy to wait for amidst to update.

### Current content creators:
	gnembonmc(obviously)
	Pegasus Epsilon
	Aplet123
	Ghoulboy
	Gnottero
	indoorjetpacks
	rv3r
	Sanluli36li
	RubberDuck55
	Mdaff386
	BisUmTo
	TheCatSaber
	MeeniMc
	KonaeAkira
	KingOfTheClouds
	Firigion
	boyenn
	_GieR
	Opsaaaaa
	Xendergo
	(Many more hopefully!)
