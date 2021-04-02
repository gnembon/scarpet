# Rifts.
App spawns random portals in the world that can be activated by paying an emerald to activate or to go through. 
Will destroy land around the portals. Creates custom dimension for each spawned portal. Resulting world with 
all its dimensions can still be used in vanilla and without the app.

## Yeeting of worlds that cause a crash
Custom dimensions is a experimental feature and drawing them randomly cannot make it more stable. 
Some dimensions may cause your game to crash, and it is impotant to remove them if you encounter it.

If you encounter a crashing world, please report it to the Mojira bug tracker. All dimensions that do load, should work.

To remove the world, exit the game, locate the faulty datapack (might be your newest created one) and remove it.
remove it also from `world/dimensions/rift/...` folder. If you loaded your game already with that world, it will still be
present in the `level.dat` file, but it may not be there. 
Open `level.dat` using NBTExplorer and remove the dimension information from `data/WorldGenSettings/dimensions/...`

## Mode d'emploi
App can function on its own - in this mode you will be restricted to standard vanilla world types with standard vanilla biomes. 
They still can get wierd, but on its own, the app will not attempt to create fancy custom worlds.

To use custom worlds features as well as customizable world heights (1.17) you must provide a valid `vanilla_worldgen...zip` file in the shared apps folder.
This repo should have the required files for 1.16 and 1.17 (21w13a) but a fresh worldgen files could be fetched from https://github.com/slicedlime/examples 
and renaming it to include the major game version number.
