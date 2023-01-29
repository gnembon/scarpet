// Displays the current render distance for servers with a dynamic render distance mod. (Like the one made by Henkelmax)
// When a player joins they can see the render distance at the bottom of the player list.
// It refreshes every 1200 ticks (1 minute) and color codes the distance according to carpet TPS logging colors.
// The refresh time of the display and the min/max render distance can be altered with commands. This will be saved.
// Made by Sulpherstær with aid from people in the carpet discord. (notably: Surf, altrisi, rv3r, Firigion, and Crec0)

global_refresh_time_in_ticks = 1200;
global_server_max_render_distance = 32;
global_server_min_render_distance = 10;

__config() -> {
    'scope' -> 'global',
    'allow_command_conflicts' -> true,
    'command_permission' -> 'ops',
    'commands' -> {
        'display maximumRenderDistance <max>' -> 'set_display_max_render_distance',
        'display mininumRenderDistance <min>' -> 'set_display_min_render_distance',
        'display limit <min> <max>' -> 'set_display_limit',
        'display refreshTimer <seconds>' -> 'set_display_refresh_timer',
        'display update' -> ['update_render_distance_display', player('all')],
        'display settings' -> 'state_current_settings',
    },
    'arguments' -> {
        'min' -> { 'type' -> 'int', 'min' -> 1, 'max' -> 32, 'suggest' -> [10]},
        'max' -> { 'type' -> 'int', 'min' -> 1, 'max' -> 32, 'suggest' -> [32]},
        'seconds' -> { 'type' -> 'int', 'min' -> 1, 'max'-> 1000000, 'suggest' -> [60]},
    }
};

__on_start() -> (
    load_settings();
    __scheduled_updater();
);

__on_close() -> (
    save_settings();
    display_title(player('all'), 'player_list_footer', null);
);

__scheduled_updater() -> (
    update_render_distance_display(player('all'));
    schedule(global_refresh_time_in_ticks, '__scheduled_updater');
);

__on_player_connects(player) -> (
    update_render_distance_display(player);
);

update_render_distance_display(players) -> (
        color = 'g';
        current_server_render_distance = system_info('game_view_distance');

        color = if (
            current_server_render_distance <= global_server_min_render_distance, 'm';
            ,current_server_render_distance < 0.5*global_server_max_render_distance, 'r';
            ,current_server_render_distance < 0.8*global_server_max_render_distance, 'y';
            ,current_server_render_distance <= global_server_max_render_distance, 'e';
            ,'l'
        );

        display_title(players, 'player_list_footer', format('g Server Render Distance: ', str('%s %d', color, current_server_render_distance)));
);

set_display_max_render_distance(distance) -> (
    global_server_max_render_distance = distance;
    print(format(' Succesfully set the maximum render distance used for display colors to ', str('l %d', global_server_max_render_distance), '  chunks'));
);

set_display_min_render_distance(distance) -> (
    global_server_min_render_distance = distance;
    print(format(' Succesfully set the minimum render distance used for display colors to ', str('l %d', global_server_min_render_distance), '  chunks'));
);

set_display_limit(min,max) -> (
    global_server_min_render_distance = min;
    global_server_max_render_distance = max;
    print(format(' Succesfully set the render distance min/max used for display colors to ', str('l %d', global_server_min_render_distance), 'g -', str('l %d', global_server_max_render_distance), '  chunks'));
);

set_display_refresh_timer(seconds) -> (
    global_refresh_time_in_ticks = seconds*20;
    print(format(' Succesfully set the refresh timer for the display to ', str('l %d', global_refresh_time_in_ticks/20), '  seconds'));
);

state_current_settings() -> (
    print(format(' The current display render distance min/max is ', str('l %d', global_server_min_render_distance), 'g -', str('l %d', global_server_max_render_distance), '  chunks, and current display refresh time is ', str('l %d', global_refresh_time_in_ticks/20), '  seconds'));
);


save_settings() -> (
    map = {
        'server_min_render_distance' -> global_server_min_render_distance,
        'server_max_render_distance' -> global_server_max_render_distance,
        'display_refresh_time_in_ticks' -> global_refresh_time_in_ticks
    };
    write_file('renderdistance', 'json', map);
);

load_settings() -> (
    data = read_file('renderdistance', 'json');
    if (data != null, // check if file exists.
        global_server_min_render_distance = data:'server_min_render_distance';
        global_server_max_render_distance = data:'server_max_render_distance';
        global_refresh_time_in_ticks = data:'display_refresh_time_in_ticks';
    );
);
