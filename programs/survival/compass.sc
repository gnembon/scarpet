// If you want your script to work with the persist flag, signal the relevant scarpet event
// For example: signal_event('player_places_block', player, player, l('acacia_door', 16, null), 'mainhand', block(0, 0, 0));

__config()->{
    'scope' -> 'global',
    'commands' -> {
        'giveCompass' -> _() -> give_compass(player()),
        'track <player>' -> ['track'],
        'spawnWith item <item> <amt>' -> ['spawn_with_item', 'âny', false],
        'spawnWith item <item> <amt> <player>' -> ['spawn_with_item', false],
        'spawnWith item <item> <amt> persist' -> ['spawn_with_item', 'âny', true],
        'spawnWith item <item> <amt> <player> persist' -> ['spawn_with_item', true],
        'spawnWith effect <effect> <duration> <amplifier>' -> ['spawn_with_effect', 'âny'],
        'spawnWith effect <effect> <duration> <amplifier> <player>' -> ['spawn_with_effect'],
        'dontSpawnWith item <item>' -> ['dont_spawn_with_item', 'âny'],
        'dontSpawnWith item <item> <player>' -> ['dont_spawn_with_item'],
        'dontSpawnWith effect <effect>' -> ['dont_spawn_with_effect', 'âny'],
        'dontSpawnWith effect <effect> <player>' -> ['dont_spawn_with_effect'],
        'timeToTrack <time>' -> ['time_to_track'],
        'timeToTrack onUse' -> ['time_to_track', 'onUse'],
        'logOnTrack <enabled>' -> ['log_on_track'],
        'maxCompasses <amt>' -> ['max_compasses'],
        'maxCompasses infinity' -> ['max_compasses', 'infinity'],
        'noPortalBreaking <enabled>' -> ['no_portal_breaking'],
        'maxNetherTravel <dist>' -> ['max_nether_travel'],
        'timeToTrack' -> _() -> print(player(), format('l How often the compasses update player positions in seconds: ', 'lb '+(global_time_to_track/20))),
        'track' -> _() -> print(player(), format('l Make the compass you\'re holding track a player')),
        'spawnWith' -> _() -> print(player(), format('l Spawn with a particular item or effect when anyone or a specified player dies')),
        'dontSpawnWith' -> _() -> print(player(), format('l Stop spawning with a particular item or effect')),
        'logOnTrack' -> _() -> print(player(), format('l Send a message when someone tracks you: ', 'lb '+if(global_log_on_track, 'true', 'false'))),
        'maxCompasses' -> _() -> print(player(), format('l The maximum amount of compasses one can give themselves: ', 'lb '+global_max_compasses)),
        'noPortalBreaking' -> _() -> print(player(), format('l Whether to allow breaking portals: ', 'lb '+if(global_no_portal_breaking, 'true', 'false'))),
        'maxNetherTravel' -> _() -> print(player(), format('l The maximum distance players can travel in the overworld thorugh the nether: ', 'lb '+(global_max_nether_travel*8))),
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
        'dist' -> {'type' -> 'int', 'min' -> 16, 'max' -> 24000000, 'suggest' -> [16, 128, 7000, 24000000]},
    }
};

to_keep = parse_nbt(read_file('to_keep', 'nbt'));

if (to_keep == 'null',
    to_keep = l();
);

global_to_keep = map(to_keep, l(_: 0, number(_: 1), if(_: 2 == '', null, nbt(_: 2)), _: 3, number(_: 4)));

to_keep_effect = parse_nbt(read_file('to_keep_effect', 'nbt'));

if (to_keep_effect == 'null',
    to_keep_effect = l();
);

global_to_keep_effect = map(to_keep_effect, l(_: 0, number(_: 1), number(_: 2), _: 3));

global_log_on_track = parse_nbt(read_file('log_on_track', 'nbt'));

if (global_log_on_track == 'null',
    global_log_on_track = false;
);

global_inventory_data = {};

global_time_to_track = parse_nbt(read_file('time_to_track', 'nbt'));

if (global_time_to_track == 'null',
    global_time_to_track = 10;
);

global_max_compasses = parse_nbt(read_file('max_compasses', 'nbt'));

if (global_max_compasses == 'null',
    global_max_compasses = 'infinity';
);

global_no_portal_breaking = parse_nbt(read_file('no_portal_breaking', 'nbt'));

if (global_no_portal_breaking == 'null',
    global_no_portal_breaking = false;
);

global_max_nether_travel = parse_nbt(read_file('max_nether_travel', 'nbt'));

if (global_max_nether_travel == 'null',
    global_max_nether_travel = 3000000;
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

    by_slot_num = map(slots, _: 3); // All the inventory slots that are taken

    // Find all the slots where the player has items that they respawn with
    for (global_to_keep,
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
                put(by_slot_num, null, slot);
            );
        );
    );


    // For all the items that aren't in the player's inventory when they die, pick the first spot that isn't taken
    for (slots,
        if (_: 3 == 'findLater',
            test_slot = 0;

            while (includes(by_slot_num, test_slot), 41, test_slot = test_slot+1);

            _: 3 = test_slot;
            put(by_slot_num, null, test_slot);
        );
    );

    // Save the slots in a variable for when they respawn
    put(global_inventory_data, player ~ 'name', slots);
);

__on_player_respawns(playerOof)->(
    schedule(0, _(outer(playerOof)) -> (
        player = player(playerOof ~ 'name');
        slots = global_inventory_data: (player ~ 'name');

        // When the player respawns, give them the items that are saved in the inventory spots they were when the player died, essentially keepInventory but for certain items
        for (slots,
            inventory_set(player, _: 3, _: 1, _: 0, _: 2);
        );

        // Apply effects
        for (global_to_keep_effect,
            if (_: 3 == 'âny' || lower(_: 3) == lower(player ~ 'name'),
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
            point_compass(player, _);
        );
    );

    // If the time_to_track is a number, point the compasses at the players after that interval
    if (global_time_to_track != 'onUse',
        schedule(global_time_to_track, 'changeCompassPos');
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

point_compass(player, slot) -> (
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

        // Give some feedback if time_to_track is set to onUse
        if (global_time_to_track == 'onUse',
            print(player, format('l Compass pointing to ', 'lb '+tracking));
        );

        // Save the compass data
        inventory_set(player, slot, compass: 1, compass: 0, compass: 2);
    );
);

__on_player_uses_item(player, item_tuple, hand)->(
    // If the player uses a compass when time_to_track is set to onUse, update the player positions and point the compass at the player
    if (global_time_to_track == 'onUse' && item_tuple: 0 == 'compass',
        updatePlayerPosition();
        point_compass(player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
    );

    if (global_no_portal_breaking && includes(l('water_bucket', 'lava_bucket', 'pufferfish_bucket', 'salmon_bucket', 'cod_bucket', 'tropical_fish_bucket', 'axolotl_bucket'), item_tuple: 0),
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
        item_consumed(player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
    );
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (global_no_portal_breaking && item_tuple: 0 == 'bone_meal' && (block == 'brown_mushroom' || block == 'red_mushroom'),
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

global_nether_enter = parse_nbt(read_file('netherEnter', 'nbt'));

if (global_nether_enter == 'null',
    global_nether_enter = {};
);

no_portal_4_u(player) -> (
    pos = player ~ 'pos';

    if (distance(pos, global_nether_enter: (player ~ 'name')) > global_max_nether_travel,
        blocks = neighbours(pos);

        for (blocks,
            if (_ == 'nether_portal',
                set(pos(_), 'air');
            );
        );
    );

    schedule(5, 'no_portal_4_u', player);
);

__on_player_changes_dimension(player, from_pos, from_dimension, to_pos, to_dimension) -> (
    if (to_dimension == 'the_nether',
        put(global_nether_enter, player ~ 'name', to_pos);

        if (global_max_nether_travel < 3000000,
            no_portal_4_u(player);
        );
        write_file('netherEnter', 'nbt', encode_nbt(global_nether_enter));
    );
);

__on_player_breaks_block(player, block) -> (
    if (global_no_portal_breaking,
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

track(to_track) -> (
    player = player();

    slot_num = player ~ 'selected_slot';

    slot = player ~ 'holds';

    if (slot: 0 != 'compass',
        // If the player isn't holding a compass, say so
        print(player, format('lb '+slot: 0, 'l s can\'t track things'));
    ,
        if (!(slot: 2 ~ 'Tracking'),
            // If the compass is a regular compass, say so
            print(player, format('l This is just a regular compass... ', 'c [Get another compass]', '! /compass giveCompass'));
        ,
            // If log_on_track is true, tell the player who's being tracked that they're being tracked
            if (global_log_on_track,
                print(player(to_track), format('lb '+player, 'l  is tracking you!'));
            );

            // Change the player that the compass is tracking
            slot: 2: 'Tracking' = to_track;  
            inventory_set(player, slot_num, slot: 1, slot: 0, slot: 2);
            print(format('l Tracking ', 'lb '+to_track));
        );
    );
);

give_compass(player) -> (
    can_give = global_max_compasses == 'infinity';

    if (!can_give,
        // If max_compasses is a number, find the amount of compasses the player has and check if it's less than the maximum amount
        slot = inventory_find(player, 'compass');
        slots = 0;
        while (slot != null, 41,
            slots = slots + inventory_get(player, slot): 1;
            slot = inventory_find(player, 'compass', slot+1);
        );

        can_give = slots < global_max_compasses;
    );

    if (can_give,
        // Find all the players that aren't the player receiving the compass
        players = filter(entity_selector('@a'),
            _ != player;
        );

        if (length(players) > 0, 
            if (global_log_on_track,
                print(player(players: 0), format('lb '+player, 'l  is tracking you!'));
            );

            nbt_data = '{Tracking:"'+lower(players: 0 ~ 'name')+'",LodestoneTracked:false,LodestonePos:{X:0,Y:0,Z:0},LodestoneDimension:"overworld",display:{Name:\'{"text":"'+players: if(i, i, 0) ~ 'name'+'"}\'}}';
            
            run('give '+player ~ 'name'+' minecraft:compass'+nbt_data+' 1');
        ,
            // If there are no other players online, say so
            print(player, format('l There are no players online'));
        );
    ,
        print(player, format('l You already have the maximum amount of compasses!'));
    );
);

item_consumed(player, slot) -> (
    item_tuple = inventory_get(player, slot);
    // Find any items that need to be replenished
    items = filter(global_to_keep, 
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

__on_player_places_block(player, item_tuple, hand, block)->(
    item_consumed(player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
);

global_ids = l('speed', 'slowness', 'haste', 'mining_fatigue', 'strength', 'instant_health', 'instant_damage', 'jump_boost', 'nausea', 'regeneration', 'resistance', 'fire_resistance', 'water_breathing', 'invisibility', 'blindess', 'night_vision', 'hunger', 'weakness', 'poison', 'wither', 'health_boost', 'absorption', 'saturation', 'glowing', 'levitation', 'luck', 'unluck', 'slow_falling', 'conduit_power', 'dolphins_grace', 'bad_omen', 'hero_of_the_village');

__on_player_finishes_using_item(player, item_tuple, hand)->(
    if (item_tuple: 0 == 'milk_bucket' && length(global_to_keep_effect) > 0,
        effects = parse_nbt(query(player, 'nbt', 'ActiveEffects'));
        if (effects != 'null',
            schedule(0, _(outer(effects)) -> (
                for (effects,
                    name = global_ids: (_: 'Id' - 1);

                    if (_: 'Duration' > 32767 && includes(map(global_to_keep_effect, _: 0), name),
                        amplifier = _: 'Amplifier';
                        modify(player(), 'effect', name, _: 'Duration', if (amplifier < 0, 256 + amplifier, amplifier), _: 'ShowParticles', _: 'ShowIcon');
                    );
                );
            );
            );
        );
    );
    
    item_consumed(player, if(hand == 'offhand', -1, player ~ 'selected_slot'));
);

spawn_with_item(item, amt, player, persistant) -> (
    if (player() ~ 'permission_level' > 1,
        put(global_to_keep, null, l(item: 0, amt, item: 2, player, persistant));
        to_keep = map(global_to_keep, l(_: 0, str(_: 1), if(_: 2, str(_: 2), ''), _: 3, str(_: 4)));
        delete_file('to_keep', 'nbt');
        write_file('to_keep', 'nbt', encode_nbt(to_keep));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

spawn_with_effect(name, duration, amplifier, player) -> (
    if (player() ~ 'permission_level' > 1,
        put(global_to_keep_effect, null, l(name, duration*20, amplifier, player));
        to_keep_effect = map(global_to_keep_effect, l(_: 0, str(_: 1), str(_: 2), _: 3));
        delete_file('to_keep_effect', 'nbt');
        write_file('to_keep_effect', 'nbt', encode_nbt(to_keep_effect));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

dont_spawn_with_item(item, player) -> (
    if (player() ~ 'permission_level' > 1,
        global_to_keep = filter(global_to_keep,
            _: 0 != item: 0 || _: 3 != player;
        );
        to_keep = map(global_to_keep, l(_: 0, str(_: 1), if(_: 2, str(_: 2), ''), _: 3));
        delete_file('to_keep', 'nbt');
        write_file('to_keep', 'nbt', encode_nbt(to_keep));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

dont_spawn_with_effect(name, player) -> (
    if (player() ~ 'permission_level' > 1,
        global_to_keep_effect = filter(global_to_keep_effect,
            _: 0 != name || _: 3 != player;
        );
        to_keep_effect = map(global_to_keep_effect, l(_: 0, str(_: 1), str(_: 2), _: 3));
        delete_file('to_keep_effect', 'nbt');
        write_file('to_keep_effect', 'nbt', encode_nbt(to_keep_effect));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

time_to_track(val) -> (
    if (player() ~ 'permission_level' > 1,
        if (type(val) == 'string',
            global_time_to_track = val;
        ,
            if (global_time_to_track == 'onUse',
                schedule(0, 'changeCompassPos');
            );
            global_time_to_track = round(val * 20);
        );

        write_file('time_to_track', 'nbt', encode_nbt(global_time_to_track));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

log_on_track(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_log_on_track = val;

        write_file('log_on_track', 'nbt', encode_nbt(global_log_on_track));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

max_compasses(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_max_compasses = val;

        write_file('max_compasses', 'nbt', encode_nbt(global_max_compasses));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

no_portal_breaking(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_no_portal_breaking = val;
        delete_file('no_portal_breaking', 'nbt');
        write_file('no_portal_breaking', 'nbt', encode_nbt(global_no_portal_breaking));
        print(player(), format('l Done'));
    ,
        print(player(), format('l Insufficient permissions', 'ei   Requires at least permission level 2'));
    );
);

max_nether_travel(val) -> (
    if (player() ~ 'permission_level' > 1,
        global_max_nether_travel = floor(val/8);

        write_file('max_nether_travel', 'nbt', encode_nbt(global_max_nether_travel));
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
    sqrt(reduce(v1 - v2, _a + _*_, 0));
);

if (global_time_to_track != 'onUse',
    changeCompassPos();
);