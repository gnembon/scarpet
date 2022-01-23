// Add a death stat counter to your server after players have already died

// Fetches the server deaths statistic to use in the scoreboard death count. 

// Add Death counter
// create

// scoreboard objectives add Deaths deathCount
//         scoreboard('Speed',player,roundmath(speed*1000,1));


_initialize() -> (
    scoreboard_add( 'Deaths', 'deathCount');
    for( player('all'),
        _update_player_death_count(_);
    );
);

__on_player_connects(p) -> (
    _update_player_death_count(p);
);

_update_player_death_count(p) -> (
    scoreboard('Deaths', p, statistic( p, 'custom', 'deaths' ));
);

_initialize()