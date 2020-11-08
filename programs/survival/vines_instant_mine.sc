///
// Vines Instant Mine
// by BisUmTo
// (Carpet Mod 1.4.9)
//
// Shears will instant-mine vines
///

__config() -> {'stay_loaded' -> true};

_vines_instant_mine(player, block) -> (
    g = player ~ 'gamemode';
    holds = player ~ 'holds';
    if(g == 'spectator' || g == 'creative' || !holds || block != 'vine', return());
    [item, count, nbt] = holds;
    if(item!='shears',return());
    if(g == 'adventure' && !nbt:'CanDestroy' ~ '"minecraft:vine"', return());
    schedule(0,
        _(outer(player), outer(block)) -> (
            if(player ~ 'breaking_progress' && player ~ 'active_block'==block,
                harvest(player, block)
            )
        )
    );
);

__on_player_clicks_block(player, block, face) -> (
    _vines_instant_mine(player, block);
)
