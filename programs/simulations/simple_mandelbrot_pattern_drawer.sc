mandelbrot_full(x1,y1,z1,size) -> mandelbrot_segment(x1,y1,z1,size, 0, 0, 2, true);

mandelbrot_segment(x1, y1, z1, size, center_x, center_z, radius, do_log) ->
(
    palette = map(l('white','yellow','orange','red','magenta','purple','blue','black'),
            block(_+'_concrete'));
    minx = x1-size;
    maxx = x1+size;
    minz = z1-size;
    maxz = z1+size;
    xsize = maxx-minx;
    zsize = maxz-minz;
    a_min = center_x-radius;
    b_min = center_z-radius;
    img_length = 2*radius;
    
    loop( xsize,
        xpos = _ + minx;
        a0 = a_min+(_/xsize)*img_length;
        loop( zsize,
            zpos = _ + minz;
            b0 = b_min+(_/zsize)*img_length;
            ops = mandelbrot(a0, b0, 254);
            ops = if(do_log, log1p(ops) , 8*ops/255  );
            set(xpos, y1, zpos, get(palette, ops )) 
        )
    )
);

mandelbrot_segment_slow(x1, y1, z1, size, center_x, center_z, radius, do_log) ->
(
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
    
    loop( xsize,
        xpos = _ + minx;
        a0 = a_min+(_/xsize)*img_length;
        loop( zsize,
            zpos = _ + minz;
            b0 = b_min+(_/zsize)*img_length;
            l(a, b) = l(0.0, 0.0);
            ops = while( a*a+b*b <= 4, 255, 
                a_temp = a*a-b*b+a0;
                b = 2*a*b+b0;
                a = a_temp;
                _
            );
            ops = if(do_log, log1p(ops) , 8*ops/255  );
            set(xpos, y1, zpos, get(palette, ops )) 
        )
    )
)