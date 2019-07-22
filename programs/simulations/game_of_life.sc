game_of_life_init(center_x, center_y, center_z, size, filler_block, tick_interval) ->
(
   //Game of life initializer script.
   //Makes sure there is space for the setup and space is clear.
   //Sets up the scene, and saves positions in global.
   
   //First we remove all the potential decimal bits from args making sure they are numbers
   l(center_x, center_y, center_z, size, interval) = map(l(center_x, center_y, center_z, size, interval), floor(_));
   if ( center_y < 1.5*size+5 || center_y > 255-size/2-5,
       exit(str('Your specified center point at Y%d and size of %d means it won\'t fit in world limits',center_y, size))
   );
   //Who builds even in Minecraft?
   if ( size % 2 == 0,
       exit(str('Pls choose an odd size of the game of life arena'))
   );
   if ( opacity(block(filler_block)) != 0,
       print('Warning: You have selected a block '+filler_block+' that is not 100% transparent.\nThis may cause massive lag.')
   );
   radius = floor(size/2);
   global_gol_location= l(center_x, center_y, center_z);
   global_gol_radius = radius;
   global_gol_tick = tick_interval;
   global_gol_block = block(filler_block);
   scan(center_x, center_y, center_z, radius+1, 3*radius+3, radius+1, radius+1, radius+1, radius+1, set(_, block('air')));
   scan(center_x, center_y-radius-2, center_z, radius+1, 2*radius+2, radius+1, radius+1, 0, radius+1, set(_,'white_concrete'));   
   scan(center_x, center_y-radius-1, center_z, radius+1, 0, radius+1, set(_,'black_concrete'));
   scan(center_x, center_y-radius-1, center_z, radius  , 0, radius  , set(_,'air'));
   for( l(l(-1,-1),l(-1,1),l(1,-1),l(1,1)),
       l(dx, dz) = _;
       scan(center_x+dx*(radius+1),center_y+radius+1,center_z+dz*(radius+1), 
            0,2*radius+1,0, 0,0,0,
            set(_, block('end_rod[facing=up]')));
       volume(center_x+dx*(radius+1),center_y+radius+1,center_z+dz*(radius+1), 
              center_x,              center_y+radius+1,center_z+dz*(radius+1),
              set(_, block('end_rod[facing='+if(dx<0,'east','west')+']')));
       volume(center_x+dx*(radius+1),center_y+radius+1,center_z+dz*(radius+1), 
              center_x+dx*(radius+1),center_y+radius+1,center_z,
              set(_, block('end_rod[facing='+if(dz<0,'south','north')+']')));
       set(center_x+dx*(radius+1),center_y+radius+1,center_z+dz*(radius+1),block('conduit[waterlogged=false]'));
       set(center_x              ,center_y+radius+1,center_z+dz*(radius+1),block('conduit[waterlogged=false]'));
       set(center_x+dx*(radius+1),center_y+radius+1,center_z              ,block('conduit[waterlogged=false]'))
   )
);

_gol_tick() -> 
(
   if(!(global_gol_radius>0), exit('Game of Life arena not initialized'));
   if ( not(global_gol_tick > 1) || tick_time()%global_gol_tick == 0,
       l(cx, cy, cz) = global_gol_location;
       offset = 2*global_gol_radius+3;
       scan(cx, cy, cz, global_gol_radius, global_gol_radius, global_gol_radius,
           nb = for(neighbours(_),_==global_gol_block);
           set(_x, _y-offset,_z,  if(nb==3, global_gol_block, nb==2, _, block('air')) )
       );
       scan(cx, cy-offset, cz, global_gol_radius, global_gol_radius, global_gol_radius, 
           if(_!=block(_x,_y+offset,_z), set(_x,_y+offset,_z,_))
       )
   )
)
