// Skip the night and no longer required fake players to sleep.
// Different from "OnePlayerSleeping", still all real player is required on the server to cause night to pass.

// Known issues: Ignore "doDaylightCycle" and "doWeatherCycle" rules, Because they can cause the player to not get up
// I don’t know yet there is any way in Scarpet to let players wakes up.
// This problem also caused the wake up time to be slightly later than Vanilla in thunderstorms.

__config() -> { 'scope' -> 'global', 'stay_loaded' -> true };

global_allPlayerSleep = false;

in_dimension('overworld',

    __on_tick() ->
    (
        if(global_allPlayerSleep && __check_sleeping_enough(),
            global_allPlayerSleep = false;
            l = day_time() + 24000;
            day_time(l - l % 24000);
            // doing this also sets rain and thunder to 0 because of scarpet impl, exactly what vanilla does when sleeping (from ServerWorld in yarn)
            if (weather('rain') > 0, weather('clear', 0))
        );
    );

    // player login
    __on_player_connects(player) ->
    (
        __update_sleeping_players();
    );

    // player logout
    __on_player_disconnects(player, reason) ->
    (
        __update_sleeping_players();
    );

    // player wakes up
    __on_player_wakes_up(player) ->
    (
        __update_sleeping_players();
    );

    // player interacts with bed
    __on_player_interacts_with_block(player, hand, block, face, hitvec) ->
    (
        if(block ~ '_bed$',
            __update_sleeping_players();
            return();
        );
    );

    __update_sleeping_players() ->
    (
        global_allPlayerSleep = false;
        if(length(entity_list('player')) > 0,
            i = 0; j = 0;
            for(entity_list('player'),
                if(_~'gamemode' == 'spectator' || _~'player_type' == 'fake',
                    i = i + 1;,
                    _~'pose' == 'sleeping',
                    j = j + 1;
                );
            );
            global_allPlayerSleep = j > 0 && j >= length(entity_list('player')) - i;
        );
    );

    __check_sleeping_enough() ->
    (
        for(entity_list('player'),
            if(_~'gamemode' == 'spectator' || _~'player_type' == 'fake' || query(_, 'nbt', 'SleepTimer') >= 100,
                null,
                return(false)
            )
        );
        return(true);
    );

);
