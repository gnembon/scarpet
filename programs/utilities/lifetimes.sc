//An app to tell the lifetimes of mobs which die, useful for debugging a mobfarm etc.
//Can also specify an area and see lifetimes of mobs which die in that area. (Thanks to this suggestion: https://discord.com/channels/211786369951989762/573613501164159016/857161350014173234)
// by Ghoulboy78

import('math', '_round');

help()-> print(
    'Available commands:\n'+
    '  /lifetimes start : Will start recording lifetimes of all mobs\n'+
    '  /lifetimes start <pos1> <pos2>: Will start recording lifetimes of all mobs within an area defined by  positions\n'+
    '  /lifetimes report [verbose] : Prints out current average mob lifetimes, broken down by mob category.'+
    ' With added "verbose" keyword prints average lifetime for each mob type individually.\n'+
    '  /lifetimes stop : Will print final full (and verbose) report of all mob lifetimes'
);

__config()->{
    'stay_loaded'->true,
    'commands'->{
        ''->'help',
        'start'->['start', null, null],
        'start <pos1> <pos2>'->'start',
        'stop'->'stop',
        'report'->['report', false],
        'report verbose'->['report', true],
    },
    'scope'->'global'
};

global_start_time = 0; //Used to keep track of total measurement time.

global_positions=[null, null];

global_mob_category_lifetimes={};//to keep track of lifetimes across mob groups for shorter report

global_mob_lifetimes={};//To keep track of which mob types are actually there along with a pair of the total lifetime and the number of mobs, so I can calculate average quickly

//todo think of a better name for this variable
global_lifetime_counters={};//In here I keep track of the starting lifetime of all mobs, and when they die I remove their entry here and add it to global_mob_lifetimes


_track(e)-> (
    global_lifetime_counters:e = tick_time();
    print('Tracking '+e);
    entity_event(e, 'on_death', _(e, r)-> (
        //Checking if we are measuring within a box, and if so whether entity is in said box
        if(global_positions!=[null, null] && __in_volume(global_positions:0, global_positions:1, pos(e)),

            if(!has(global_mob_lifetimes,(e~'type')), global_mob_lifetimes:(e~'type') = [0,0]);//initialising counter if it doesn't exist
            if(!has(global_mob_category_lifetimes,(e~'category')), global_mob_category_lifetimes:(e~'category') = [0,0]);
            
            global_mob_lifetimes:(e~'type'):0 += 1;
            global_mob_lifetimes:(e~'type'):1 += tick_time() - global_lifetime_counters:e;//mob's lifetime, i.e birth time - death time.
            //Doing the same, but for mob category for shorter report
            global_mob_category_lifetimes:(e~'category'):0 += 1;
            global_mob_category_lifetimes:(e~'category'):1 += tick_time() - global_lifetime_counters:e;
        );
        delete(global_lifetime_counters, e)
    ))
);

//Commands

start(pos1, pos2)-> (
    _reset();//Getting rid of previous values, in case we hadn't already. In theory unnecessary, in practice a JIC measure

    global_positions = [pos1, pos2];//Check for null values is done in entity 'on_death' event
    global_start_time = tick_time();
    entity_load_handler('living', '_track');
    print('Began tracking lifetimes for all mobs');
);

stop()-> (
    if(global_start_time==0, exit(print(format('r You are not currently tracking'))));//cant stop before starting
    entity_load_handler('living', null);
    print('Stopped tracking lifetimes, printing report...');
    report(true);
    _reset()
);

report(verbose)->(
    if(global_start_time==0, exit(print(format('r Can only report while tracking lifetimes!'))));//can only report while tracking ofc
    total_count=0;
    total_lifetime=0;
    ticks = tick_time()-global_start_time;
    print(str('Average lifetimes for mobs (in ticks) over %s ticks (%s mins %s s):', ticks, floor(ticks/1200), round(ticks/20) % 60));
    if(verbose,
        for(global_mob_lifetimes,
            count = global_mob_lifetimes:_:0;
            total = global_mob_lifetimes:_:1;
            print(format(
                str('gi     %s: %s mobs, avg lifetime: %s t', _, count, _round(total/count, 0.01)),
            ));
            total_count+=count;
            total_lifetime+=total
        ),
        //shorter report
        for(global_mob_category_lifetimes,
            count = global_mob_category_lifetimes:_:0;
            total = global_mob_category_lifetimes:_:1;
            print(format(
                str('gi     %s: %s mobs, avg lifetime: %s t', _, count, _round(total/count, 0.01)),
            ));
            total_count+=count;
            total_lifetime+=total
        )
    );
    print('\nTotal average lifetime for all mobs is: '+ _round(total_lifetime/total_count, 0.001)+' ticks')
);

_reset()->(
    global_start_time = 0;
    global_mob_lifetimes = {};
    global_lifetime_counters = {};
    global_mob_category_lifetimes = {}
)

__in_volume(box_1,box_2,pos)->(//Checks if pos is in volume defined by box_1 and box_2, todo move to a utility library
    [x,y,z]=pos;
    [min_x, min_y, min_z]=map(box_1, max(_, box_2:_i));
    [max_x, max_y, max_z]=map(box_1, min(_, box_2:_i));

    min_x <= x && x <= max_x && min_y <= y && y <=max_y && min_z <= z && z <= max_z
);