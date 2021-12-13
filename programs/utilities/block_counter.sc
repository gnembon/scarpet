//Counts blocks in a given region, optionally filtering by block type or tag. 
//Prints the output into a chat or a file.
//By gnembom and Firigion

__config() ->
{
   'commands' -> {
      '<from_pos> <to_pos>' -> 'count_blocks',
      '<from_pos> <to_pos> <filter>' -> 'count_blocks_filter',
      '<from_pos> <to_pos> <blockpredicate>' -> 'count_blocks_predicate',
   },
   'allow_command_conflicts' -> true
};

count_blocks(from_pos, to_pos) ->
(
    stats = {};
    volume(from_pos, to_pos, stats:str(_) += 1 );
    tally(stats, sum(... values(stats)));
);

count_blocks_filter(from_pos, to_pos, filtr) ->
(
    stats = {}; total = 0;
    volume(from_pos, to_pos, total += 1; if (_~filtr, stats:str(_) += 1 ) );
    tally(stats, total);
);

count_blocks_tag(from_pos, to_pos, tag) ->
(
    stats = {}; total = 0;
    volume(from_pos, to_pos, total += 1; if (block_tags(_, tag), stats:str(_) += 1 ) );
    tally(stats, total);
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
);

dpad(num, wid) ->
(
   strn = str(num);
   if (length(strn) < wid, strn = '...'*(wid-length(strn))+strn);
   strn;
)

