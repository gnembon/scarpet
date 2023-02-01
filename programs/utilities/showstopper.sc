// An app to get notified or even pause the game when things start going too slow
// By altrisi
// Configuration is done via the in-game command, and will be saved and restored from a configuration file


// Mode definition

// Returns the tick event handler for this mode.
global_HANDLER = 0;
// Returns the information provider for this mode.
// It's a function that takes no arguments and returns a string
global_INFO_PROVIDER = 1;

// null means no event handler. Given it's nothing, it needs no global_INFO_PROVIDER
OFF = [
    null,
];
overN = [
    'overNHandler',
    _() -> 'Running in average of ' + global_interval + ' over ' + global_argument + 'mspt mode'
];
overNSingle = [
    'overNSingleHandler',
    _() -> 'Running in single tick over ' + global_argument + 'mspt mode'
];
overPercentage = [
    'overPercentageHandler', 
    _() -> 'Running in average of ' + global_interval + ' ticks being at least '+(global_argument-1)*100+'% higher than general average mode'
];

// Mode setter definition
// Whether this mode requires the extra argument to be passed to the command
global_REQUIRES_ARGUMENT = 0;
// Handles setting the argument and returns the mode to be set
global_ARG_TO_MODE_FUNCTION = 1;

// Holds name -> modeSetter
global_MODE_MAP = {
    'off'              -> [false, _(arg, outer(OFF))            -> OFF],
    'over50mspt'       -> [false, _(arg, outer(overN))          -> (global_argument = 50;  return(overN))],
    'over50msptsingle' -> [false, _(arg, outer(overNSingle))    -> (global_argument = 50;  return(overNSingle))],
    'overpercentage'   -> [true,  _(arg, outer(overPercentage)) -> (global_argument = 1 + arg/100; return(overPercentage))],
    'overN'            -> [true,  _(arg, outer(overN))          -> (global_argument = arg; return(overN))],
    'overNsingle'      -> [true,  _(arg, outer(overNSingle))    -> (global_argument = arg; return(overNSingle))]
};

__config() -> {
    'scope' -> 'global',
    'commands' -> {
        '' -> _() 
            -> print(
                '----------- [Showstopper Commands] ----------- \n' +
                'mode <mode> [argument] | Current: ' + global_modeSaveName + '\n' +
                '  - Sets the listener mode \n' +
                'freeze [boolean] | Current: ' + global_shouldFreeze + '\n' + 
                '  - Sets whether the game should be frozen on anomaly \n' +
                'interval <ticks> | Current: ' + global_interval + '\n' +
                '  - Sets the ticks to be grouped in non-single modes \n' +
                'cooldown <ticks> | Current: ' + global_cooldown + '\n' +
                '  - Sets the cooldown to wait after an anomaly'
            ),
        'freeze' -> _()->setFreeze(!global_shouldFreeze),
        'freeze <bool>' -> 'setFreeze',
        'interval <ticks>' -> 'setInterval',
        'cooldown <ticks>' -> 'setCooldown',
        'mode <mode>' -> ['setMode', ''],
        'mode <mode> <argument>' -> 'setMode'
    },
    'arguments' -> {
        'mode' -> {
            'type' -> 'term',
            'options' -> keys(global_MODE_MAP)
        },
        'argument' -> {
            'type' -> 'float',
            'min' -> 0,
            'suggest' -> [7.5, 10, 25, 50, 125]
        },
        'ticks' -> {
            'type' -> 'int',
            'min' -> 0,
            'suggest' -> [2, 4, 10, 20]
        }
    }
};

// Configuration

global_mode = OFF;
global_argument = -1;
global_shouldFreeze = false;
global_interval = 4;
global_cooldown = 0;

// runtime flags
global_onCooldown = false;
global_shouldSave = false; // updated after loading config

overNSingleHandler() -> (
    lastTimes = system_info('server_last_tick_times');
    
    if (lastTimes:0 >= global_argument, 
        reactToAnomaly('Single tick took %.2f ms', lastTimes:0);
    );
);

overNHandler() -> (
    lastTimes = system_info('server_last_tick_times');
    time = averageLast(lastTimes, global_interval);
    
    if (time >= global_argument,
        reactToAnomaly('Last %d tick average took %.2f ms', global_interval, time);
    );
);

overPercentageHandler() -> (
    lastTimes = system_info('server_last_tick_times');
    lastTime = averageLast(lastTimes, global_interval);
    loop(global_interval, // delete the ones from the interval for the general average
        delete(lastTimes, 0);
    );
    generalAverageTime = averageTimes(lastTimes);
    if (lastTime > generalAverageTime * global_argument,
        if (global_shouldFreeze,
            // last_tick_times keeps counting times even when frozen, give some time for the numbers to recover
            cooldown(100);
        );
        reactToAnomaly('Last %d tick average was %.2fms when the general average time was %.2f', global_interval, lastTime, generalAverageTime);
    );
);

setMode(mode, argument) -> (
    modeSetter = global_MODE_MAP:mode;
    if (global_shouldSave, // Don't check argument while on startup
        if (modeSetter:global_REQUIRES_ARGUMENT && argument == '',
            print('Mode "' + mode + '" requires an argument, but none has been provided!');
            return();
        , !modeSetter:global_REQUIRES_ARGUMENT && argument != '',
            print('Mode "' + mode + '" doesn\'t accept an argument, but one has been provided!');
            return();
        );
    );
    global_mode = call(modeSetter:global_ARG_TO_MODE_FUNCTION, argument);
    handle_event('tick', null); // Remove old handler
    handle_event('tick', global_mode:global_HANDLER); // set new handler
        
    print('Showstopper mode has been set to ' + mode + ' ' + argument);
    global_modeSaveName = mode;
    saveToFile();
);

setFreeze(value) -> (
    global_shouldFreeze = value;
    if (value,
        print('Showstopper will freeze the game on a problematic tick');
    ,
        print('Showstopper will only notify you on a problematic tick');
    );
    saveToFile();
);

setInterval(value) -> (
    global_interval = value;
    print('The number of ticks to aggregate for non-single modes has been set to ' + value);
    saveToFile();
);

setCooldown(ticks) -> (
    global_cooldown = ticks;
    print('Set the cooldown to ' + ticks + ' ticks');
    saveToFile();
);

reactToAnomaly(message, ...stringArgs) -> (
    for(player('*'), sound('minecraft:block.note_block.didgeridoo', pos(_), 100, 0.8, 'voice'));
    print(player('*'), '----------------- [Showstopper] -----------------');
    print(player('*'), str(message, stringArgs));
    if (global_shouldFreeze,
        run('tick freeze on');
        print(player('*'), 'The game has been paused');
    );
    menu = format(' Quick actions: ', 'g [DISABLE]', command('mode off'), '^g Click to disable showstopper');
    if (global_shouldFreeze, 
        menu += format('y  [UNPAUSE]', '!/tick freeze off', '^g Click to unpause the game');
    );
    print(player('*'), call(global_mode:global_INFO_PROVIDER));
    print(player('*'), menu);
    print(player('*'), '------------------------------------------------');
    cooldown(global_cooldown);
);

cooldown(ticks) -> (
    if (!global_onCooldown && ticks > 0,
        handle_event('tick', null);
        global_onCooldown = true;
        schedule(ticks, _() -> (
            handle_event('tick', global_mode:global_HANDLER);
            global_onCooldown = false;
        ));
    );
);

// Save utils

global_modeSaveName = 'off';

global_DATA_VERSION = 1;

saveToFile() -> (
    if (global_shouldSave,
        data = {
            'version' -> global_DATA_VERSION,
            'mode' -> global_modeSaveName,
            'argument' -> global_argument,
            'cooldown' -> global_cooldown,
            'freeze' -> global_shouldFreeze,
            'interval' -> global_interval
        };
        write_file('config', 'json', data);
    );
);

loadFromFile() -> (
    logger('Loading showstopper\'s configuration file...');
    try(
        data = read_file('config', 'json');
    , 'json_error',
        print(format('r Error while loading showstopper\'s config file!'));
        print(format('g '+_:'error'));
        print(format('r The config file will be reset if you change something, or you can fix it and reload the app before that'));
        return();
    );
    if (data == null,
        logger('No config file found, preparing showstopper first launch...');
        return(); // a lot to prepare, I know!
    );
    if (data:'version' > global_DATA_VERSION,
        print(format('r Config file format is from a newer version!'));
        print(format('r Config file will be ignored and recreated on configuration change'));
        return();
    );
    setMode(data:'mode', 0);
    global_argument = data:'argument';
    setCooldown(data:'cooldown');
    setFreeze(data:'freeze');
    setInterval(data:'interval');
    print(format('g Showstopper config has loaded correctly!'));
);

loadFromFile();
global_shouldSave = true;

// Utils

command(args) -> '!/' + system_info('app_name') + ' ' + args;

averageTimes(times) -> reduce(times, _a + _, 0)/length(times);

averageLast(times, count) -> (
    c_for(i = 0, i < count, i += 1,
        acumulator += times:i;
    );
    acumulator/count
);