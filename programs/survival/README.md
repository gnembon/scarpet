# Survival scarpet apps
Various scripts that modify various game elements, often replicating popular modded features, intended for use in (but not limited to) survival worlds.

## Survival scarpet apps in alphabetical order with creator:

### angel_block.sc: 
#### By "Pegasus Epsilon" <pegasus@pimpninjas.org>
	Reimplementation of Angel Blocks from RandomThings mod in scarpet 1.4.
	(Please enter an explanation here)
	
### auto_lighter.sc
#### By gnembon
	When the player right clicks with a torch looking into the air and not on a block, this will start to send
	out torches and light up the caves in the 128 block sphere around the player. If the player is in 
    survival, it will yeet the torches from its inventory. 
	
	App is multiplayer compatible.

### auto_pickup.sc:
#### By gnembon
	There is a video on his channel about this.
	When you break a block, the item gets tp-ed to player, and left on ground if the inventory is full.
	Also works for inventory blocks, delaying the check until they are done to grab the items off the ground.

### bridge.sc:
#### By Aplet123
	When holding golden sword(configurable), sneaking and clicking on a block, the blocks in the offhand will be placed.
	Also comes with useful funcs, __set_nbt_in_slot(player, slot, tag_name, tag_value)
	and __get_nbt_value_in_slot(player, slot, tag_name)
	which can be very useful anywhere.
	
### cam.sc:
#### By Gnembon
    As /c /s used to be in the olden 1.15- days, just configurable, safe with anti-cheat mechanisms, saving player landing location in 
    app nbt's for future use. The whole shabang.
	
### easier_renweable_sponge.sc:
#### By Ghoulboy
	If you kill one of each type of fish(Pufferfish, tropical fish, salmon, cod)
	you have a 1/50 chance of getting a wet sponge.
	Can be used to make renewable sponge without the use of lightning RNG manipulation,
	which in 1.14+ is impossible, hence the name easier_renweable_sponge.

### hammer.sc:
#### By gnembonmc
	There is a video on his channel about this.
	If you have a stone pick, right click on block to set the area of effect of the hammer(1x1,3x3,5x5).
	Then you can break multiple blocks at the same time, honouring enchantments like fortune or silk touch.
	Useful for hollowing out large areas.
	
### holy_hand_grenades.sc:
#### By gnembonmc
	There is a video on his channel about this.
	Reference to Monty Python and the Holy Grail (R.I.P Terry Jones and Graham Chapman).
	Right clicking with fire charge enchanted with any level of power will shoot it, causing blocks to blow up and fly back as
	falling block entities.
	The higher the power, the further back the blocks go.
	
### inventory_refill.sc:
#### By gnembonmc
	There is a video on his channel about this.
	Right clicking on an inventory and having partially empty slots in your inventory will refill them from the inventory.
	Left clicking will send your items to any unfilled slots in the inventory.
	(Please add a better descreption as right now it just sounds like a less laggy storage_drawers.sc)

### locate_block.sc:
#### By Ghoulboy
	This will allow you to see how many blocks of a specific type there are in an area around a point.
	locate will tell you first 100 blocks, as well as how many there are in total, and you can tp to them by clicking in chat.
	hist will print a histogram like in gnembon's ancient debris video, but this will accept any block, and if there are less than 	40 blocks for that y level, it will print a nice looking histogram.

### magic_doors.sc:
#### By gnembonmc
	There is a video on his channel about this.
	If you right click with an empty hand on a bunch of stained glass, it will disappear for a moment and then reappear.
	(Please input more detailed explanation)
	
### prospectors_pick.sc:
#### By gnembonmc
	There is a video on his channel about this.
	If you hold a gold pick with fortune, it do a number of things:
	If you're on the the surface, it will probe in diamond level and show particles for ores, based on the colour.
	If you're in a cave, it will show a direct line ores around you in a small volume.
	Makes branch mining more interesting for sure.
	Fortune 1 shows only iron, coal and redstone.
	Fortune 3 shows diamond, lapis,gold, redstone and emerald, not iron and coal

### replace_hotbar.sc:
#### By gnembonmc
	There is a video on his channel about this.
	If you run out of an item for any reason, it will be refilled from the hotbar, then shulker boxes in the hotbar.
	Useful for large scale building, or afk sand placing around a monument.
	
### replace_inventory.sc:
#### By gnembonmc
	There is a video on his channel about this.
	If you run out of an item for any reason, it will be refilled from the inventory, then shulker boxes.
	Useful for large scale building, or afk sand placing around a monument.
	I guess its just like a better version of replace_hotbar.sc
	(Please explain better someone!)
	
### revive_coral.sc:
#### By Ghoulboy
	You can right click on a dead coral block with a water bottle to revive it, consuming the water.
	The coral will be reset to normal, meaning that it will die again if you don't mine it.
	It's useful in liaison with /carpet renewableCorals rule, as it can be used to simplify a coral block farm.
	
### shear_corals.sc:
#### By Ghoulboy
	You can shear a coral, dead or alive, and there will be 0-5 drops which can be either coral fans or corals.
	This is not op, as if you don't have silk touch, you will have to this underwater, and at that stage you will likely be doing this while fighting off drowned, crouching on a magma block.
	In creative it will not drop anything, like for any block.

### signs_for_water_streams.sc:
#### By indoorjetpacks
	Hold any bucket in your offhand while placing signs will place the sign without getting the 'Edit sign message' prompt.
	Useful for placing water/lava flows if you make a lot of them. Looking at you, SciCraft server.
	
### silk_spawners.sc:
#### By "Pegasus Epsilon" <pegasus@pimpninjas.org>
	Reimplementation of Silk Spawners mod in scarpet 1.5.
	Allows to silk touch mine spawners if holding a diamond or netherite pick with silk touch.
	Keep entity that you are spawning when you place back down.
	Useful to make a crazy n-tuple spider farm or something like that.
	
### sleep_ignore_fake_players.sc:
#### By Sanluli36li
	Skip the night and no longer required fake players to sleep.
	Different from `OnePlayerSleeping`, still all real player is required on the server to cause night to pass.

### smasher.sc:
#### By gnembonmc
	There is a video on his channel about this.
	Allows to smash a cave-like hollow in the ground, with random bits jutting out.
	Doesn't drop items, and breaks all blocks, so don't confuse with veinminer.

### sneak_grow_overpowered.sc:
#### By gnembonmc
	This random ticks plants in a 23*23*17 volume around the player.
	Blocks near the player will be random ticked more often.

### speed.sc:
#### By Ghoulboy
    This app calculates speed of a player.
    It will display a number in the scoreboard. Divide said number by 100 to get actual speed.
    Speed toggle in minihud is obviously much better.

### storage_drawers.sc:
#### By gnembonmc
	There is a video on his channel about this.
	https://www.youtube.com/watch?v=g7Ku73ElDBs
	Cos im too lazy to write all the shit down.
	
### vacuum.sc
#### By rv3r
	Puts inventory items into inventory shulker boxes as long as the item is already present.
	Idea first shown by Xisumavoid https://youtu.be/FMu8T8KriQY
	
### veinminer.sc:
#### By gnembonmc
	There is a video on his channel about this.
	The more the sharpness, the more the veinminer, and it doesn't consume hunger.
	Requires player to be sneaking.
	It's as simple as that.
	
### very_basic_overworld_wither_cage_finder.sc:
#### By gnembonmc
	There is a video on his channel about this.
	It will search in the given radius of blocks for a 3*3 of bedrock, and nothing else.
	It's very basic, hence it's name.
	
### villager_poi.sc:
#### By gnembonmc
	Keeps track of villager poi system?
	(Please enter an explanation, as it quite unclear)

### vortex_effect.sc:
#### By gnembon
	There is a video on his channel about this
	If you hold an ax with Sharpness, stuff will start flying around you at faster and faster speeds
	Vortex 1 is a useful magnet, and Vortex 3 is a crazy random tornado which no-one wants except for a prank or messing around
	I suppose you could use it with TNT to send it smashing into things as a powerful and volatile weapon
	
### world_map.sc:
#### By gnembon
	https://www.youtube.com/watch?v=TqgyvnjEAn4
	Cos gnembon was too lazy to wait for amidst to update.

### Current content creators:
	gnembonmc(obviously)
	Pegasus Epsilon
	Aplet123
	Ghoulboy
	indoorjetpacks
	rv3r
	(Many more hopefully!)
