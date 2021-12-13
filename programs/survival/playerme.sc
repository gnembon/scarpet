// A wrapper over carpet /player command that will only work if you target yourself as the player
// By BisUmTo & DukeEdivad05

__config() -> {
    'stay_loaded' -> true,
    'scope' -> 'global',
    'commands' -> {
        'attack' ->                     _() ->   _player('attack'),
        'attack continuous' ->          _() ->   _player('attack continuous'),
        'attack interval <ticks>' ->    _(tk) -> _player('attack interval ' + tk),
        'attack once' ->                _() ->   _player('attack once'),
        'dismount' ->                   _() ->   _player('dismount'),
        'drop' ->                       _() ->   _player('drop'),
        'drop <hotbarslot>' ->          _(so) -> _player('drop ' + so),
        'drop all' ->                   _() ->   _player('drop all'),
        'drop continuous' ->            _() ->   _player('drop continuous'),
        'drop interval <ticks>' ->      _(tk) -> _player('drop interval ' + tk),
        'drop mainhand' ->              _() ->   _player('drop mainhand'),
        'drop offhand' ->               _() ->   _player('drop offhand'),
        'drop once' ->                  _() ->   _player('drop once'),
        'dropStack' ->                  _() ->   _player('dropStack'),
        'dropStack <hotbarslot>' ->     _(so) -> _player('dropStack '+so),
        'dropStack all' ->              _() ->   _player('dropStack all'),
        'dropStack continuous' ->       _() ->   _player('dropStack continuous'),
        'dropStack mainhand' ->         _() ->   _player('dropStack mainhand'),
        'dropStack offhand' ->          _() ->   _player('dropStack offhand'),
        'dropStack once' ->             _() ->   _player('dropStack once'),
        'hotbar <hotbarslot>' ->        _(so) -> _player('hotbar ' + so),
        'mount' ->                      _() ->   _player('mount'),
        'mount anything' ->             _() ->   _player('mount anything'),
        'shadow' ->                     _() ->   _player('shadow'),
        'stop' ->                       _() ->   _player('stop'),
        'swapHands' ->                  _() ->   _player('swapHands'),
        'swapHands continuous' ->       _() ->   _player('swapHands continuous'),
        'swapHands interval <ticks>' -> _(tk) -> _player('swapHands interval' + tk),
        'swapHands once' ->             _() ->   _player('swapHands once'),
        'use' ->                        _() ->   _player('use'),
        'use continuous' ->             _() ->   _player('use continuous'),
        'use interval <ticks>' ->       _(tk) -> _player('use interval ' + tk),
        'use once' ->                   _() ->   _player('use once')
    },
    'arguments' -> {
        'ticks' -> {'type' -> 'int', 'min' -> 1, 'max' -> 72000, 'suggest' -> [20]},
        'hotbarslot' -> {'type' -> 'int', 'min' -> 1, 'max' -> 9, 'suggest' -> [1,2,3,4,5,6,7,8,9]},
    }
};

_player(argument) -> run(str('player %s %s', player(), argument))
