///
// Deopped Players Sidebar Changer
// by BisUmTo
// (Carpet Mod 1.4.9)
//
// Simple command that allows deopped players to change the scoreboard on the sidebar.
//
// /sidebar show <scoreboard>
///

__config() -> {'scope'->'global'};
__command() -> '';

show(value) -> run(str('/scoreboard objectives setdisplay sidebar %s',value));
