// Statistic Display by CommandLeo

global_total_text = ' §lTotal';
global_block_list = block_list();
global_item_list = item_list();
global_entity_list = entity_types('*');
global_server_whitelisted = system_info('server_whitelisted') || length(system_info('server_whitelist')) > 0;
global_app_name = system_info('app_name');
global_hex_charset = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

pickaxes = filter(global_item_list, _~'_pickaxe');
axes = filter(global_item_list, _~'_axe');
shovels = filter(global_item_list, _~'_shovel');
hoes = filter(global_item_list, _~'_hoe');

display_names = read_file('display_names', 'json');
global_misc_stats = display_names:'misc';
global_block_names = display_names:'blocks';
global_item_names = display_names:'items';
global_entity_names = display_names:'entities';
global_categories = {'mined' -> '%s Mined', 'crafted' -> '%s Crafted', 'used' -> '%s Used', 'broken' -> '%s Broken', 'picked_up' -> '%s Picked Up', 'dropped' -> '%s Dropped', 'killed' -> '%s Killed', 'killed_by' -> 'Killed by %s', 'custom' -> '%s', 'extra' -> '%s', 'digs' -> 'Digs [%s]'};
global_extra_stats = {'bedrock_removed' -> 'Bedrock Removed', 'ping' -> 'Ping', 'health' -> 'Health', 'xp_level' -> 'Levels of Experience', 'hunger' -> 'Hunger', 'saturation' -> 'Saturation', 'air' -> 'Remaining Air'};
global_dig_data = {'combined_blocks' -> ['Combined Blocks', null], 'total' -> ['Total', [...pickaxes, ...shovels, ...axes, ...hoes, 'shears']], 'pick' -> ['Pickaxe', pickaxes], 'shovel' -> ['Shovel', shovels], 'pickshovel' -> ['Pickaxe & Shovel', [...pickaxes, ...shovels]], 'axe' -> ['Axe', axes], 'hoe' -> ['Hoe', hoes]};

global_bedrock_removed = read_file('bedrock_removed', 'json') || {};
global_digs = {};
for(list_files('digs', 'json'), global_digs:slice(_, length('digs') + 1) = read_file(_, 'json'));
global_carousel_data = read_file('carousel', 'json') || {'interval' -> 200, 'entries' -> []};

settings = read_file('settings', 'json');
global_stat = settings:'stat' || [];
global_bots_included = settings:'bots_included';
global_offline_digs = if(settings:'offline_digs' != null, settings:'offline_digs', true);
global_dig_display = settings:'dig_display' || {};
global_dig_display_color = settings:'dig_display_color' || {};
global_stat_color = settings:'stat_color' || 'FFEE44';
global_default_dig = settings:'default_dig' || 'combined_blocks';

global_help_pages = [
    [
        '%color% /%app_name% mined <block> ', 'f ｜ ', 'g Amount of <block> mined', ' \n',
        '%color% /%app_name% used <item> ', 'f ｜ ', 'g Amount of <item> used or placed', ' \n',
        '%color% /%app_name% crafted <item> ', 'f ｜ ', 'g Amount of <item> crafted', ' \n',
        '%color% /%app_name% dropped <item> ', 'f ｜ ', 'g Amount of <item> dropped', ' \n',
        '%color% /%app_name% picked_up <item> ', 'f ｜ ', 'g Amount of <item> picked up', ' \n',
        '%color% /%app_name% broken <item> ', 'f ｜ ', 'g Amount of <item> broken', '^g (that ran out of durability)', ' \n',
        '%color% /%app_name% killed <entity> ', 'f ｜ ', 'g Amount of <entity> killed', ' \n',
        '%color% /%app_name% killed_by <entity> ', 'f ｜ ', 'g Amount of times <entity> killed you', ' \n',
        '%color% /%app_name% misc <misc_stat> ', 'f ｜ ', 'g Misc statistics, e.g. play_time, deaths, mob_kills, aviate_one_cm', ' \n',
        '%color% /%app_name% extra <extra_stat> ', 'f ｜ ', 'g Extra statistics that are not normally in the game, e.g. xp_level, ping, health, hunger', ' \n',
        '%color% /%app_name% bedrock_removed ', 'f ｜ ', 'g Amount of bedrock removed by hand using pistons and tnt', ' \n',
        '%color% /%app_name% digs <dig> ', 'f ｜ ', 'g Amount of digs (%default_dig% by default)', ' \n',
        '%color% /%app_name% combined <combined_stat> ', 'f ｜ ', 'g Multiple statistics combined together', ' \n',
    ],
    [
        '%color% /%app_name% print <category> <entry> [<player>] ', 'f ｜ ', 'g Prints the value of a stat of a player', ' \n',
        '%color% /%app_name% hide ', 'f ｜ ', 'g Hides the scoreboard', ' \n',
        '%color% /%app_name% show ', 'f ｜ ', 'g Shows the scoreboard', '  \n',
        '%color% /%app_name% bots (on|off|toggle) ', 'f ｜ ', 'g A shortcut for /%app_name% settings bots_included', '  \n',
        '%color% /%app_name% settings bots_included [on|off|toggle] ', 'f ｜ ', 'g Includes or excludes bots in the scoreboard', ' \n',
        '%color% /%app_name% settings default_dig <dig> ', 'f ｜ ', 'g Sets the default dig type ', 'f *', '^g For server operators only', '  \n',
        '%color% /%app_name% settings offline_digs [on|off|toggle] ', 'f ｜ ', 'g Includes or excludes digs of offline whitelisted players in the scoreboard', ' \n',
        '%color% /%app_name% settings dig_display [on|off|toggle] ', 'f ｜ ', 'g Shows or hides digs in the player list footer', ' \n',
        '%color% /%app_name% settings dig_display_color <hex_color> ', 'f ｜ ', 'g Changes the color of digs display for yourself; leave empty to reset', ' \n',
        '%color% /%app_name% settings stat_color <hex_color> ', 'f ｜ ', 'g Changes the color of the scoreboard name for everyone; leave empty to reset ', 'f *', '^g For server operators only', ' \n'
    ],
    [
        '%color% /%app_name% settings combined_stats list ', 'f ｜ ', 'g Lists combined statistics ', ' \n',
        '%color% /%app_name% settings combined_stats info <combined_stat> ', 'f ｜ ', 'g Prints the entries of a combined statistic ', ' \n',
        '%color% /%app_name% settings combined_stats create <name> <display_name> <category> <entries> ', 'f ｜ ', 'g Creates a combined statistic ', 'f *', '^g For server operators only', ' \n',
        '%color% /%app_name% settings combined_stats delete <combined_stat> ', 'f ｜ ', 'g Deletes a combined statistic ', 'f *', '^g For server operators only', ' \n\n',
        '%color% /%app_name% carousel start ', 'f ｜ ', 'g Starts a carousel of statistics', ' \n',
        '%color% /%app_name% carousel stop ', 'f ｜ ', 'g Stops the carousel', ' \n',
        '%color% /%app_name% carousel interval [<seconds>] ', 'f ｜ ', 'g Gets or sets the interval of the carousel', ' \n',
        '%color% /%app_name% carousel list ', 'f ｜ ', 'g Lists carousel entries', ' \n',
        '%color% /%app_name% carousel add <category> <entry> ', 'f ｜ ', 'g Adds an entry to the carousel', ' \n',
        '%color% /%app_name% carousel remove <index> ', 'f ｜ ', 'g Removes an entry from the carousel', ' \n',
    ]
];

__config() -> {
    'resources' -> [
        // Display names
        {
        'source' -> str('https://raw.githubusercontent.com/CommandLeo/scarpet/main/resources/stat/display_names/%d.json', system_info('game_major_target')),
        'target' -> 'display_names.json'
        },
        // Default combined stats
        {
            'source' -> 'https://raw.githubusercontent.com/CommandLeo/scarpet/main/resources/stat/combined/concrete_placed.txt',
            'target' -> 'combined/concrete_placed.txt'
        },
        {
            'source' -> 'https://raw.githubusercontent.com/CommandLeo/scarpet/main/resources/stat/combined/ores_mined.txt',
            'target' -> 'combined/ores_mined.txt'
        }
    ],
    'commands' -> {
        '' -> 'menu',
        'hide' -> 'hide',
        'show' -> 'show',
        'help' -> ['help', 1],
        'help <page>' -> 'help',

        'bots' -> ['toggleBots', null],
        'bots on' -> ['toggleBots', true],
        'bots off' -> ['toggleBots', false],
        'bots toggle' -> ['toggleBots', null],
        'settings bots_included' -> ['toggleBots', null],
        'settings bots_included on' -> ['toggleBots', true],
        'settings bots_included off' -> ['toggleBots', false],
        'settings bots_included toggle' -> ['toggleBots', null],
        'settings offline_digs' -> ['toggleOfflineDigs', null],
        'settings offline_digs on' -> ['toggleOfflineDigs', true],
        'settings offline_digs off' -> ['toggleOfflineDigs', false],
        'settings offline_digs toggle' -> ['toggleOfflineDigs', null],
        'settings dig_display' -> ['toggleDigDisplay', null],
        'settings dig_display on' -> ['toggleDigDisplay', true],
        'settings dig_display off' -> ['toggleDigDisplay', false],
        'settings dig_display toggle' -> ['toggleDigDisplay', null],
        'settings dig_display_color' -> ['setDigDisplayColor', null],
        'settings dig_display_color <hex_color>' -> 'setDigDisplayColor',
        'settings stat_color' -> ['setStatColor', null],
        'settings stat_color <hex_color>' -> 'setStatColor',
        'settings default_dig <dig>' -> 'setDefaultDig',
        'settings combined_stats list' -> 'listCombinedStats',
        'settings combined_stats info <combined_stat>' -> 'combinedStatInfo',
        'settings combined_stats create <name> <display_name> <category> <entries>' -> 'createCombinedStat',
        'settings combined_stats delete <combined_stat>' -> 'deleteCombinedStat',

        'mined <block>' -> ['changeStat', 'mined'],
        'crafted <item>' -> ['changeStat', 'crafted'],
        'used <item>' -> ['changeStat', 'used'],
        'broken <item>' -> ['changeStat', 'broken'],
        'picked_up <item>' -> ['changeStat', 'picked_up'],
        'dropped <item>' -> ['changeStat', 'dropped'],
        'killed <entity>' -> ['changeStat', 'killed'],
        'killed_by <entity>' -> ['changeStat', 'killed_by'],
        'misc <misc_stat>' -> ['changeStat', 'custom'],
        'extra <extra_stat>' -> ['changeStat', 'extra'],
        'bedrock_removed' -> ['changeStat', 'bedrock_removed', 'extra'],
        'digs <dig>' -> ['changeStat', 'digs'],
        'digs' -> ['changeStat', null, 'digs'],
        'combined <combined_stat>' -> ['changeStat', 'combined'],

        'print mined <block>' -> ['printStatValue', null, 'mined'],
        'print crafted <item>' -> ['printStatValue', null, 'crafted'],
        'print used <item>' -> ['printStatValue', null, 'used'],
        'print broken <item>' -> ['printStatValue', null, 'broken'],
        'print picked_up <item>' -> ['printStatValue', null, 'picked_up'],
        'print dropped <item>' -> ['printStatValue', null, 'dropped'],
        'print killed <entity>' -> ['printStatValue', null, 'killed'],
        'print killed_by <entity>' -> ['printStatValue', null, 'killed_by'],
        'print misc <misc_stat>' -> ['printStatValue', null, 'custom'],
        'print extra <extra_stat>' -> ['printStatValue', null, 'extra'],
        'print digs <dig>' -> ['printStatValue', null, 'digs'],
        'print combined <combined_stat>' -> ['printStatValue', 'combined', null],
        'print mined <block> <player>' -> ['printStatValue', 'mined'],
        'print crafted <item> <player>' -> ['printStatValue', 'crafted'],
        'print used <item> <player>' -> ['printStatValue', 'used'],
        'print broken <item> <player>' -> ['printStatValue', 'broken'],
        'print picked_up <item> <player>' -> ['printStatValue', 'picked_up'],
        'print dropped <item> <player>' -> ['printStatValue', 'dropped'],
        'print killed <entity> <player>' -> ['printStatValue', 'killed'],
        'print killed_by <entity> <player>' -> ['printStatValue', 'killed_by'],
        'print misc <misc_stat> <player>' -> ['printStatValue', 'custom'],
        'print extra <extra_stat> <player>' -> ['printStatValue', 'extra'],
        'print digs <dig> <player>' -> ['printStatValue', 'digs'],
        'print combined <combined_stat> <player>' -> ['printStatValue', 'combined'],

        'carousel start' -> 'startCarousel',
        'carousel stop' -> 'stopCarousel',
        'carousel interval' -> ['carouselInterval', null],
        'carousel interval <seconds>' -> 'carouselInterval',
        'carousel remove <index>' -> 'removeCarouselEntry',
        'carousel add mined <block>' -> ['addCarouselEntry', 'mined'],
        'carousel add crafted <item>' -> ['addCarouselEntry', 'crafted'],
        'carousel add used <item>' -> ['addCarouselEntry', 'used'],
        'carousel add broken <item>' -> ['addCarouselEntry', 'broken'],
        'carousel add picked_up <item>' -> ['addCarouselEntry', 'picked_up'],
        'carousel add dropped <item>' -> ['addCarouselEntry', 'dropped'],
        'carousel add killed <entity>' -> ['addCarouselEntry', 'killed'],
        'carousel add killed_by <entity>' -> ['addCarouselEntry', 'killed_by'],
        'carousel add misc <misc_stat>' -> ['addCarouselEntry', 'custom'],
        'carousel add extra <extra_stat>' -> ['addCarouselEntry', 'extra'],
        'carousel add digs <dig>' -> ['addCarouselEntry', 'digs'],
        'carousel add combined <combined_stat>' -> ['addCarouselEntry', 'combined'],
        'carousel list' -> 'listCarouselEntries'
    },
    'arguments' -> {
        'block' -> {
            'type' -> 'term',
            'suggestions' -> global_block_list,
            'options' -> global_block_list,
            'case_sensitive' -> false
        },
        'item' -> {
            'type' -> 'term',
            'suggestions' -> global_item_list,
            'options' -> global_item_list,
            'case_sensitive' -> false
        },
        'entity' -> {
            'type' -> 'term',
            'suggestions' -> global_entity_list,
            'options' -> global_entity_list,
            'case_sensitive' -> false
        },
        'misc_stat' -> {
            'type' -> 'term',
            'suggestions' -> keys(global_misc_stats),
            'options' -> keys(global_misc_stats),
            'case_sensitive' -> false
        },
        'extra_stat' -> {
            'type' -> 'term',
            'suggestions' -> keys(global_extra_stats),
            'options' -> keys(global_extra_stats),
            'case_sensitive' -> false
        },
        'dig' -> {
            'type' -> 'term',
            'suggestions' -> keys(global_dig_data),
            'options' -> keys(global_dig_data),
            'case_sensitive' -> false
        },
        'combined_stat' -> {
            'type' -> 'term',
            'suggester' -> _(args) -> map(list_files('combined', 'text'), slice(_, length('combined') + 1)),
            'case_sensitive' -> false
        },
        'category' -> {
            'type' -> 'term',
            'suggest' -> keys(global_categories),
            'options' -> keys(global_categories),
            'case_sensitive' -> false
        },
        'player' -> {
            'type' -> 'players',
            'single' -> true
        },
        'entries' -> {
            'type' -> 'text',
            'suggester' -> _(args) -> (
                entries_string = args:'entries' || ' ';
                category = args:'category';
                list = if(
                    category == 'used' || category == 'broken' || category == 'crafted' || category == 'dropped' || category == 'picked_up', global_item_list, 
                    category == 'mined', global_block_list,
                    category == 'killed' || category == 'killed_by', global_entity_list,
                    category == 'custom', keys(global_misc_stats),
                    category == 'extra', keys(global_extra_stats),
                    category == 'digs', keys(global_dig_data)
                );
                entries = split(' ', entries_string);
                if(length(entries) && slice(entries_string, -1) != ' ', delete(entries, -1));
                if(entries, map(list, str('%s %s', join(' ', entries), _)), list);
            ),
            'case_sensitive' -> false
        },      
        'hex_color' -> {
            'type' -> 'term',
            'suggester' -> _(args) -> (
                color = upper(args:'hex_color' || '');
                if(!color || (length(color) < 6 && all(split(color), has(global_hex_charset, _))), map(global_hex_charset, color + _), []);
            ),
            'case_sensitive' -> false
        },
        'name' -> {
            'type' -> 'term',
            'suggest' -> []
        },
        'display_name' -> {
            'type' -> 'string',
            'suggest' -> []
        },
        'page' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> length(global_help_pages),
            'suggest' -> [range(length(global_help_pages))] + 1
        },
        'seconds' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 3600,
            'suggest' -> []
        },
        'index' -> {
            'type' -> 'int',
            'suggest' -> []
        }
    },
    'requires' -> {
        'carpet' -> '>=1.4.44'
    },
    'scope' -> 'global'
};

// HELPER FUNCTIONS

_error(error) -> exit(print(format(str('r %s', error))));

isInvalidEntry(entry) -> (
    if(entry == global_total_text, return(false));
    if(global_stat:0 == 'digs' && global_server_whitelisted && global_offline_digs, return(!has(system_info('server_whitelist'), str(entry))));
    return(!player(entry) || (!global_bots_included && player(entry)~'player_type' == 'fake'));
);

removeInvalidEntries() -> (
    for(scoreboard('stats'), if(isInvalidEntry(_), scoreboard_remove('stats', _)));
);

parseCombinedFile(name) -> (
    file = read_file('combined/' + name, 'text');
    display_name = file:0;
    category = if(length(file) > 1, file:1);
    loop(2, delete(file, 0));
    return([display_name, category, file]);
);

calculateTotal() -> (
    if(!global_stat, return());
    for(scoreboard('stats'), if(_ != global_total_text, total += scoreboard('stats', _)));
    scoreboard('stats', global_total_text, total);
);

getDisplayName(category, event) -> (
    return(str(global_categories:category, if(
        category == 'used' || category == 'broken' || category == 'crafted' || category == 'dropped' || category == 'picked_up', global_item_names, 
        category == 'mined', global_block_names,
        category == 'killed' || category == 'killed_by', global_entity_names,
        category == 'custom', global_misc_stats,
        category == 'extra', global_extra_stats,
        category == 'digs', global_dig_data
    ):event || event));
);

getStat(player, category, event) -> (
    if(category == 'digs',
        if(!player(player), return(global_digs:event:str(player)));
        if(event == 'combined_blocks',
            return(reduce(global_block_list, _a + statistic(player, 'mined', _), 0)),
            tools = global_dig_data:event:1;
            return(if(tools, reduce(tools, _a + statistic(player, 'used', _), 0)));
        );
    );
    if(category == 'combined',
        if(event == global_stat:1 && global_combined, [category, entries] = global_combined, [display_name, category, entries] = parseCombinedFile(event));
        return(if(entries, reduce(entries, _a + statistic(player, category, _), 0)));
    );
    if(category == 'extra',
        if(event == 'bedrock_removed', return(global_bedrock_removed:(player(player)~'uuid')));
        return(player(player)~event);
    );
    return(statistic(player, category, event));
);

displayDigs(player) -> (
    uuid = player~'uuid';
    if(global_dig_display:uuid == false || !player(player), return());
    color = global_dig_display_color:uuid || global_stat_color;
    display_title(player, 'player_list_footer', format(str('#%s ⬛ %s', color, getStat(player, 'digs', 'combined_blocks')), '#343A40  ｜ ', str('#%s ⚒ %s', color, getStat(player, 'digs', 'total')), '#343A40  ｜ ', str('#%s ⛏ %s', color, getStat(player, 'digs', 'pick'))));
);

// MAIN FUNCTIONS

menu() -> (
    texts = [
        'fs ' + ' ' * 80, ' \n',
        '#FED330b Statistic Display ', 'g by ', '%color%b CommandLeo', '^g https://github.com/CommandLeo', ' \n\n',
        'g An app to easily display statistics on the scoreboard.', '  \n',
        'g Run ', '%color% /%app_name% help', '!/%app_name% help', '^g Click to run the command', 'g  to see a list of all the commands.', '  \n',
        'fs ' + ' ' * 80
    ];
    replacement_map = {'%app_name%' -> global_app_name, '%color%' -> '#FFEE44'};
    print(format(map(texts, reduce(pairs(replacement_map), replace(_a, ..._), _))));
);

help(page) -> (
    length = length(global_help_pages);
    if(page < 1 || page > length, _error('Invalid page number'));
    page = page - 1;
    previous_page = (page - 1) % length + 1;
    next_page = ((page + 1) % length + 1);
    texts = ['fs ' + ' ' * 80, ' \n', ...global_help_pages:page, 'fs ' + ' ' * 31, '  ', 'fb «', '^g Previous page', '!/%app_name% help ' + previous_page, str('g \ Page %d/%d ', page + 1, length), 'fb »', '^g Next page', '!/%app_name% help ' + next_page, '  ', 'fs ' + ' ' * 31];
    replacement_map = {'%app_name%' -> global_app_name, '%color%' -> '#FFEE44', '%default_dig%' -> global_default_dig};
    print(format(map(texts, reduce(pairs(replacement_map), replace(_a, ..._), _))));
);

hide() -> (
    if(scoreboard_property('stats', 'display_slot')~'sidebar' != null, scoreboard_display('sidebar', null));
);

show() -> (
    scoreboard_display('sidebar', 'stats');
);

toggleBots(value) -> (
    global_bots_included = if(value == null, !global_bots_included, value);
    print(format('f » ', 'g Bots are now ', ...if(global_bots_included, ['l included', 'g  in '], ['r excluded', 'g  from ']), 'g the scoreboard'));
    bots = filter(player('all'), _~'player_type' == 'fake');
    for(bots, updateStat(_));
    calculateTotal();
);

toggleOfflineDigs(value) -> (
    global_offline_digs = if(value == null, !global_offline_digs, value);
    print(format('f » ', 'g Offline digs are now ', if(global_offline_digs, 'l enabled', 'r disabled')));
    if(global_stat:0 != 'digs' || !global_server_whitelisted, exit());
    for(if(global_offline_digs, system_info('server_whitelist'), player('all')), updateStat(_));
    removeInvalidEntries();
    calculateTotal();
);

toggleDigDisplay(value) -> (
    uuid = player()~'uuid';
    global_dig_display:uuid = if(value == null, global_dig_display:uuid == false, value);
    print(format('f » ', 'g Digs are now ', if(global_dig_display:uuid, 'l displayed', 'r hidden'), 'g  in the player list footer'));
    if(global_dig_display:uuid, displayDigs(player()), display_title(player(), 'player_list_footer'));
);

setDigDisplayColor(color) -> (
    uuid = player()~'uuid';
    if(!color,
        delete(global_dig_display_color:uuid);
        print(format('f » ', 'g Dig display color has been ', 'r reset')),
        color = upper(replace(color, '#'));
        if(length(color) != 6 || !all(split(color), has(global_hex_charset, _ )), _error('Invalid hex color'));
        global_dig_display_color:uuid = color;
        print(format('f » ', 'g Dig display color has been set to ', str('#%s #%s', global_dig_display_color:uuid, global_dig_display_color:uuid)));
    );
    if(global_dig_display:uuid != false, displayDigs(player()));
);

setStatColor(color) -> (
    if(player()~'permission_level' == 0, _error('You must be an operator to run this command'));
    if(!color,
        global_stat_color = 'FFEE44';
        print(format('f » ', 'g Stat color has been ', 'r reset')),
        color = upper(replace(color, '#', ''));
        if(length(color) != 6 || !all(split(color), has(global_hex_charset, _ )), _error('Invalid hex color'));
        global_stat_color = color;
        print(format('f » ', 'g Stat color has been set to ', str('#%s #%s', global_stat_color, global_stat_color)));
    );
    scoreboard_property('stats', 'display_name', format(str('#%s %s', global_stat_color, scoreboard_property('stats', 'display_name'))));
    for(player('all'), displayDigs(_));
);

setDefaultDig(dig) -> (
    if(player()~'permission_level' == 0, _error('You must be an operator to run this command'));
    if(!has(global_dig_data, dig), _error('Invalid dig type'));
    global_default_dig = dig;
    for(player('all'), updateDigs(_));
    print(format('f » ', 'g The default dig type is now ', str('#%s %s', 'FFEE44', global_default_dig)));
);

printStatValue(event, player, category) -> (
    player = player || player();
    value = getStat(player, category, event);
    if(!value, _error('No value was found'));
    print(format('f » ', str('g Value of \'%s\' for %s is ', getDisplayName(category, event), player), '#FFEE44 ' + value));
);

changeStat(event, category) -> (
    if(global_carousel_active, _error('Couldn\'t change the displayed statistic, a carousel is currently active'));
    if(category == 'combined' && !parseCombinedFile(event):0, _error('Combined statistic not found'));
    showStat(category, if(category == 'digs' && !event, global_default_dig, event));
    show();
    logger(str('[Stat] Stat Change | %s -> %s.%s', player(), category, event));
);

showStat(category, event) -> (
    if(category == 'combined', 
        [display_name, combined_category, entries] = parseCombinedFile(event);
        global_combined = [combined_category, entries];
    );
    global_stat = [category, event];
    scoreboard_property('stats', 'display_name', format(str('#%s %s', global_stat_color, display_name || getDisplayName(category, event))));
    for(if(category == 'digs' && global_server_whitelisted && global_offline_digs, system_info('server_whitelist'), player('all')), updateStat(_));
    removeInvalidEntries();
    calculateTotal();
);

updateStat(player) -> (
    if(!global_stat, return());
    if(isInvalidEntry(str(player)), return(scoreboard_remove('stats', player)));
    value = getStat(player, ...global_stat);
    if(value, scoreboard('stats', player, value), scoreboard_remove('stats', player));
);

updateDigs(player) -> (
    if(!player(player), return());
    if(!global_server_whitelisted || has(system_info('server_whitelist'), str(player)),
        for(global_dig_data,
            global_digs:_ = global_digs:_ || {};
            amount = getStat(player, 'digs', _);
            if(amount > 0, global_digs:_:str(player) = amount);
        );
    );
    displayDigs(player(player));
    scoreboard('digs', player, getStat(player, 'digs', global_default_dig));
);

// COMBINED STATS MANAGING

listCombinedStats() -> (
    combined_stats = map(list_files('combined', 'text'), slice(_, length('combined') + 1));
    texts = reduce(combined_stats, [..._a, if(_i == 0, '', 'g , '), str('#FFEE44 %s', _), str('?/%s settings combined_stats info %s', global_app_name, _)], ['f » ', 'g Available combined stats: ']);
    print(format(texts));
);

combinedStatInfo(name) -> (
    [display_name, category, entries] = parseCombinedFile(name);
    if(!display_name && !category && !entries, _error('Combined statistic not found'));
    print(format('f » ', str('#FFEE44 %s', display_name), str('^g %s', name), 'g  ｜ Entries:\n', 'g ' + join('\n', map(entries, str('    %s.%s', category, _)))));
);

createCombinedStat(name, display_name, category, entries_string) -> (
    if(player()~'permission_level' == 0, _error('You must be an operator to run this command'));
    filename = 'combined/' + name;
    if(read_file(filename, 'text'), _error('There\'s already a combined statistic with that name'));
    if(!has(global_categories, category), _error('Invalid category'));
    entries = split(' ', entries_string);
    if(!entries, _error('No entries provided'));
    write_file(filename, 'text', display_name, category, ...entries);
    print(format('f » ', 'g Successfully created the combined stat'));
);

deleteCombinedStat(name) -> (
    if(player()~'permission_level' == 0, _error('You must be an operator to run this command'));
    if(delete_file('combined/' + name, 'text'), print(format('f » ', 'g Successfully deleted the combined stat')), _error('Combined stat not found'));
);

// CAROUSEL

startCarousel() -> (
    if(global_carousel_active, _error('There\'s already a carousel active'));
    interval = global_carousel_data:'interval';
    entries = global_carousel_data:'entries';
    if(!entries, _error('No entries were found'));
    if(!interval, _error('No interval was provided'));
    print(format('f » ', 'g You ', 'l started ', 'g the carousel'));
    logger(str('[Stat] Carousel Start | %s', player()));
    global_carousel_active = true;
    show();
    carousel(entries, 0);
);

stopCarousel() -> (
    if(!global_carousel_active, _error('There is no carousel active'));
    print(format('f » ', 'g You ', 'r stopped ', 'g the carousel'));
    logger(str('[Stat] Carousel Stop | %s', player()));
    global_carousel_active = false;
);

carouselInterval(seconds) -> (
    if(!seconds, exit(print(format('f » ', 'g Carousel interval is currently set to ', str('d %d ', global_carousel_data:'interval' / 20), 'g seconds'))));
    if(type(seconds) != 'number', _error('The interval provided is not a number'));
    global_carousel_data:'interval' = seconds * 20;
    print(format('f » ', 'g Carousel interval was set to ', str('d %d ', seconds), 'g seconds'));
    logger(str('[Stat] Carousel Interval Change | %s -> %d', player(), seconds));
);

addCarouselEntry(entry, category) -> (
    global_carousel_data:'entries' += [category, entry];
    print(format('f » ', 'g Successfully added an entry to the carousel'));
);

removeCarouselEntry(index) -> (
    entries = global_carousel_data:'entries';
    if(index >= length(entries), _error('Invalid index'));
    delete(entries, index);
    print(format('f » ', 'g The entry was removed from the carousel'));
);

listCarouselEntries() -> (
    entries = global_carousel_data:'entries';
    if(!length(entries), exit(print(format('f » ', 'g No entries to show, the carousel is empty'))));
    print(format(reduce(entries, [..._a, ' \n  ', '#EB4D4Bb ❌', '^r Remove entry', str('?/%s carousel remove %d', global_app_name, _i), '  ', str('g %s.%s', _)], ['f » ', 'g Carousel entries: ', '#26DE81b (+)', '^l Add more entries', str('?/%s carousel add ', global_app_name)])));
);

carousel(entries, i) -> (
    if(global_carousel_active,
        stat = entries:i;
        showStat(...stat);
        schedule(global_carousel_data:'interval', 'carousel', entries, (i + 1) % length(entries));
    );
);

// EVENTS

__on_statistic(player, category, event, value) -> (
    if(category == 'mined' || (category == 'used' && global_dig_data:'total':1~event != null), schedule(0, 'updateDigs', player));
    if(!global_stat || global_stat:0 == 'extra', exit());
    if(global_stat == [category, event] || (global_stat == ['digs', 'combined_blocks'] && category == 'mined') || (category == 'used' && global_stat:0 == 'digs' && global_dig_data:'total':1~event != null) || (global_stat:0 == 'combined' && global_combined:0 == category && global_combined:1~event != null), schedule(0, 'updateStat', player); schedule(0, 'calculateTotal'));
);

// Bedrock breaking detection
__on_player_places_block(player, item_tuple, hand, block) -> (
    if(!block~'piston', exit());
    facing_pos = pos_offset(block, block_state(block, 'facing'));
    facing_block = block(facing_pos);
    if(facing_block != 'bedrock', exit());
    schedule(2, _(outer(facing_pos), outer(player)) -> 
        if(block(facing_pos) != 'bedrock',
            global_bedrock_removed:(player~'uuid') += 1;
            scoreboard('bedrock_removed', player, global_bedrock_removed:(player~'uuid'));
            if(global_stat == ['extra', 'bedrock_removed'], updateStat(player); calculateTotal());
        );
    );
);

__on_tick() -> (
    if((global_stat:0 == 'extra' && global_stat:1 != 'bedrock_removed') || (global_stat:0 == 'custom' && has({'play_one_minute', 'play_time', 'time_since_death', 'time_since_rest', 'total_world_time'}, global_stat:1)), for(player('all'), updateStat(_)); calculateTotal());
);

__on_player_connects(player) -> (
    schedule(0, 'updateDigs', player);
    schedule(0, 'updateStat', player);
    schedule(0, 'calculateTotal');
);

__on_player_disconnects(player, reason) -> (
    schedule(0, 'updateStat', player);
    schedule(0, 'calculateTotal');
);

__on_close() -> (
    write_file('bedrock_removed', 'json', global_bedrock_removed);
    write_file('carousel', 'json', global_carousel_data);
    settings = {
        'stat' -> global_stat,
        'bots_included' -> global_bots_included,
        'offline_digs' -> global_offline_digs,
        'dig_display' -> global_dig_display,
        'dig_display_color' -> global_dig_display_color,
        'stat_color' -> global_stat_color,
        'default_dig' -> global_default_dig
    };
    write_file('settings', 'json', settings);
    for(global_digs, write_file(str('digs/%s', _), 'json', global_digs:_));
);

// INITIALIZATION

__on_start() -> (
    for(['stats', 'bedrock_removed', 'digs'], if(scoreboard()~_ == null, scoreboard_add(_)));
    scoreboard_display('list', 'digs');

    if(global_stat:0 == 'combined', [display_name, combined_category, entries] = parseCombinedFile(global_stat:1); global_combined = [combined_category, entries]);
    for(player('all'), updateDigs(_); updateStat(_));
    removeInvalidEntries();
    calculateTotal();
);