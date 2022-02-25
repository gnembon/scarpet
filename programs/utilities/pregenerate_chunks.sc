// by gnembon

__config() -> {
    'commands' -> {
        '' -> _() -> print('type \'/'+system_info('app_name')+' from <from_pos> to <to_pos>\' to start'),
        'from <from_pos> to <to_pos>' -> ['pregenerate', false],
        'from <from_pos> to <to_pos> aggressive' -> ['pregenerate', true],
        'abort' -> 'abort',
        'clear' -> 'clear'
    }
};

global_chunks_to_go = [];
global_title = 'Chunk Generation Progress';
global_aggressive = false;

pregenerate(from_pos, to_pos, aggressive) ->
(
   if (global_chunks_to_go,
       print(format('w There is still a process running, click ', 'yb here ', '!/'+system_info('app_name')+' abort', 'w to abort it'));
       return();
   );
   global_aggressive = aggressive;

   global_chunks_to_go = [];
   from_chpos = from_pos / 16;
   to_chpos = to_pos / 16;
   min_chpos = [min(from_chpos:0, to_chpos:0), min(from_chpos:2, to_chpos:2)];
   max_chpos = [max(from_chpos:0, to_chpos:0), max(from_chpos:2, to_chpos:2)];

   for ( range(min_chpos:0, max_chpos:0), chx = _;
      for( range(min_chpos:1, max_chpos:1), chz = _;
         global_chunks_to_go += l(chx, chz)
      );
   );
   if (length(global_chunks_to_go) > 50000,
      print(format('gi Calculating chunks ...'));
   );
   // do diamond shape

   global_chunks_to_go = sort_key(global_chunks_to_go, rx = floor((_:0)/32); rz = floor((_:1)/32); -10000*rx-rz-(floor(abs(_:0))+floor(abs(_:1)))/100000);
   scoreboard_remove(global_title);
   scoreboard_add(global_title);
   scoreboard_display('sidebar', global_title);
   scoreboard(global_title, 'total chunks in the area', length(global_chunks_to_go));
   __step();
   print(format('w Pregenerating started, click ', 'yb here ', '!/'+system_info('app_name')+' abort', 'w to abort it'));
);

abort() ->
(
   global_chunks_to_go = [];
   print('Aborted');
);

clear() ->
(
   if (global_chunks_to_go,
       print(format('w There is still a process running, click ', 'yb here ', '!/'+system_info('app_name')+' abort', 'w to abort it'));
       return();
   );
   scoreboard_remove(global_title);
);

__incstat(what) -> scoreboard(global_title, what, scoreboard(global_title, what)+1);

__step() ->
(
   if (!global_chunks_to_go,
      return();
   );
   if ( system_info('server_last_tick_times'):0 > 60,
      schedule(1, '__step');
      return();
   );

   start = time();
   chunks_this_tick = 0;
   aggro = global_aggressive;
   while(global_chunks_to_go && (chunks_this_tick < 2 || time() < start+40), 1000,
      [chx, chz] = global_chunks_to_go:(-1);
      delete(global_chunks_to_go:(-1));
      // do stuff with the chunk
      chunk_pos = l(16*chx+8, 128, 16*chz+8);
      if (generation_status(chunk_pos, true) != 'full', // full shoudl suffice, but 'spawn' happens when light is removed
         // this will generate the chunk to full
         if (aggro,
            add_chunk_ticket(chunk_pos, 'portal', 1)
         ,
            __incstat('newly generated');
         );
         str(block(chunk_pos));

      , !aggro ,
         __incstat('already present');
      );
      __incstat('total processed');
      chunks_this_tick += 1;
      scoreboard(global_title, 'region x', floor(chx/32));
      scoreboard(global_title, 'region z', floor(chz/32));
   );
   if ( !global_chunks_to_go,
      print(format('w Done, click ', 'eb here ', '!/'+system_info('app_name')+' clear', 'w to clear sidebar'));
   );
   //scoreboard(global_title, 'chunks/tick', chunks_this_tick);
   schedule(1, '__step');
);