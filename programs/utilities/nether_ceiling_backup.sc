// by gnembon


__command() -> '
utility meant to store and load what\'s 
above the nether ceiling.

You can use it to store your above-nether builds
then wipe the nether dimension, and restore them
after with this app

You can also transplant builds from one world to another.

Utility is meant to only copy blocks. 

Entities will not be copied using this tool. Feel free to add
entities if you want to.

Call backup_chunks to save what\'s above the nether
 - stand around 0,0 for that operation to perform best
 - specify how far away from 0,0 you are 
   expecting stored chunks to be
 - This may take a long time.
call restore_chunks to print them into the world

You can throw away the app data after that, unless
you want to load it into another world.

';


global_chunks_to_go = l();
global_chunks_to_check = l();
global_chunks_to_save = l();

backup_chunks(radius) ->
(
   global_chunks_to_go = l();
   global_chunks_to_save = l();
   chunk_range = ceil(radius/16);
   for ( range(-chunk_range, chunk_range), chx = _;
      for( range(-chunk_range, chunk_range), chz = _;
         global_chunks_to_go += l(chx, chz)
      );
   );
   scoreboard_remove('Save Progress');
   scoreboard_add('Save Progress');
   scoreboard_display('sidebar', 'Save Progress');
   scoreboard('Save Progress', 'total chunks in the area', length(global_chunks_to_go));
   __prepare_step();
   print(format('w Saving started, click ', 'yb here ', '!/nether_ceiling_backup abort', 'w to abort it'));
   '';
);

restore_chunks() ->
(
   global_chunks_to_save = l();
   global_chunks_to_save = parse_nbt(read_file('chunk/index', 'nbt')):'ChunkIndex';
   global_chunks_to_save = map(range(length(global_chunks_to_save)-1, -1, -1), global_chunks_to_save:_ );
   print('Restoring '+length(global_chunks_to_save)+ ' chunks');
   scoreboard_remove('Restore Progress');
   scoreboard_add('Restore Progress');
   scoreboard_display('sidebar', 'Restore Progress');
   scoreboard('Restore Progress', 'total chunks to restore', length(global_chunks_to_save));
   __restore_step();
   print(format('w Restoring '+length(global_chunks_to_save)+ ' chunks, click ', 'yb here ', '!/nether_ceiling_backup abort', 'w to abort it'));
   '';
);

abort() ->
(
   global_chunks_to_go = l();
   global_chunks_to_save = l();
   global_chunks_to_check = l();
   print('Aborted');
   '';
);

__incstat(what) -> scoreboard('Save Progress', what, scoreboard('Save Progress', what)+1);

global_eligible_chunks = m('features', 'light', 'spawn', 'full');

__prepare_step() ->
(
   if (!global_chunks_to_go,
      print('Listing eligible chunks finished');
      __sort_save_chunks();
      schedule(100, '__save_step');
      return();
   );

   start = time();
   chunks_this_tick = 0;
   while(global_chunks_to_go && time() < start+20, 1000,
      l(chx, chz) = global_chunks_to_go:(-1);
      delete(global_chunks_to_go:(-1));
      // do stuff with the chunk
      chunk_start = l(16*chx, 128, 16*chz);
      if (has(global_eligible_chunks, generation_status(chunk_start, true)), // full shoudl suffice, but 'spawn' happens when light is removed
         global_chunks_to_check += l(chx, chz);
         __incstat('candidates - visited chunks');
      ,
         __incstat('candidates - outside map');
      );
      __incstat('candidates - total');
      chunks_this_tick += 1;
   );
   scoreboard('Save Progress', 'per tick', chunks_this_tick);
   schedule(1, '__prepare_step');
);

__sort_save_chunks() ->
(
   neighbours = l( l(1, 0), l(0, 1), l(-1, 0), l(0, -1));
   check_set = m();
   for ( global_chunks_to_check, check_set += _ );
   ordered_chunks = l( l(0,0) );
   i = 0;
   while( check_set, length(check_set),
      if (!has(ordered_chunks, i), break());
      current_chunk = ordered_chunks:i;
      i+=1;
      for ( neighbours,
         nb = current_chunk+_;
         if (has(check_set, nb),
            ordered_chunks+= nb;
            delete(check_set, nb);
         );
      );      
   );
   print(str('%d chunks in connected areas, %d in disconnected areas', length(ordered_chunks), length(check_set)));
   for (check_set, ordered_chunks += _);
   global_chunks_to_check = l();
   for(range(length(ordered_chunks)-1, -1, -1), global_chunks_to_check += ordered_chunks:_ );
);

__save_step() ->
(
   if (!global_chunks_to_check,
      print(format(
         'w Saving finished, click ', 
         'rb here ', '!/script run scoreboard_remove(\'Save Progress\'); \'\'', 
         'w to clear progress report')
      );
      tag = nbt('{}');
      for (global_chunks_to_save, put(tag, 'ChunkIndex', _, -1 ));
      write_file('chunk/index', 'nbt', tag);
      return();
   );

   start = time();
   chunks_this_tick = 0;
   while(global_chunks_to_check && time() < start+20, 1000,
      l(chx, chz) = global_chunks_to_check:(-1);
      delete(global_chunks_to_check:(-1));
      // do stuff with the chunk
      if (__needs_saving(chx, chz), 
         __save_chunk(chx, chz);
         __incstat('processed - saved');
         global_chunks_to_save += l(chx, chz);
      ,
         __incstat('processed - nothing to save');
      ); 
      __incstat('processed - total');
      chunks_this_tick += 1;
   );
   scoreboard('Save Progress', 'per tick', chunks_this_tick);
   schedule(1, '__save_step');
);

__restore_step() ->
(
   if (!global_chunks_to_save,
      print(format(
         'w Restore finished, click ', 
         'rb here ', '!/script run scoreboard_remove(\'Restore Progress\'); \'\'', 
         'w to clear progress report')
      );
      return();
   );
  
   start = time();
   chunks_this_tick = 0;
   while(global_chunks_to_save && time() < start+30, 1000,
      l(chx, chz) = global_chunks_to_save:(-1);
      delete(global_chunks_to_save:(-1));
      __restore_chunk(chx, chz);
      scoreboard('Restore Progress', 'total restored', scoreboard('Restore Progress', 'total restored')+1);
      chunks_this_tick += 1;
   );
   scoreboard('Restore Progress', 'per tick', chunks_this_tick);
   schedule(1, '__restore_step');
);


__needs_saving(chx, chz) ->
(
   chunk_start = l(16*chx, 128, 16*chz);
   has_stuff = false;
   volume(chunk_start, chunk_start+l(15,0,15), if (!has_stuff,
      topy = top('surface', _);
      if(topy > 129 || (topy == 129 && !(block(_)~'_mushroom')), has_stuff = true);
   ));
   has_stuff;
);

__save_chunk(chx, chz) ->
(
   chunk_start = l(16*chx, 128, 16*chz);
   tag = nbt('{}');
   volume(chunk_start, chunk_start+l(15,128,15), if (!air(_),
      cur_block = _;
      block_tag = nbt('{}');
      block_tag:'pos' = pos(cur_block);
      block_tag:'block' = str(cur_block);
      properties = block_properties(cur_block);
      for(properties,
         block_tag:('prop.'+_) = '"'+property(cur_block, _)+'"';
      );
      if( (d = block_data(cur_block)),   
         block_tag:'data' = d;
      ); 
      put(tag, 'blocks', block_tag, -1);
   ));
   write_file(str('chunk/ch%d_%d', chx, chz) , 'nbt', tag);
);

__restore_chunk(chx, chz) ->
(
   blocks_tag = read_file(str('chunk/ch%d_%d', chx, chz), 'nbt');
   blocks = blocks_tag:'blocks[]';
   if (type(blocks)!='list', blocks = l(blocks) );
   without_updates( for (blocks, 
      block_data = parse_nbt(_);
      unparsed_tag = _;
      bpos = block_data:'pos';
      bname = block_data:'block';
      props = block_data:'prop';
      prop_list = l();
      if ( props, for (keys(props),
         prop_list += _;
         prop_list += str(props:_);
      ));
      data = null;
      if (has(block_data, 'data'),
         data = nbt(unparsed_tag:'data');
      );
      set(bpos, bname, prop_list, data);
   ));
);

