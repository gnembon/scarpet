// by Ghoulboy78

__command()->(
    print(
        'Available commands:\n'+
        '/lifetimes start : Will start recording lifetimes of all mobs\n'+
        '/lifetimes report : Prints out current lifetimes of mobs\n'+
        '/lifetimes stop : Will print final full report of all mob lifetimes'
    );
    return('')
);

//Globals

global_start=false;

global_ticks=0;

global_minutes=0.0;//floating point number

global_mobs=m();//To keep track of which mob types are actually there

global_lifetimes=m();//SO that I can measure multiple lifetimes at the same time, I have to have an entry for each mob which he puts into global_lifetimes when he dies

global_lifetimes_global=m();


//Events

__on_tick()->(
    if(!global_start,return());
    global_ticks+=1;
    global_minutes=round(global_ticks/12)/100;//it would be round(global_ticks/1200(mins)*100)/100, but I simplified
    e=filter(entity_selector('@e'),query(_,'category')!='misc');
    for(e,
        if(!query(_,'has_tag','lifetime'),
            modify(_,'tag','lifetime');
            global_mobs:query(_,'type')+=1;
            entity_event(_,'on_death',_(e,c)->put(global_lifetimes_global,query(e,'type'),get(global_lifetimes_global,query(e,'type'))+global_lifetimes:e));//tried to define func but nulpointerexception bs
        );
        global_lifetimes:_+=1
    )
);

//Commands

start()->global_start=true;

stop()->_reset();

report()->(
    total_avg=0.0;
    print('Average lifetimes for mobs (in ticks) over '+global_minutes+' minutes:');
    for(global_mobs,
        mob_avg=round(global_lifetimes_global:_/(global_mobs:_*6))/10
        print('    '+_+': '+mob_avg+' seconds');
        total_avg=(total_avg+mob_avg)/2
    );
    print('Total average lifetime for all mobs is: '+total_avg+' seconds')
);

//Other functions

_reset()->(
    report();
    print('Reset all values, can now measure lifetimes again.');
    global_start=false;
    global_ticks=0;
    global_minutes=0.0;
    global_mobs=m();
    global_lifetimes=m();
    global_lifetimes_global=m();
    return('')
)
