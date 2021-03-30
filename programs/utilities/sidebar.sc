///
// Deopped Players Sidebar Changer
// by BisUmTo
// (Carpet Mod 1.4.18)
//
// Simple command that allows deopped players to change the scoreboard on the sidebar.
//
// Command to show a specific score on the sidebar:
// /sidebar <score>
//
// Command to hide the sidebar:
// /sidebar
///

__config() -> {
    'stay_loaded' -> true,
    'commands' -> {
        '' -> _() -> run('scoreboard objectives setdisplay sidebar'),
        '<objective>' -> _(score) -> run('scoreboard objectives setdisplay sidebar ' + score:0)
    }
}
