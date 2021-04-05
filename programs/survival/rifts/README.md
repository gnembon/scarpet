# Rifts.
App spawns random portals in the world that can be activated by paying an emerald to activate or to go through. 
Will destroy land around the portals. Creates custom dimension for each spawned portal. Resulting world with 
all its dimensions can still be used in vanilla and without the app.

## Features of Rifts

[Showcase as an April fools video pretending it's a vanilla update](https://youtu.be/gkhHWqD5Hc0)

[Basic showcase how to use basic Rifts commands](https://youtu.be/MsgCy8tFfRw)

[General information on how to install scarpet apps](https://github.com/gnembon/fabric-carpet/wiki/Installing-carpet-scripts-in-your-world)

Extra features that were added after the release of these videos:

(v1.1)
 * You can rename the emerald or a gold nugget on an anvil and that name will be used as a seed for the world. If it's a number
 that number will be used directly as the seed of the world, and if its anything else - the name will be used as a seed instead.
 * Dimensions with the same seed should look the same in every world. Try with 1.16.5 with seeds from 1 to 20 - half of these worlds
 are really something.
 * Elevated the issues with igloos spawning at the top of the world causing 
 the world to crash: [MC-222146](https://bugs.mojang.com/browse/MC-222146). This means that all worlds will now spawn for safety
 with lowered max hights, set 16 blocks below the build limit. This will also allow stuff to show up at the roofs of the nether
 type biomes.

## Mode d'emploi

App can function on its own - in this mode you will be restricted to standard vanilla world types with standard vanilla biomes. 
They still can get wierd, but on its own, the app will not attempt to create fancy custom worlds. Also in 1.17 you are limited
to mix only settings from worlds of the same heights (nether/end and overworlds) due to [MC-220652](https://bugs.mojang.com/browse/MC-220652)

To use custom worlds features as well as customizable world heights (1.17) you must provide a 
valid `vanilla_worldgen...zip` file in the shared apps folder.
This repo should have the required files for 1.16 and 1.17 (21w13a) but a fresh worldgen files 
could be fetched from https://github.com/slicedlime/examples 
and renaming it to include the major game version number.

In 1.17 this allows for custom world heights (although they have to be higher than 0-256
because of [MC-218039](https://bugs.mojang.com/browse/MC-218039)), and in 1.16.5 - for custom biomes. 
Custom funky biomes are currently disabled as of 21w13a
because of [MC-201141](https://bugs.mojang.com/browse/MC-201141), but you can try and risk it yourself by changing 
`global_allow_custom_biomes = true` at the top of the `rifts.sc` file.

## Yeeting of worlds that cause a crash
Custom dimensions is a experimental feature and drawing them randomly cannot make it more stable. 
Some dimensions may cause your game to crash, and it is important to remove them if you encounter it.

If you encounter a crashing world, please report it to the Mojira bug tracker. All dimensions that do load, should work.

To remove the world, exit the game, locate the faulty datapack (might be your newest created one) and remove it.
remove it also from `world/dimensions/rift/...` folder. If you loaded your game already with that world, it will still be
present in the `level.dat` file, but it may not be there. 
Open `level.dat` using NBTExplorer and remove the dimension information from `data/WorldGenSettings/dimensions/...`