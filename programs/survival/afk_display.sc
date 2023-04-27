__config() -> {
    'scope' -> 'global',
    'stay_loaded' -> true
};

global_teams = read_file('teams.json', 'json');
if(global_teams == null,
    global_teams = write_file('teams.json', 'json', {});
    global_teams = read_file('teams.json', 'json');
);

// Change these values to your liking
global_afk_prefix = '';
global_afk_suffix = '';
global_afk_timeout = 5; // seconds

if(scoreboard('afkX') == null, scoreboard_add('afkX'));
if(scoreboard('afkY') == null, scoreboard_add('afkY'));
if(scoreboard('afkZ') == null, scoreboard_add('afkZ'));
if(scoreboard('afkScore') == null, scoreboard_add('afkScore'));
if(team_list('afk_players') == null,
    team_add('afk_players');
    team_property('afk_players', 'color', 'gray');
    team_property('afk_players', 'prefix', global_afk_prefix);
    team_property('afk_players', 'suffix', global_afk_suffix);
);

check_afk() -> (
    for(player('*'),
        if(and(
            scoreboard('afkX', _) == scoreboard('afkX', _, _~'pos':0),
            scoreboard('afkY', _) == scoreboard('afkY', _, _~'pos':1),
            scoreboard('afkZ', _) == scoreboard('afkZ', _, _~'pos':2);
            ),
            scoreboard('afkScore', _, scoreboard('afkScore', _) + 1),
            scoreboard('afkScore', _, 0);
        );
        if(scoreboard('afkScore', _) >= global_afk_timeout,
            if(_~'team' == 'afk_players', continue());
            global_teams:_ = _~'team';
            team_add('afk_players', _),

            if(_~'team' != 'afk_players', continue());
            if(global_teams:_, 
                team_add(global_teams:_, _),
                team_leave(_);
            );
        );
    );
    schedule(20, 'check_afk');
);

__on_start()->(
    check_afk();
);

__on_close()->(
    write_file('teams.json', 'json', global_teams);
);
