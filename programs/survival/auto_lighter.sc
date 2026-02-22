// requires carpet 1.2.1 (towards scarpet v1.6)
// Script that spreads light sources in a sphere around the player.
// Torches for land.
// Lanterns for water.
// Consumes items in survival mode.
//
// FIRST TIME SETUP: Left-click air with a torch or sea lantern to activate the script
//
// Controls:
//  - Left-click air (not sneaking): Cycle radius (16→32→64→128→256) 
// 	- Custom radius can be set by modifying global_effect_radius in __on_start - default is 16.
//  - Stack size: Sets minimum light level threshold
//  - Right-click with torch/sea_lantern:
//      * Mainhand + not sneaking: Cave-only mode
//      * Mainhand + sneaking: Surface-only mode
//      * Offhand: Hybrid mode (caves and surface)


// =======================
// Initialization
// =======================

__on_start() -> (
    global_script_active = false;  // Script requires one-time activation
    global_effect_radius = 16;
    global_min_light_level = 1;
    global_light_ground = false;
    global_survival = false;
    global_pending_torches = [];
    global_spread_love = 0;
    global_light_block = 'torch';
    global_sky_only = false;
    global_no_sky_only = false;
    global_active_mode = 'Caves';
    global_active_light_level = 1;
);

// =======================
// Player Interactions
// =======================

__on_player_swings_hand(player, hand) ->
(
    if (hand == 'mainhand',
        held_item = inventory_get(player, player~'selected_slot');
        
        if (held_item != null && (held_item:0 == 'torch' || held_item:0 == 'sea_lantern'),
            if (query(player, 'trace') == null,
                // One-time activation check
                if (!global_script_active,
                    global_script_active = true;
                    print(player, format('g ━━━ Auto Lighter ━━━\n', 
					'y Left-click air to cycle radius\n', 
					'y Right-click air to activate\n', 
					'y Sneak to change modes\n', 
					'y Offhand for hybrid mode'));
                    return()
                );
                
                // Normal radius cycling (only when not sneaking)
                if (!player~'sneaking',
                    global_effect_radius = if(global_effect_radius >= 256, 16, global_effect_radius * 2);
                );
            );
        );
    );
);

__on_tick() ->
(
    if (!global_script_active, return());  // Exit if not activated
    
    for(player('all'),
        held_main = query(_, 'holds', 'mainhand');
        held_off = query(_, 'holds', 'offhand');
        
        is_torch_main = (held_main != null && (held_main:0 == 'torch' || held_main:0 == 'sea_lantern'));
        is_torch_off = (held_off != null && (held_off:0 == 'torch' || held_off:0 == 'sea_lantern'));
        
        if (is_torch_main || is_torch_off,
            if (global_spread_love,
                mode_text = global_active_mode;
                light_level = global_active_light_level;
            ,
                if (is_torch_off,
                    light_level = held_off:1;
                    mode_text = 'Hybrid';
                ,
                    is_sneaking = _~'sneaking';
                    light_level = held_main:1;
                    mode_text = if(!is_sneaking, 'Caves', 'Surface');
                );
            );
            
            status_text = if(global_spread_love, 'ON', 'OFF');
            status_color = if(global_spread_love, 'green', 'red');
            
            run(str('title %s actionbar {"text":"Status: ","color":"gold","extra":[{"text":"%s","color":"%s"},{"text":" | Mode: %s | Radius: %d | Light: %d","color":"gold"}]}',
                _~'command_name', status_text, status_color, mode_text, global_effect_radius, light_level));
        );
    );
);

__on_player_uses_item(player, item, hand) ->
(
    if (!global_script_active, return());  // Exit if not activated
    if (item:0 != 'torch' && item:0 != 'sea_lantern', return());
    
    global_light_block = item:0;
    global_min_light_level = item:1;
    
    is_offhand = (hand == 'offhand');
    is_sneaking = player~'sneaking';
    
    // Set mode flags
    if (!is_offhand && !is_sneaking,
        global_light_ground = false;
        global_sky_only = false;
        global_no_sky_only = true;
    ,
        if (!is_offhand && is_sneaking,
            global_light_ground = true;
            global_sky_only = true;
            global_no_sky_only = false;
        ,
            global_light_ground = true;
            global_sky_only = false;
            global_no_sky_only = false;
        );
    );
    
    mode_string = if(global_sky_only, 'Surface', if(global_no_sky_only, 'Caves', 'Hybrid'));
    
    // Toggle on/off
    if (global_spread_love,
        global_spread_love = 0;
    ,
        if (player~'gamemode_id' != 3,
            global_spread_love = 1;
            global_active_mode = mode_string;
            global_active_light_level = global_min_light_level;
            global_survival = !(player~'gamemode_id' % 2);
            schedule(0, 'spread_torches', player, player~'gamemode_id');
        );
    );
);

// =======================
// Helpers
// =======================

__distance_sq(vec1, vec2) -> reduce(vec1 - vec2, _a + _*_, 0);

// Used to prevent placing torches within 14 blocks of each other during the same cycle, greatly reducing the amount of torches required. Savings diminish as radius increases.
// Slightly slower torch placement. You won't notice if you don't read this.
//Change 196 to 0 to remove this check.
__is_near_pending(pos) ->
(
    if (global_pending_torches == null || length(global_pending_torches) == 0, return(false));
    reduce(global_pending_torches, _a || (__distance_sq(_, pos) < 196), false)
);

// =======================
// Main Light Spread Loop
// =======================

spread_torches(player, initial_gamemode) ->
(
    if (global_spread_love && player~'gamemode_id' == initial_gamemode,
        is_survival = global_survival;
        cpos = pos(player);
        d = global_effect_radius * 2;
        dd = global_effect_radius * global_effect_radius;
        
        loop(4000,
            lpos = cpos + l(rand(d), rand(d), rand(d)) - d/2;
            valid = false;
            
            if (global_light_block == 'torch',
                valid = air(lpos) 
                    && block_light(lpos) < global_min_light_level 
                    && (global_light_ground || light(lpos) < global_min_light_level) 
                    && solid(pos_offset(lpos, 'down'));
            ,
                valid = block(lpos) == 'water' 
                    && block_state(lpos):'level' == 0 
                    && block(pos_offset(lpos, 'up')) != 'air' 
                    && block_light(lpos) < global_min_light_level 
                    && (global_light_ground || light(lpos) < global_min_light_level) 
                    && (solid(pos_offset(lpos, 'down')) || solid(pos_offset(lpos, 'up')));
            );
            
            if (__distance_sq(cpos, lpos) <= dd 
                && valid 
                && (!global_sky_only || sky_light(lpos) > 0) 
                && (!global_no_sky_only || sky_light(lpos) == 0) 
                && !__is_near_pending(lpos),
                
                if (is_survival && not_able_loose_light_block(player), return());
                
                global_pending_torches += lpos;
                __send_light_block(player, lpos);
                success += 1;
                
                if (success > 2,
                    schedule(1, 'spread_torches', player, initial_gamemode);
                    return()
                );
            )
        );
        schedule(1, 'spread_torches', player, initial_gamemode)
    )
);

not_able_loose_light_block(p) ->
(
    slot = inventory_find(p, global_light_block, p~'selected_slot'+1);
    if (slot == null,
        slot = inventory_find(p, global_light_block);
        if (slot == null, return(true));
    );
    item = inventory_get(p, slot);
    inventory_set(p, slot, item:1-1, item:0, item:2);
    false
);

// =======================
// Light Animation
// =======================

__light_tick(entity, start, end, block_name) ->
(
    if (entity~'age' >= 20,
        modify(entity, 'remove');
        
        if (block_name == 'torch',
            if (air(end) && solid(pos_offset(end, 'down')), set(end, 'torch'));
        ,
            if (block(end) == 'water' 
                && block_state(end):'level' == 0 
                && (solid(pos_offset(end, 'down')) || solid(pos_offset(end, 'up'))),
                set(end, 'sea_lantern');
            );
        );
        
        if (global_pending_torches != null,
            global_pending_torches = filter(global_pending_torches, _ != end);
        );
        return()
    );
    
    stage = __find_stage(entity~'age', 20);
    final_pos = start*(1-stage)+(end+l(0.5,0,0.5))*stage;
    stage = __find_stage(entity~'age'+1, 20);
    next_pos = start*(1-stage)+(end+l(0.5,0,0.5))*stage;
    modify(entity, 'pos', final_pos);
    modify(entity, 'motion', 1.0111*(next_pos-final_pos));
);


__send_light_block(p, destination) ->
(
    start_position = pos(p)+l(0,p~'eye_height',0)+p~'look';
    marker = spawn('falling_block', start_position,
        '{BlockState:{Name:"minecraft:' + global_light_block + '"},Time:'+(600-50)+',NoGravity:1,DropItem:0}'
    );
    modify(marker,'no_clip');
    entity_event(marker, 'on_tick', '__light_tick', pos(marker), destination, global_light_block);
);

__find_stage(age, maxage) -> 1-sqrt(maxage*maxage-age*age)/maxage;

// =======================
// Utility
// =======================

clear_all_torches() ->
(
    l(x,y,z) = pos(player());
    d = global_effect_radius;
    scan(x,y,z,d,d,d,if(_=='torch' || _=='sea_lantern', set(_, 'air')));
);
