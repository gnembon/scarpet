//Counts blocks in a given region, optionally filtering by block type or tag. 
//Prints the output into a chat or a file.
//If vertical scan is set to true, it will return a separate report for each y value.
//By gnembom and Firigion

__config() ->
{
   'commands' -> {
      '<from_pos> <to_pos>' -> 'count_blocks',
      '<from_pos> <to_pos> <filter>' -> 'count_blocks_filter',
      '<from_pos> <to_pos> <blockpredicate>' -> 'count_blocks_predicate',
      'save' -> ['save_scan', null],
      'save <name>' -> 'save_scan',
      'autosave <bool>' -> 'autosave',
      'vertical_scan <bool>' -> 'vertical_scan', // I'm keeping this as an option because doing the vertically spearated scans is 30% to 100% slower
   },
   'allow_command_conflicts' -> true
};

count_blocks(from_pos, to_pos) ->
(
    stats = {}; total = 0;
    if(global_vertscan,
      // do a scan separating results by y value
      area = get_flat_area(from_pos, to_pos);
      [minpos, maxpos] = get_minmax_pos(from_pos, to_pos);
      for(range(minpos, maxpos+1), stats:_ = {});
      volume(from_pos, to_pos, 
         stats:(minpos+floor(total/area)):str(_) += 1; //very slightly faster than calling pos(_):1
         total += 1; 
      );
      for(pairs(stats),
         [y, level_stats] = _;
         print(format('yb Results for y='+str(y)));
         tally(level_stats, area);
      );
      global_stats = stats,
      // do a regular scan
      volume(from_pos, to_pos, stats:str(_) += 1 );
      total = sum(... values(stats));
      tally(stats, total);
    );

    if(global_autosave, save_scan(null));
);

count_blocks_filter(from_pos, to_pos, filtr) ->
(
    stats = {}; total = 0;
    if(global_vertscan,
      // do a scan separating results by y value
      area = get_flat_area(from_pos, to_pos);
      [minpos, maxpos] = get_minmax_pos(from_pos, to_pos);
      for(range(minpos, maxpos+1), stats:_ = {});
      volume(from_pos, to_pos, 
         if (_~filtr, stats:(minpos+floor(total/area)):str(_) += 1); //very slightly faster than calling pos(_):1
         total += 1; 
      );
      for(pairs(stats),
         [y, level_stats] = _;
         if(level_stats=={}, continue());
         print(format('yb Results for y='+str(y)));
         tally(level_stats, area);
      );
      global_stats = stats,
      // do a regular scan
      volume(from_pos, to_pos, total += 1; if (_~filtr, stats:str(_) += 1 ) );
      tally(stats, total);
    );

    if(global_autosave, save_scan(null));
);

count_blocks_tag(from_pos, to_pos, tag) ->
(
    stats = {}; total = 0;
    if(global_vertscan,
      // do a scan separating results by y value
      area = get_flat_area(from_pos, to_pos);
      [minpos, maxpos] = get_minmax_pos(from_pos, to_pos);
      for(range(minpos, maxpos+1), stats:_ = {});
      volume(from_pos, to_pos, 
         if (block_tags(_, tag), stats:(minpos+floor(total/area)):str(_) += 1); //very slightly faster than calling pos(_):1
         total += 1; 
      );
      for(pairs(stats),
         [y, level_stats] = _;
         if(level_stats=={}, continue());
         print(format('yb Results for y='+str(y)));
         tally(level_stats, area);
      );
      global_stats = stats,
      // do a regular scan
      volume(from_pos, to_pos, total += 1; if (block_tags(_, tag), stats:str(_) += 1 ) );
      tally(stats, total);
    );

    if(global_autosave, save_scan(null));
);

count_blocks_predicate(from_pos, to_pos, predicate) ->
(
    [block, tag, trash, trash] = predicate;
    if(block==null, 
      count_blocks_tag(from_pos, to_pos, tag),
      count_blocks_filter(from_pos, to_pos, filtr)
    )
);

tally(stats, grand_total) ->
(
   total = sum(... values(stats));
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

   //for saving purposes
   global_stats = stats;
   global_stats:'total' = grand_total;

);

dpad(num, wid) ->
(
   strn = str(num);
   if (length(strn) < wid, strn = '...'*(wid-length(strn))+strn);
   strn;
);

global_stats = {};
save_scan(name) -> (
   if(global_stats=={}, print(format('r Need to run a scan first.')); exit());

   if(name==null,
      count = length(list_files('', 'json'));
      name = count+1;
   );
   write_file(name, 'json', global_stats);
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
