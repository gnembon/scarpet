// by Ghoulboy78

__command()->(
    print(format(
        'wi Available commands:\n',
        'ti	/lifetimes start :','wi  Will start recording lifetimes of all mobs\n',
        'ti	/lifetimes report :','wi  Prints out current lifetimes of mob categories\n',
        'ti	/lifetimes report_verbose :','wi  Prints out current lifetimes of mob types\n',
        'ti	/lifetimes stop :','wi  Will print final full report of all mob lifetimes'
    ));
	null
);

global_start_time = 0;

global_lifetimes = {};

global_lifetimes_categories = {};

_track(e, r)->(
	entry = global_lifetimes:str(e);
	if(entry==null,
		global_lifetimes:str(e) = [e~'age', 1],
		global_lifetimes:str(e) = entry + [e~'age', 1] //can't use += cos of bug
	);
	
	category = e~'category';
	entry = global_lifetimes_categories:str(category);
	if(entry==null,
		global_lifetimes_categories:str(category) = [e~'age', 1],
		global_lifetimes_categories:str(category) = entry + [e~'age', 1] //can't use += cos of bug
	)
);

start()->(
	global_start_time = tick_time();
	entity_load_handler('!misc',_(e, new)->if(new, entity_event(e, 'on_death', '_track')));
	print('Began tracking mob lifetimes');
	null
);

stop()->_reset();

report()->_report(false);

report_verbose()->_report(true);

_report(verbose)->(
	if(global_start_time==0, exit('Nothing to report'));
    total_time=0;
	total_mobs=0;
	minutes = round((tick_time()-global_start_time)/12)/100; //2 d.p
    print('Average lifetimes for mobs (in seconds) over '+minutes+' minutes:');
	map_to_iterate_over = if(verbose, global_lifetimes, global_lifetimes_categories);
    for(map_to_iterate_over,
		entry = map_to_iterate_over:_;
        mob_avg=round(entry:0/entry:1*5)/100; //2 d.p
        print('    '+_+': '+mob_avg+' seconds');
        total_time+=entry:0;
		total_mobs+=entry:1;
    );
    print('Total average lifetime over '+total_mobs+' mobs is: '+round(total_time/total_mobs*5)/100+' seconds');
	null
);

//Other functions

_reset()->(
    report_verbose();
    print('Reset all values');
    global_start_time=0;
    global_lifetimes={};
    global_lifetimes_categories={};
    null
)
