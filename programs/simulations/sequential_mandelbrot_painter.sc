mandelbrot_whole(x1,y1,z1,size) -> mandelbrot_segment(x1,y1,z1,size, 0, 0, 2, true);

mandelbrot_segment(x1, y1, z1, size, center_x, center_z, radius, do_log) -> 
 mandelbrot_segment_offset(x1, y1, z1,size, center_x, center_z, radius, do_log, 2*size, 0);

mandelbrot_segment_offset(x1,y1,z1, size, center_x, center_z, radius, do_log, xsteps, xstart) ->
(
   //radius : radius of the draw area
   //do_log : log coloring
   palette = map(l('white','yellow','orange','red','magenta','purple','blue','black'),
           block(_+'_concrete_powder'));
   minx = x1-size;
   maxx = x1+size;
   minz = z1-size;
   maxz = z1+size;
   xsize = maxx-minx;
   zsize = maxz-minz;
   a_min = center_x-radius;
   b_min = center_z-radius;
   img_length = 2*radius;
   print('drawing mandelbrot size '+xsize+' x '+zsize);
   
   loop( xsteps,
       game_tick();
       iter = _ + xstart;
       print(number(iter+1)+'/'+xsize);
       xpos = iter + minx;
       if(xpos >= maxx ,  
           print('job done already!'); 
           return(1)
       );
       a0 = a_min+(iter/xsize)*img_length; //scaled to lie in the Mandelbrot X scale (-2.5, 1)
       loop( zsize,
           zpos = _ + minz;
           b0 = b_min+(_/zsize)*img_length;
           ops = mandelbrot(a0, b0, 254);
           ops = if(do_log, log1p(ops) , 8*ops/255  );
           set(xpos, y1, zpos, get(palette, ops )) 
       )
   )
);

// /script in sequential_mandelbrot_painter invokepoint mandelbrot_segment_offset x y z 100 -0.74515346 0.11259498 0.007 false 50 0
// Obviously you would normally do that for much larger area, like 0 1 0 10000 ....
