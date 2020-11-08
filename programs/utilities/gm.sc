///
// Deopped Players Gamemode Command
// by BisUmTo
// (Carpet Mod 1.4.9)
//
// Simple command that allows deopped players to change their own gamemode.
//
// /gm <s|s|sp|a>
///

__config() -> {'scope'->'global'};
__command() -> '';

c() -> (run('gamemode creative '+player()~'command_name');return('Set own game mode to Creative Mode'));
s() -> (run('gamemode survival '+player()~'command_name');return('Set own game mode to Survival Mode'));
sp() -> (run('gamemode spectator '+player()~'command_name');return('Set own game mode to Spectator Mode'));
a() -> (run('gamemode adventure '+player()~'command_name');return('Set own game mode to Adventure Mode'))
