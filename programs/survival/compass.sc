__config()->{
    'stay_loaded' -> true,
    'commands' -> {
        'giveCompass' -> _() -> giveCompass(entity_selector('@p'): 0),
        'track <player>' -> ['track'],
        'spawnWith item <item> <amt>' -> ['spawnWithItem', 'âny', false],
        'spawnWith item <item> <amt> <player>' -> ['spawnWithItem', false],
        'spawnWith item <item> <amt> persist' -> ['spawnWithItem', 'âny', true],
        'spawnWith item <item> <amt> <player> persist' -> ['spawnWithItem', true],
        'spawnWith effect <effect> <duration> <amplifier>' -> ['spawnWithEffect', 'âny'],
        'spawnWith effect <effect> <duration> <amplifier> <player>' -> ['spawnWithEffect'],
        'dontSpawnWith item <item>' -> ['dontSpawnWithItem', 'âny'],
        'dontSpawnWith item <item> <player>' -> ['dontSpawnWithItem'],
        'dontSpawnWith effect <effect>' -> ['dontSpawnWithEffect', 'âny'],
        'dontSpawnWith effect <effect> <player>' -> ['dontSpawnWithEffect'],
        'timeToTrack <time>' -> ['timeToTrack'],
        'timeToTrack onUse' -> ['timeToTrack', 'onUse'],
        'logOnTrack <enabled>' -> ['logOnTrack'],
        'maxCompasses <amt>' -> ['maxCompasses'],
        'maxCompasses infinity' -> ['maxCompasses', 'infinity'],
        'noPortalBreaking <enabled>' -> ['noPortalBreaking'],
        'maxNetherTravel <dist>' -> ['maxNetherTravel'],
        'timeToTrack' -> _() -> print(player(), format('l How often the compasses update player positions in seconds: ', 'lb '+(global_timeToTrack/20))),
        'track' -> _() -> print(player(), format('l Make the compass you\'re holding track a player')),
        'spawnWith' -> _() -> print(player(), format('l Spawn with a particular item or effect when anyone or a specified player dies')),
        'dontSpawnWith' -> _() -> print(player(), format('l Stop spawning with a particular item or effect')),
        'logOnTrack' -> _() -> print(player(), format('l Send a message when someone tracks you: ', 'lb '+if(global_logOnTrack, 'true', 'false'))),
        'maxCompasses' -> _() -> print(player(), format('l The maximum amount of compasses one can give themselves: ', 'lb '+global_maxCompasses)),
        'noPortalBreaking' -> _() -> print(player(), format('l Whether to allow breaking portals: ', 'lb '+if(global_noPortalBreaking, 'true', 'false'))),
        'maxNetherTravel' -> _() -> print(player(), format('l The maximum distance players can travel in the overworld thorugh the nether: ', 'lb '+(global_maxNetherTravel*8))),
    },
    'arguments' -> {
        'player' -> {'type' -> 'string', 'suggester' -> _(args) -> entity_selector('@a'), 'case_sensitive' -> false},
        'item' -> {'type' -> 'item'},
        'amt' -> {'type' -> 'int', 'min' -> 1, 'max' -> 64, 'suggest' -> [1, 16, 64]},
        'time' -> {'type' -> 'float', 'min' -> 0.05, 'suggest' -> [0.5, 1, 10]},
        'enabled' -> {'type' -> 'bool'},
        'effect' -> {'type' -> 'effect'},
        'duration' -> {'type' -> 'int', 'min' -> 0, 'max' -> 1000000, 'suggest' -> [15, 60, 1000000]},
        'amplifier' -> {'type' -> 'int', 'min' -> 0, 'max' -> 255, 'suggest' -> [0, 1, 2, 3, 255]},
        'dist' -> {'type' -> 'int', 'min' -> 16, 'max' -> 24000000, 'suggest' -> [8, 128, 7000, 24000000]},
    }
};

toKeep = parse_nbt(read_file('toKeep', 'nbt'));

if (toKeep == 'null',
    toKeep = l();
);

global_toKeep = map(toKeep, l(_: 0, number(_: 1), if(_: 2 == '', null, nbt(_: 2)), _: 3, number(_: 4)));

toKeepEffect = parse_nbt(read_file('toKeepEffect', 'nbt'));

if (toKeepEffect == 'null',
    toKeepEffect = l();
);

global_toKeepEffect = map(toKeepEffect, l(_: 0, number(_: 1), number(_: 2), _: 3));

global_logOnTrack = parse_nbt(read_file('logOnTrack', 'nbt'));

if (global_logOnTrack == 'null',
    global_logOnTrack = false;
);

global_inventoryData = {};

global_timeToTrack = parse_nbt(read_file('timeToTrack', 'nbt'));

if (global_timeToTrack == 'null',
    global_timeToTrack = 10;
);

// Crappy fix for https://github.com/gnembon/fabric-carpet/issues/677
system_variable_set('compass:timeToTrack', global_timeToTrack);

global_maxCompasses = parse_nbt(read_file('maxCompasses', 'nbt'));

if (global_maxCompasses == 'null',
    global_maxCompasses = 'infinity';
);

global_noPortalBreaking = parse_nbt(read_file('noPortalBreaking', 'nbt'));

if (global_noPortalBreaking == 'null',
    global_noPortalBreaking = false;
);

global_maxNetherTravel = parse_nbt(read_file('maxNetherTravel', 'nbt'));

if (global_maxNetherTravel == 'null',
    global_maxNetherTravel = 3000000;
);

__on_player_dies(player) -> (
    slots = l(); // The list of the items that need to be given back when the player respawns

    // Find the slots of all the compasses
    slot = inventory_find(player, 'compass');
    while (slot != null, 41,
        // Only add the compass to the list if it's a tracking compass
        if (inventory_get(player, slot): 2: 'Tracking',
            triple = inventory_get(player, slot);
            put(slots, null, l(triple: 0, triple: 1, triple: 2, slot));
            inventory_set(player, slot, 0);
        );
        slot = inventory_find(player, 'compass', slot+1);
    );

    bySlotNum = map(slots, _: 3); // All the inventory slots that are taken

    // Find all the slots where the player has items that they respawn with
    for (global_toKeep,
        if (_: 3 == 'âny' || _: 3 == lower(player ~ 'name'), // It's impossible to use the character ̂  in a player name
            // Find the first slot with the item
            slot = inventory_find(player, _: 0);

            if (slot == null,
                // If the slot doesn't exist, pick a slot later so it won't be overwritten by other items
                put(slots, null, l(copy(_: 0), copy(_: 1), copy(_: 2), 'findLater'));
            ,
                // If the slot does exist, save it in slots and note that the slot is now taken
                triple = inventory_get(player, slot);
                put(slots, null, l(triple: 0, copy(_: 1), copy(_: 2), slot));
                inventory_set(player, slot, 0);
                put(bySlotNum, null, slot);
            );
        );
    );


    // For all the items that aren't in the player's inventory when they die, pick the first spot that isn't taken
    for (slots,
        if (_: 3 == 'findLater',
            testSlot = 0;

            while (includes(bySlotNum, testSlot), 41, testSlot = testSlot+1);

            _: 3 = testSlot;
            put(bySlotNum, null, testSlot);
        );
    );

    // Save the slots in a variable for when they respawn
    put(global_inventoryData, player ~ 'name', slots);
);

__on_player_respawns(playerOof)->(
    schedule(0, _(outer(playerOof)) -> (
        player = player(playerOof ~ 'name');
        slots = global_inventoryData: (player ~ 'name');

        // When the player respawns, give them the items that are saved in the inventory spots they were when the player died, essentially keepInventory but for certain items
        for (slots,
            inventory_set(player, _: 3, _: 1, _: 0, _: 2);
        );

        // Apply effects
        for (global_toKeepEffect,
            if (_: 3 == 'âny' || lower(_: 3) == player ~ 'name',
                modify(player, 'effect', _: 0, _: 1, _: 2, false);
            );
        );

    );
    );
);

global_playerPos = {};

changeCompassPos() -> {
    // Store the current locations of all the players
    updatePlayerPosition();

    players = entity_selector('@a');

    for(players,
        player = _;

        // Find all the slots with compasses in them
        slot = inventory_find(player, 'compass');
        slots = l();
        while (slot != null, 41,
            put(slots, null, slot);
            slot = inventory_find(player, 'compass', slot+1);
        );
        
        for(slots,
            pointCompass(player, _);
        );
    );

    global_timeToTrack =system_variable_get('compass:timeToTrack');

    // If the timeToTrack is a number, point the compasses at the players after that interval
    if (global_timeToTrack != 'onUse',
        schedule(global_timeToTrack, 'changeCompassPos');
    );
};

updatePlayerPosition() -> (
    players = entity_selector('@a');

    for(players,
        // If the player isn't stored in the playerPos variable, put it there
        if (!has(global_playerPos, lower(_ ~ 'name')),
            put(global_playerPos, lower(_ ~ 'name'), {});
        );

        // Store the position of the player in the dimension they're in
        global_playerPos: lower(_ ~ 'name'): (_ ~ 'dimension') = _ ~ 'pos';
    );
);

pointCompass(player, slot) -> (
    compass = inventory_get(player, slot);

    // Check if the compass is a tracking compass and not just a regular compass
    if (compass: 2: 'Tracking',
        tracking = (compass: 2): 'Tracking';

        p = player(tracking);
        if (p == null,
            // If the player isn't in the game, rename the compass to say so
            compass: 2: 'LodestoneDimension' = 'hoy'; // make the compass spin
            v = compass: 2;
            put(v, 'display.Name', '\'{"text":"'+tracking+' isn\\\'t in the game"}\'');
            compass: 2 = v;
        ,
            pos = global_playerPos: tracking: (player ~ 'dimension');
            
            if (!pos,
                // If the player's online but their position isn't stored in the dimension the player's in, then the player must've neer been to that dimension, so rename the compass to say so
                v = compass: 2;
                put(v, 'LodestoneDimension', 'hoy'); // make the compass spin
                put(v, 'display.Name', '\'{"text":"'+tracking+' has never been to this dimension"}\'');
                compass: 2 = v;
            ,
                v = compass: 2;
                if (p ~ 'dimension' == player ~ 'dimension',
                    put(v, 'display.Name', '\'{"text":"'+tracking+'"}\'');
                ,
                    // Say that the compass is tracking their most recent position to avoid confusion
                    put(v, 'display.Name', '\'{"text":"'+tracking+'\\\'s most recent position in this dimension"}\'');
                );
                put(v, 'LodestoneDimension', (player ~ 'dimension'));
                // Point the compass at the position
                put(v, 'LodestonePos.X', pos: 0);
                put(v, 'LodestonePos.Y', pos: 1);
                put(v, 'LodestonePos.Z', pos: 2);
                compass: 2 = v;
            );
        );

        global_timeToTrack =system_variable_get('compass:timeToTrack');

        // Give some feedback if timeToTrack is set to onUse
        if (global_timeToTrack == 'onUse',
            print(player, format('l Compass pointing to ', 'lb '+tracking));
        );

        // Save the compass data
        inventory_set(player, slot, compass: 1, compass: 0, compass: 2);
    );
);

__on_player_uses_item(player, item_tuple, hand)->(
    // If the player uses a compass when timeToTrack is set to onUse, update the player positions and point the compass at the player
    if (global_timeToTrack == 'onUse' && item_tuple: 0 == 'compass',
        updatePlayerPosition();
        pointCompass(player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
    );

    if (global_noPortalBreaking && includes(l('water_bucket', 'lava_bucket', 'pufferfish_bucket', 'salmon_bucket', 'cod_bucket', 'tropical_fish_bucket', 'axolotl_bucket'), item_tuple: 0),
        pos = query(player, 'trace', 4.5, 'exact') - (player ~ 'look')*0.01;
        // Replace the portal blocks with stone then replace it back so the player can't place water in the portal block
        without_updates(
            neighbors = rect(pos, l(5, 5, 5));
            for (neighbors,
                if (_ == 'nether_portal',
                    state = block_state(_);
                    newPos = pos(_);
                    set(newPos, 'stone');
                    schedule(0, _(outer(newPos), outer(state)) -> (set(newPos, 'nether_portal', state)));
                );
            );
        );
    );

    // Signal the item consumed event if the player used an item that can be consumed
    if (includes(['water_bucket', 'lava_bucket', 'fire_charge', 'item_frame', 'painting', 'pufferfish_bucket', 'salmon_bucket', 'cod_bucket', 'tropical_fish_bucket', 'axolotl_bucket', 'minecart', 'chest_minecart', 'furnace_minecart', 'hopper_minecart', 'bonemeal', 'ender_pearl', 'eye_of_ender', 'firework_rocket', 'tnt_minecart', 'armor_stand', 'name_tag', 'end_crystal', 'splash_potion', 'lingering_potion', 'spruce_boat', 'birch_boat', 'jungle_boat', 'acacia_boat', 'dark_oak_boat'], item_tuple: 0),
        signal_event('item_consumed', player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
    );
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (global_noPortalBreaking && item_tuple: 0 == 'bone_meal' && (block == 'brown_mushroom' || block == 'red_mushroom'),
        blocks = rect(pos(block), l(3, 3, 3));

        // Replace end portal and frame blocks with stone so mushrooms can't overwrite them, then put them back
        for (blocks,
            if (_ == 'end_portal',
                newPos = pos(_);
                set(newPos, 'stone');
                schedule(0, _(outer(newPos), outer(state)) -> (set(newPos, 'end_portal')));
            );

            if (_ == 'end_portal_frame',
                state = block_state(_);
                newPos = pos(_);
                set(newPos, 'stone');
                schedule(0, _(outer(newPos), outer(state)) -> (set(newPos, 'end_portal_frame', state)));
            );
        );
    );
);

global_netherEnter = parse_nbt(read_file('netherEnter', 'nbt'));

if (global_netherEnter == 'null',
    global_netherEnter = {};
);

noPortal4U(player) -> (
    pos = player ~ 'pos';

    if (distance(pos, global_netherEnter: (player ~ 'name')) > global_maxNetherTravel,
        blocks = neighbours(pos);

        for (blocks,
            if (_ == 'nether_portal',
                set(pos(_), 'air');
            );
        );
    );

    schedule(5, 'noPortal4U', player);
);

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> (
    if (to_dimension == 'the_nether',
        put(global_netherEnter, player ~ 'name', to_pos);

        if (global_maxNetherTravel < 3000000,
            noPortal4U(player);
        );
        write_file('netherEnter', 'nbt', encode_nbt(global_netherEnter));
    );
);

__on_player_breaks_block(player, block) -> (
    if (global_noPortalBreaking,
        if (block == 'obsidian',
            pos = pos(block);

            // Check if the obsidian is required for the portal
            if (block(pos+l(0, 1, 0)) == 'nether_portal' || block(pos-l(0, 1, 0)) == 'nether_portal' || (block(pos+l(0, 0, 1)) == 'nether_portal' && block_state(block(pos+l(0, 0, 1)), 'axis') == 'z') || (block(pos-l(0, 0, 1)) == 'nether_portal' && block_state(block(pos-l(0, 0, 1)), 'axis') == 'z') || (block(pos+l(1, 0, 0)) == 'nether_portal' && block_state(block(pos+l(1, 0, 0)), 'axis') == 'x') || (block(pos-l(1, 0, 0)) == 'nether_portal' && block_state(block(pos-l(1, 0, 0)), 'axis') == 'x'),
                // Remove the obsidian without causing updates, then the game handles breaking an air block, then put the obsidian back
                without_updates(
                    set(pos, 'air');
                    schedule(0, _(outer(pos)) -> (set(pos, 'obsidian')));
                );
            );
        );
    );
);

track(toTrack) -> (
    player = player();

    slotNum = player ~ 'selected_slot';

    slot = player ~ 'holds';

    if (slot: 0 != 'compass',
        // If the player isn't holding a compass, say so
        print(player, format('lb '+slot: 0, 'l s can\'t track things'));
    ,
        if (!(slot: 2 ~ 'Tracking'),
            // If the compass is a regular compass, say so
            print(player, format('l This is just a regular compass... ', 'c [Get another compass]', '! /compass giveCompass'));
        ,
            // If logOnTrack is true, tell the player who's being tracked that they're being tracked
            if (global_logOnTrack,
                print(player(toTrack), format('lb '+player, 'l  is tracking you!'));
            );

            // Change the player that the compass is tracking
            slot: 2: 'Tracking' = toTrack;  
            inventory_set(player, slotNum, slot: 1, slot: 0, slot: 2);
            print(format('l Tracking ', 'lb '+toTrack));
        );
    );
);

giveCompass(player) -> (
    canGive = global_maxCompasses == 'infinity';

    if (!canGive,
        // If maxCompasses is a number, find the amount of compasses the player has and check if it's less than the maximum amount
        slot = inventory_find(player, 'compass');
        slots = 0;
        while (slot != null, 41,
            slots = slots + inventory_get(player, slot): 1;
            slot = inventory_find(player, 'compass', slot+1);
        );

        canGive = slots < global_maxCompasses;
    );

    if (canGive,
        // Find all the players that aren't the player receiving the compass
        players = filter(entity_selector('@a'),
            _ != player;
        );

        if (length(players) > 0, 
            if (global_logOnTrack,
                print(player(players: 0), format('lb '+player, 'l  is tracking you!'));
            );

            nbtData = '{Tracking:"'+lower(players: 0 ~ 'name')+'",LodestoneTracked:false,LodestonePos:{X:0,Y:0,Z:0},LodestoneDimension:"overworld",display:{Name:\'{"text":"'+players: if(i, i, 0) ~ 'name'+'"}\'}}';
            
            run('give '+player ~ 'name'+' minecraft:compass'+nbtData+' 1');
        ,
            // If there are no other players online, say so
            print(player, format('l There are no players online'));
        );
    ,
        print(player, format('l You already have the maximum amount of compasses!'));
    );
);

item_consumed(slot) -> (
    item_tuple = inventory_get(player(), slot);
    // Find any items that need to be replenished
    items = filter(global_toKeep, 
        item_tuple: 0 == _: 0 && _: 4
    );

    if (length(items) > 0,
        item = items: 0;
        // If there are items to replenish, schedule a tick to replenish them after the game has handled removing the item
        schedule(0, _(outer(slot), outer(item)) -> (
            inventory_set(player(), slot, item: 1, item: 0, item: 2);
        ));
    );
);

handle_event('item_consumed', 'item_consumed');

__on_player_places_block(player, item_tuple, hand, block)->(
    signal_event('item_consumed', player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
);

global_ids = l('speed', 'slowness', 'haste', 'mining_fatigue', 'strength', 'instant_health', 'instant_damage', 'jump_boost', 'nausea', 'regeneration', 'resistance', 'fire_resistance', 'water_breathing', 'invisibility', 'blindess', 'night_vision', 'hunger', 'weakness', 'poison', 'wither', 'health_boost', 'absorption', 'saturation', 'glowing', 'levitation', 'luck', 'unluck', 'slow_falling', 'conduit_power', 'dolphins_grace', 'bad_omen', 'hero_of_the_village');

__on_player_finishes_using_item(player, item_tuple, hand)->(
    if (item_tuple: 0 == 'milk_bucket' && length(global_toKeepEffect) > 0,
        effects = parse_nbt(query(player, 'nbt', 'ActiveEffects'));
        if (effects != 'null',
            schedule(0, _(outer(effects)) -> (
                for (effects,
                    name = global_ids: (_: 'Id' - 1);

                    if (_: 'Duration' > 32767 && includes(map(global_toKeepEffect, _: 0), name),
                        modify(player(), 'effect', name, _: 'Duration', abs(_: 'Amplifier'), _: 'ShowParticles', _: 'ShowIcon');
                    );
                );
            );
            );
        );
    );
    
    signal_event('item_consumed', player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
);

spawnWithItem(item, amt, player, persistant) -> (
    if (player() ~ 'permission_level' > 1,
        put(global_toKeep, null, l(item: 0, amt, item: 2, player, persistant));
        toKeep = map(global_toKeep, l(_: 0, str(_: 1), if(_: 2, str(_: 2), ''), _: 3, str(_: 4)));
        delete_file('toKeep', 'nbt');
        write_file('toKeep', 'nbt', encode_nbt(toKeep));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

spawnWithEffect(name, duration, amplifier, player) -> (
    if (player() ~ 'permission_level' > 1,
        put(global_toKeepEffect, null, l(name, duration*20, amplifier, player));
        toKeepEffect = map(global_toKeepEffect, l(_: 0, str(_: 1), str(_: 2), _: 3));
        delete_file('toKeepEffect', 'nbt');
        write_file('toKeepEffect', 'nbt', encode_nbt(toKeepEffect));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

dontSpawnWithItem(item, player) -> (
    if (player() ~ 'permission_level' > 1,
        global_toKeep = filter(global_toKeep,
            _: 0 != item: 0 || _: 3 != player;
        );
        toKeep = map(global_toKeep, l(_: 0, str(_: 1), if(_: 2, str(_: 2), ''), _: 3));
        delete_file('toKeep', 'nbt');
        write_file('toKeep', 'nbt', encode_nbt(toKeep));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

dontSpawnWithEffect(name, player) -> (
    if (player() ~ 'permission_level' > 1,
        global_toKeepEffect = filter(global_toKeepEffect,
            _: 0 != name || _: 3 != player;
        );
        toKeepEffect = map(global_toKeepEffect, l(_: 0, str(_: 1), str(_: 2), _: 3));
        delete_file('toKeepEffect', 'nbt');
        write_file('toKeepEffect', 'nbt', encode_nbt(toKeepEffect));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

timeToTrack(val) -> (
    if (player() ~ 'permission_level' > 1,
        if (type(val) == 'string',
            global_timeToTrack = val;
        ,
            if (global_timeToTrack == 'onUse',
                schedule(0, 'changeCompassPos');
            );
            global_timeToTrack = round(val * 20);
        );

        system_variable_set('compass:timeToTrack', global_timeToTrack);

        write_file('timeToTrack', 'nbt', encode_nbt(global_timeToTrack));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

logOnTrack(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_logOnTrack = val;

        write_file('logOnTrack', 'nbt', encode_nbt(global_logOnTrack));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

maxCompasses(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_maxCompasses = val;

        write_file('maxCompasses', 'nbt', encode_nbt(global_maxCompasses));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

noPortalBreaking(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_noPortalBreaking = val;
        delete_file('noPortalBreaking', 'nbt');
        write_file('noPortalBreaking', 'nbt', encode_nbt(global_noPortalBreaking));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

maxNetherTravel(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_maxNetherTravel = floor(val/8);

        write_file('maxNetherTravel', 'nbt', encode_nbt(global_maxNetherTravel));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

includes(list, v) -> {
    for (list,
        if (_ == v,
            return(true);
        );
    );

    return(false);
};

distance(v1, v2) -> (
    sqrt(reduce(v1, _a+(_-(v2: _i))^2, 0));
);

if (global_timeToTrack != 'onUse',
    changeCompassPos();
);