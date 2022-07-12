//Does the same thing as MiniHUD's light level overlay
//Displays block light level over all solid spawnable blocks (so not bedrock or redstone)
//Numbers are green if block light is above minimum light level for mob spawning,
//yellow if sky light level is above minimum light level,
//and red if overall light level is below minimum light level
//NB: On servers only works for carpet clients.
//By: Ghoulboy

//The range of this effect. 20 is typically pretty tolerable
//Use smaller numbers if you know your connection is poor
global_range = 20;
//The number of ticks until the display is refreshed, and updated for changes in position and light level
//Use longer delays if you know your connection is poor
global_refresh_rate = 50;

global_resolution = 50;
//Change this if your server has a datapack which changes the minimum light level for mob spawning
//TODO replace this with system_info to get it automatically
global_min_light_level = 1;

__config()->{
    //not gonna work for vanilla players anyways
    'stay_loaded' -> true,
    'commands'->{
        ''->'toggle',
        'range <range>'->'set_range',
        'refresh_rate <rate>'->'set_refresh_rate'
    },
    'arguments'->{
        'range'->{'type'->'int','min'->0, 'suggest'->[50, 20, 10]},
        'rate'->{'type'->'int','min'->0, 'suggest'->[50, 20, 100]},
    }
};

global_armour_stand = null;

set_range(range)->(
    global_range = range;
    display_title(player(), 'actionbar', format(str('t Set display range to %s blocks', range)))
);

set_refresh_rate(rate)->(
    global_refresh_rate = rate;
    display_title(player(), 'actionbar', format(str('t Set refresh rate to %s ticks', rate)))
);

toggle()->(
    global_active = !global_active;
    if(global_active,
        player = player();
        display_title(player, 'actionbar', format('y Turned on light level overlay'));
        global_armour_stand = spawn('armor_stand', pos(player) + [0, player~'eye_height', 0], {'Marker'->true, 'Invisible'->true});
        schedule_overlay(),
        display_title(player(), 'actionbar', format('y Turned off light level overlay'));
        modify(global_armour_stand, 'remove');
        global_armour_stand = null;
    );
    null
);

global_active = false;

block_colour(pos)->if(
    block_light(pos)>=global_min_light_level,
    0x00FF00FF,
    light(pos)>=global_min_light_level,
    0xFFFF00FF,
    0xFF0000FF
);

valid_spawnable(block)->solid(block) && block!='bedrock' && block!='redstone_block';

schedule_overlay_new()->(
    if(!global_armour_stand, return());
    player = player();
    pos = pos(player);
    //Tp armour stand to player to it can rotate and look at blocks
    modify(global_armour_stand, 'pos', pos(player) + [0, player~'eye_height', 0]);
    batch = [];
    c_for(yaw = -180, yaw < 180, yaw += 360/global_resolution,
        s_yaw = sin(yaw);
        c_yaw = cos(yaw);
        c_for(pitch = -90, pitch < 90, pitch += 180/global_resolution,
            s_pitch = sin(pitch);
            c_pitch = cos(pitch);
            look = [s_yaw*c_pitch, s_pitch, c_yaw*c_pitch];
            modify(global_armour_stand, 'look', look);
            block = query(global_armour_stand, 'trace', global_range, 'blocks', 'liquids');
            if(block!=null,
                pos = pos(block);
                if(valid_spawnable(block) && air(pos_offset(pos, 'up')) && air(pos_offset(pos, 'up', 2)),
                    [x, y, z] = pos;
                    print(pos);
                    batch += [
                        'label', global_refresh_rate, 
                        'pos', [x+0.5, y, z+0.5], 
                        'text', block_light(x, y, z), 
                        'size', 20, 
                        'player', player, 
                        'color', block_colour(block)
                    ]
                )
            )
        )
    );
    draw_shape(batch);
    if(global_active, schedule(global_refresh_rate, 'schedule_overlay'))
);

schedule_overlay()->(
    player = player();
    pos = pos(player);
    batch = [];
    scan(pos, [global_range, global_range, global_range],
        if(air(_) && valid_spawnable(block(pos_offset(_, 'down'))) && air(pos_offset(_, 'up')),
            batch += [
                'label', global_refresh_rate, 
                'pos', [_x+0.5,_y,_z+0.5], 
                'text', block_light(_x,_y,_z), 
                'size', 20, 
                'player', player, 
                'color', block_colour(_)
            ]
        )
    );
    draw_shape(batch);
    if(global_active, schedule(global_refresh_rate, 'schedule_overlay'))
);

__on_close()->if(global_armour_stand!=null, modify(global_armour_stand, 'remove'); global_armour_stand = null)
