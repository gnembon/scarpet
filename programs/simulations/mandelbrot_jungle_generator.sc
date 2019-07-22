
clear_area(center_x,center_y,center_z,radius) -> 
    scan(center_x, 0, center_z, radius, 0, radius,
        yy = min(100,top('terrain',_x,0,_z));
        loop(yy+1,
            set(_x,yy-_,_z,'air')
        )
    );

mandelbrot_hills(xc, yc, zc, size, center_x, center_z, zoom, zero_point) -> 
(
    minx = xc-size;
    maxx = xc+size;
    minz = zc-size;
    maxz = zc+size;
    xsize = maxx-minx;
    zsize = maxz-minz;
    a_min = center_x-zoom;
    b_min = center_z-zoom;
    img_length = 2*zoom;
    print('drawing mandelbrot size '+xsize+' x '+zsize);
    l(maxmb, minmb) = l(0,10000);
    loop( xsize,
        game_tick();
        print(_+'/'+xsize);
        xpos = _ + minx;
        a0 = a_min+(_/xsize)*img_length; //scaled to lie in the Mandelbrot X scale (-2.5, 1)
        loop( zsize,
            zpos = _ + minz;
            b0 = b_min+(_/zsize)*img_length;
            mbrot = mandelbrot(a0, b0, 10000)-zero_point;
            minmb = min(mbrot, minmb);
            maxmb = max(mbrot, maxmb);
            ops = log(mbrot);
            loop(ops, set(xpos, _ , zpos, 'grass_block'))
        )
    );
    'values from '+minmb+' to '+maxmb
);

smooth_area(xc,yc,zc,size) ->
(
    scan(xc, yc, zc, size-1, 0, size-1,
        ypos = top('terrain', _x, 0, _z)-1;
        if ( ypos > 0,
            bl = block(_x, ypos, _z);
            if(air(_x, ypos-1, _z-1), volume(_x, 0, _z-1,_x, ypos-1, _z-1, set(_,bl)); total+=1);
            if(air(_x, ypos-1, _z+1), volume(_x, 0, _z+1,_x, ypos-1, _z+1, set(_,bl)); total+=1);
            if(air(_x-1, ypos-1, _z), volume(_x-1, 0, _z,_x-1, ypos-1, _z, set(_,bl)); total+=1);
            if(air(_x+1, ypos-1, _z), volume(_x+1, 0, _z,_x+1, ypos-1, _z, set(_,bl)); total+=1)
        )
    );
    total
);

smooth_peaks(xc,yc,zc,size) ->
(
    scan(xc, yc, zc, size, 0, size,
        ypos = top('terrain', _x, 0, _z)-1;
        if(     !block(_x, ypos, _z-1) && !block(_x-1, ypos, _z) && 
                !block(_x, ypos, _z+1) && !block(_x+1, ypos, _z),
            set(_x, ypos, _z, 'air'); 
            total = total+1
        )
    );  total
);    

smooth_holes(xc,yc,zc,size) -> 
(
    scan(xc, yc, zc, size, 0, size,
        ypos = top('terrain', _x, 0, _z);
        if(     block(_x, ypos, _z-1) && block(_x-1, ypos, _z) && 
                block(_x, ypos, _z+1) && block(_x+1, ypos, _z),
            set(_x, ypos, _z, 'grass_block'); 
            total = total+1
        )
    ); total
);    

top_block(xc,yc,zc,size) ->
(
    scan(xc, yc, zc, size, 0, size,
        topY = max(topY, top('terrain', _x, 0, _z))
    );
    topY
);    

spread_jungle(xc,yc,zc,size, tries, top_y_block) ->
(
    loop(tries,
        if (_%100==0, game_tick(); print(_));
        randx = round(xc-size+rand(2*size));
        randz = round(zc-size+rand(2*size));
        top_air_y = top('terrain', randx, 0, randz);
        top_distance = top_y_block-top_air_y;
        if (top_distance<1 && !rand(3), 
                plop(randx,top_air_y, randz, 'jungle_large'),
            top_distance<3 && !rand(3),
                plop(randx,top_air_y, randz, 'oak_large'),
            top_distance<5 && !rand(4),
                plop(randx,top_air_y, randz, 'jungle'),
            top_distance<7 && !rand(3),
                plop(randx,top_air_y, randz, 'shrub'),
            top_distance<7 && !rand(30),
                plop(randx,top_air_y, randz, 'melon'),
            !rand(15),
                plop(randx,top_air_y, randz, 'grass'),
            !rand(50),
                plop(randx,top_air_y, randz, 'fern'),
            !rand(200),
                plop(randx,top_air_y, randz, 'lake'),
            !rand(20),
                plop(randx,top_air_y, randz, 'sugarcane'),
            !rand(20),
                plop(randx,top_air_y, randz, 'lilypad'),
            !rand(100),
                plop(randx,top_air_y, randz, 'shrub_acacia'),
            !rand(10000),
                plop(randx,top_air_y, randz, 'jungle_temple'),
        )
    )
);

// /script invokepoint clear_area 0 0 0 500
// /script invokepoint mandelbrot_hills 0 0 0 500 -0.74515346  0.11259498  0.005 26
// /script invokepoint smooth_area 0 0 0 500
// /script run smooth_peaks(0,0,0,500); game_tick(50); smooth_holes(0,0,0,500)
// /script invokepoint smooth_peaks 0 0 0 500
// /script invokepoint smooth_holes 0 0 0 500
// /script invokepoint top_block 0 0 0 500
// /script invokepoint spread_jungle 0 0 0 500 100000 13

