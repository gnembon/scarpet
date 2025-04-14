//Counts blocks in a given region, optionally filtering by block type or tag. 
//Prints the output into a chat or a file.
//If vertical scan is set to true, it will return a separate report for each y value.
//By gnembon and Firigion

__config() ->
{
   'commands' -> {
      '' -> 'help',
      '<from_pos> <to_pos>' -> 'count_blocks',
      '<from_pos> <to_pos> <filter>' -> 'count_blocks_filter',
      '<from_pos> <to_pos> <blockpredicate>' -> 'count_blocks_predicate',
      'save' -> ['save_scan', null],
      'save <name>' -> 'save_scan',
      'autosave <bool>' -> 'autosave',
      'layered_scan <bool>' -> 'vertical_scan', // I'm keeping this as an option because doing the vertically spearated scans is 30% to 100% slower
      'histogram <block>' -> 'histogram',
      'tally_level <int>' -> 'tally_level',
      //'book' -> 'get_book_report',
      'last' -> 'last_report',
   },
   'allow_command_conflicts' -> true
};


/////////////////////////////
///// Scans
/////////////////////////////

count_blocks(from_pos, to_pos) ->
(
    global_data = make_data_stuct(from_pos, to_pos, null); //make global empty data structure
    stats = global_data:'stats'; //save reference for easier access

    if(global_vertscan,
      // do a scan separating results by y value
      area = global_data:'area';
      [miny, maxy] = get_minmax_pos(from_pos, to_pos);
      total = 0;

      volume(from_pos, to_pos, 
         stats:(miny+floor(total/area)):str(_) += 1; //very slightly faster than calling pos(_):1
         total += 1; 
      );
      vertical_report(),

      // do a regular scan
      volume(from_pos, to_pos, stats:str(_) += 1 );
      tally(global_data:'stats', global_data:'volume');
      global_data:'total' = sum(... values(global_data:'stats'));
    );

    if(global_autosave, save_scan(null));
);

count_blocks_filter(from_pos, to_pos, filtr) ->
(
    global_data = make_data_stuct(from_pos, to_pos, filtr); //make global empty data structure
    stats = global_data:'stats'; //save reference for easier access

    if(global_vertscan,
      // do a scan separating results by y value
      area = global_data:'area';
      [miny, maxy] = get_minmax_pos(from_pos, to_pos);
      total = 0;

      volume(from_pos, to_pos, 
         if (_~filtr, stats:(miny+floor(total/area)):str(_) += 1); //very slightly faster than calling pos(_):1
         total += 1; 
      );
      vertical_report(),
      // do a regular scan
      volume(from_pos, to_pos, total += 1; if (_~filtr, stats:str(_) += 1 ) );
      tally(global_data:'stats', global_data:'volume');
      global_data:'total' = sum(... values(global_data:'stats'));
    );

    if(global_autosave, save_scan(null));
);

count_blocks_tag(from_pos, to_pos, tag) ->
(   
    global_data = make_data_stuct(from_pos, to_pos,'#'+tag); //make global empty data structure
    stats = global_data:'stats'; //save reference for easier access

    if(global_vertscan,
      // do a scan separating results by y value
      area = global_data:'area';
      [miny, maxy] = get_minmax_pos(from_pos, to_pos);
      total = 0;

      volume(from_pos, to_pos, 
         if (block_tags(_, tag), stats:(miny+floor(total/area)):str(_) += 1); //very slightly faster than calling pos(_):1
         total += 1; 
      );
      vertical_report(),
      // do a regular scan
      volume(from_pos, to_pos, total += 1; if (block_tags(_, tag), stats:str(_) += 1 ) );
      tally(global_data:'stats', global_data:'volume');
      global_data:'total' = sum(... values(global_data:'stats'));
    );

    if(global_autosave, save_scan(null));
);

count_blocks_block(from_pos, to_pos, block) ->
(
    global_data = make_data_stuct(from_pos, to_pos,'minecraft:'+block); //make global empty data structure
    stats = global_data:'stats'; //save reference for easier access

    if(global_vertscan,
      // do a scan separating results by y value
      area = global_data:'area';
      [miny, maxy] = get_minmax_pos(from_pos, to_pos);
      total = 0;

      volume(from_pos, to_pos, 
         if (_==block, stats:(miny+floor(total/area)):str(_) += 1); //very slightly faster than calling pos(_):1
         total += 1; 
      );
      vertical_report(),
      // do a regular scan
      volume(from_pos, to_pos, total += 1; if (_==block, stats:str(_) += 1 ) );
      tally(global_data:'stats', global_data:'volume');
      global_data:'total' = sum(... values(global_data:'stats'));
    );

    if(global_autosave, save_scan(null));
);

count_blocks_predicate(from_pos, to_pos, predicate) ->
(
    [block, tag, trash, trash] = predicate;
    if(block==null, 
      count_blocks_tag(from_pos, to_pos, tag),
      count_blocks_block(from_pos, to_pos, block)
    )
);


/////////////////////////////
///// Reports
/////////////////////////////

tally(stats, grand_total) ->
(  
   total = sum(... values(stats));
   if(total==null, nothing_found());
   if (total == grand_total,
      print(format('wb    Count  |%Area | Block'));
      print('.---------+------+---------------');
      for ( sort_key(keys(stats), stats:_ ),
         print(str('%s | %05.2f%% | %s', dpad(stats:_, 9), 100*stats:_/total, _))
      );
      print('Total: '+total);
   ,
      print(format('wb    Count  |%Pool |%Area | Block'));
      print('.---------+------+------+---------------');
      for ( sort_key(keys(stats), stats:_ ),
         print(str('%s | %05.2f%% | %05.2f%% | %s', dpad(stats:_, 9), 100*stats:_/total, 100*stats:_/grand_total, _))
      );
      print(format('gi Total tally: '+total));
      print(format('gi Scanned area: '+grand_total));
   );
);

dpad(num, wid) ->
(
   strn = str(num);
   if (length(strn) < wid, strn = '...'*(wid-length(strn))+strn);
   strn;
);

hpad(num, wid) ->
(
   strn = str(num);
   if (length(strn) < wid, strn = strn+'...'*(wid-length(strn)));
   strn;
);

vertical_report() -> (
   //generate a map of the y level that contains each block
   stats = global_data:'stats';
   area = global_data:'area';
   blocks = global_data:'blocks';

   if(blocks=={},
      for(reverse(sort(keys(stats))),
         y =  _; level_stats = stats:y;
         for(keys(level_stats),
            if(blocks:_==null,
               blocks:_ = [y],
               blocks:_ += y
            );
            global_data:'total' += level_stats:_;
         )
      )
   );
   if(blocks=={}, nothing_found());

   //print block names and y levels as clickable things to display tallys and histograms
   print(format(make_clickable(blocks, 'histogram', 'y')));
   print(format(make_clickable(stats, 'tally_level', 'c')));
   //print(format('m [Get book report]', str('!/%s book', system_info('app_name')) ));
);

make_clickable(input_map, fun, color) -> (
   output = [];
   for(sort(keys(input_map)),
      if(input_map:_=={}, continue());
      line = [
         str('%s [%s] ', color, _), 
         '^g Click to display results for '+_,
         str('!/%s %s %s', system_info('app_name'), fun, _)
      ];
      put(output, null, line, 'extend');
   );
   output
);

histogram(block) -> (
   block = str(block);
   blocks = global_data:'blocks';
   if(blocks:block==null, _error(block + ' not available from last scan.'));
   maxcount = 0;
   values = map(blocks:block,
      y = _;
      count = global_data:'stats':y:block;
      maxcount = max(maxcount, count);
      [y, count];
   );
   divisor = ceil(maxcount/100); //to make sure the histogram bar is not longer than the chat box
   print('');
   print(format('y Results for ', 'yb '+block));
   for(values, 
      [y, count] = _;
      total += count;
      print(format(str('w %s %s', hpad(y, 4), '|'*(count/divisor) ), '^g count: '+count) )
   );

   volume = global_data:'volume';
   total_found = global_data:'total';
   if(volume==total_found,
      print(format(str('g Total: %s  |  %05.2f%% of the scanned volume', total, total/volume*100))),
      print(format(str('g Total: %s  |  %05.2f%% of total volume  |  %05.2f%% of pool  ', total, total/volume*100, total/total_found*100))),
   );
);

tally_level(y) -> (
   stats = global_data:'stats';
   area = global_data:'area';

   if(stats:y==null, _error(y + ' not available from last scan.'));
   print('');
   print(format('yb Results for y='+str(y)));
   tally(stats:y, area);
);

last_report() -> (
   if(
      type(global_data:'stats':(global_data:'corners':0:1))=='map', //a convoluted way to see if scan was layered 
         vertical_report(),
      global_data,
         tally(global_data:'stats', global_data:'volume'),
      //else
         _error('You need to make a scan first')
   )
);

// unused for now
get_book_report() -> //print(format('g This option is not yet implemented')); //19 characters wide, 14 lines
(
   cover_page = [
      {
         'text' -> 'Available y levels',
         'color' -> 'dark_purple',
         'bold' -> true,
         'underlined' -> true,
      },
      ...map( range(-55, -30),
         {
            'text' -> '[45]',
            'color' -> 'gold',
            'clickEvent' -> {'action'->'change_page', 'value' -> str(_i+2) }
         }
      )
   ];
   cover_page = encode_nbt(cover_page);
   pages = map(range(99), encode_nbt({'text'->''}));

   book_nbt = {
      'title'-> 'Results [-56 to 40]',
      'author' -> 'scan',
      'pages' -> map([cover_page, ...pages], '\'_\''),
   };
   run(str('/give @p written_book{%s} 1', encode_nbt(book_nbt)) );
);
//title:"",author:"",pages:['[{"text":"Available y levels\\n","color":"dark_blue","bold":true,"underlined":true,"hoverEvent":{"action":"show_text","contents":[{"text":"count:123584"}]}},{"text":"[45]","color":"gold","clickEvent":{"action":"change_page","value":"2"}},{"text":"[55]","color":"gold","clickEvent":{"action":"change_page","value":"3"}},{"text":"[123]","color":"gold","clickEvent":{"action":"change_page","value":"4"}}]','{"text":""}','{"text":""}','{"text":"smth"}'

nothing_found() -> (
   print(format('gi No blocks were found with requested filter ' + global_data:'filter'));
   exit();
);

/////////////////////////////
///// Others
/////////////////////////////

global_data = {};
make_data_stuct(from, to, filter) -> (
   area = get_flat_area(from, to);
   [miny, maxy] = get_minmax_pos(from, to);
   volume = area * (maxy - miny + 1);
   
   stats = {};
   if(global_vertscan, for(range(miny, maxy+1), stats:_ = {}));
   {
      'corners' -> [from, to],
      'area' -> area,
      'volume' -> volume,
      'filter' -> filter,
      'stats' -> stats,
      'blocks' -> {},
      'total' -> 0,
   }
);

global_stats = {};
save_scan(name) -> (
   if(global_data=={}, _error('Need to run a scan first.'));

   if(name==null,
      count = length(list_files('', 'json'));
      name = count+1;
   );
   save_data = copy(global_data);
   delete(save_data, 'blocks');
   write_file(name, 'json', save_data);
   print(format('gi Saved results of the last scan to ' + name + '.json'));
);

global_autosave = false;
autosave(bool) -> (
   global_autosave = bool;
   print(format('gi Set autosave to ' + bool));
);

global_vertscan = false;
vertical_scan(bool) -> (
   global_vertscan = bool;
   print(format('gi Set vertical scan to ' + bool));
);

get_minmax_pos(from_pos, to_pos) ->(
   minpos = min(from_pos:1, to_pos:1);
   maxpos = max(from_pos:1, to_pos:1);
   [minpos, maxpos]
);

get_flat_area(from_pos, to_pos) -> (
   [dx, dy, dz] = map(from_pos - to_pos, abs(_)+1);
   dx*dz;
);

_error(msg) -> (print(format('r '+msg)); exit());

reverse(list) ->  map(range(length(list)-1, -1, -1), list:_ );

help() -> (
   p = player();
   appname = system_info('app_name');
   print(p, format('yb ==== Block Counter ===='));
   print(p, format('w The following commands are available:'));
   print(p, format(str('c /%s <pos1> <pos2> [filter]', appname), 'g \ : Count blocks of each type in the selected volume. ',
      'g [filter] can be a block tag, a block name or any string (like "ore")'));
   print(p, format(str('c /%s save [name]', appname), 'g \ : Save the last scan into a JSON file'));
   print(p, format(str('c /%s autosave <bool>', appname), 'g \ : Every scan will automatically save into a file once it\'s done. ',
   print(p, format(str('c /%s layered_scan <bool>', appname), 'g \ : The scan results are separated by y levels'));
      'g Click the block names or y values to display results. This mdoe is slightly slower than the regular mode.'));
   print(p, format(str('c /%s last', appname), 'g \ : Display the results of the last scan'));
   print(p, format(str('c /%s histogram', appname), 'g \ : Ignore it- it\'s used internally'));
   print(p, format(str('c /%s tally_level', appname), 'g \ : Ignore it- it\'s used internally'));
   //print(p, format(str('c /%s book', appname), 'g \ : Ignore it- it\'s used internally'));
   print(p, '');
   print(p, format('f by gnembon and Firigion'))
);
