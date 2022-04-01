__config() -> {
    'stay_loaded'->true,
    'commands'->{
        'wand'->['wand'],
        'paste <Block> <replace>'->['paste'],
        'paste <Block>'->['paste',null],
        'paste'->['paste','diamond_block',null],
        'undo'->['undo'],
        'del'->['del','point'],
        'del <object>'->['del']
    },
    'arguments'->{
        'Block'->{'type'->'block'},
        'replace'->{'type'->'block'},
        'object'->{
            'type'->'term',
            'suggest'->['spline','point','all']
        }
    }
};

global_loft={
    'wand'->'phantom_membrane',
    'material'->'diamond_block',
    'replace'->null,
    'sets'->[],//array containing current information about the m splines created
    'cset'->[], //grid of nxm points created based on global_sets by the function _normalise_set()
    'a'->[], //grid of a coefficients needed for bezier surfaces
    'b'->[], //grid of b coefficients needed for bezier surfaces
    'c'->[], //grid of c coefficients needed for bezier surfaces
    'cursor'->0,
    'history'->[], //vector containing information about block that the script changed in the last N paste operation
    'draw'->false,
    'affected_blocks'->[] //vector containing information about block that the script changed in the last paste operation
};

wand()->(
    print('the wand is:');
    print(global_loft:'wand')
);

__on_player_breaks_block(player, block)->( //starts a new spline
    item_tuple = query(player,'holds',hand='mainhand'):0;
    if(item_tuple==global_loft:'wand',
        schedule(0, _(outer(block)) -> set(pos(block), block));
        //insert a new spline in position spline_cursor and moove all the following splines by one position
        if(length(global_loft:'sets')==0,

            global_loft:'sets':0=[pos(block)];
            global_loft:'cursor'=1,

            [spline_cursor,point_cursor]=_get_cursor_position();
            l=length(global_loft:'sets');
            c_for(j=l,j>spline_cursor,j=j-1,
                global_loft:'sets':j=global_loft:'sets':(j-1)
            );
            global_loft:'sets':spline_cursor=[pos(block)];
            global_loft:'cursor'=global_loft:'cursor'+2
        );
        _normalise_set();
        if(global_loft:'sets'==[[pos(block)]],
            global_loft:'draw'=true;
            _draw_surf_tick();
        )
    )
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec)->( //expands the spline adding new points
    if((item_tuple:0==global_loft:'wand')&&(hand=='mainhand')&&(global_loft:'sets'!=[]),
        [spline_cursor,point_cursor]=_get_cursor_position();
        if(point_cursor!=0,
            spline_cursor=spline_cursor-1
        );
        l=length(global_loft:'sets':spline_cursor);
        //insert a new control point in the selected spline at the selected position
        c_for(i=l,i>point_cursor,i=i-1,
            global_loft:'sets':spline_cursor:i=global_loft:'sets':spline_cursor:(i-1);
        );
        global_loft:'sets':spline_cursor:point_cursor=pos(block);
        global_loft:'cursor'=global_loft:'cursor'+1;
        _normalise_set();
    )
);

_switch_slot(player,slot)->(
    run('/player '+player+' hotbar '+slot);
);
__on_player_switches_slot(player, from, to)->(
    item=inventory_get(player, from):0;
    if((item=='phantom_membrane')&&(length(global_loft:'sets')>0),
        k=to-from;
        if(to-from==8,k=-1);
        if(to-from==-8,k=1);
        if(abs(k)==1,
            schedule(0,'_switch_slot',player,from+1);
            N=0;
            c_for(i=0,i<length(global_loft:'sets'),i=i+1,
                N=N+length(global_loft:'sets':i)+1
            );
            global_loft:'cursor'=(global_loft:'cursor'+k)%N
        );
    );
);

_get_cursor_position()->( //a function to match the cursor to a spline and a point of that spline
    m=global_loft:'cursor';
    spline_cursor=0;
    point_cursor=0;
    n=m;
    while(n>0,m,
        point_cursor=n;
        n=n-length(global_loft:'sets':spline_cursor)-1;
        spline_cursor=spline_cursor+1;
    );
    if(n==0,point_cursor=0);
    [spline_cursor,point_cursor]
);

//this function transform the global_sets in a rectangoular grid (m*n) creating the loft using the created splines
//it defines global_a, global_b and global_c array of coefficient points needed for the defination of the surfaces
_normalise_set()->( 
    global_loft:'cset'=[];
    m=length(global_loft:'sets'); //m is the number of splines contained in global_sets
    n=length(global_loft:'sets':0); //n will be the max number of points found in one of the splines
    c_for(i=1,i<m,i=i+1, 
        if(length(global_loft:'sets':i)>n,
            n=length(global_loft:'sets':i)
        )
    );
    if(n<=1, //in case each spline is composed by only one point (so not really a spline in this case)
        global_loft:'cset'=global_loft:'sets';
    );
    if(n>1,
        c_for(i=0,i<m,i=i+1, //calculate new_points, a vector of n points for each splines that has less than n points
            if(n>length(global_loft:'sets':i),
                new_points=[];
                //in case spline had 1 point
                if(1==length(global_loft:'sets':i), 
                    c_for(j=0,j<n,j=j+1,
                        new_points+=global_loft:'sets':i:0
                    )
                );
                //in case spline had 2 points
                if(2==length(global_loft:'sets':i),
                    c_for(j=0,j<n,j=j+1,
                        new_points+=(global_loft:'sets':i:0)+((global_loft:'sets':i:1)-(global_loft:'sets':i:0))*j/(n-1);
                    )
                );
                //in case spline had more than 2 points (this operation may distort a little the spline)
                if(2<length(global_loft:'sets':i), 
                    a=[];
                    a+=0.25*(global_loft:'sets':i:0)-0.25*(global_loft:'sets':i:2)+(global_loft:'sets':i:1);
                    c_for(j=1,j<length(global_loft:'sets':i)-1,j=j+1,
                        a+=2*(global_loft:'sets':i:j)-a:(j-1);
                    );
                    r=(length(global_loft:'sets':i)-1)/(n-1);
                    c_for(j=0,j<n-1,j=j+1,
                        t=(j*r)%1;
                        k=j*r-t;
                        new_points+=(global_loft:'sets':i:k)*(1-t)^2+2*(a:k)*t*(1-t)+(global_loft:'sets':i:(k+1))*t^2
                    );
                    new_points+=global_loft:'sets':i:(-1)
                );
                global_loft:'cset'+=new_points
            );
            if(n==length(global_loft:'sets':i),
                global_loft:'cset'+=global_loft:'sets':i
            )
        )
    );
    //now we have global_cset containing all the point of the surface (a grid of m*n points)
    //global_cset contains m splines each spline is composed by n points
    //then we want to calculate all the control points of the surfaces global_a, global_b and global_c
    global_loft:'a'=[]; //will be a m*(n-1) grid of coefficient points
    global_loft:'b'=[]; //will be a (m-1)*n grid of coefficient points
    global_loft:'c'=[]; //will be a (m-1)*(n-1) grid of coefficient points
    if((n==2)&&(m==2),
        global_loft:'a'=[[0.5*(global_loft:'cset':0:0)+0.5*(global_loft:'cset':0:1)],[0.5*(global_loft:'cset':1:0)+0.5*(global_loft:'cset':1:1)]];
        global_loft:'b'=[[0.5*(global_loft:'cset':0:0)+0.5*(global_loft:'cset':1:0),0.5*(global_loft:'cset':0:1)+0.5*(global_loft:'cset':1:1)]];
        global_loft:'c'=[[0.5*(global_loft:'a':0:0)+0.5*(global_loft:'a':1:0)]]
    );
    if((n==2)&&(m>2), //in case you have m spline each formed by only 2 points (so m lines...)
        c_for(j=0,j<m,j=j+1,
            global_loft:'a'+=[0.5*(global_loft:'cset':j:0)+0.5*(global_loft:'cset':j:1)]
        );
        global_loft:'b'+=[(global_loft:'cset':1:0)+0.25*(global_loft:'cset':0:0)-0.25*(global_loft:'cset':2:0),
                   (global_loft:'cset':1:1)+0.25*(global_loft:'cset':0:1)-0.25*(global_loft:'cset':2:1)];
        global_loft:'b':0:1=(global_loft:'cset':1:1)+0.25*(global_loft:'cset':0:1)-0.25*(global_loft:'cset':2:1);
        c_for(j=1,j<m-1,j=j+1,
            global_loft:'b'+=[2*(global_loft:'cset':j:0)-(global_loft:'b':(j-1):0),2*(global_loft:'cset':j:1)-(global_loft:'b':(j-1):1)];
        );
        c_for(j=0,j<m-1,j=j+1,
            global_loft:'c'+=[0.5*(global_loft:'b':j:0)+0.5*(global_loft:'b':j:1)]
        )
    );
    if((m==2)&&(n>2), //in case you have 2 splines each composed by more than 2 points
        a0=[(global_loft:'cset':0:1)+0.25*(global_loft:'cset':0:0)-0.25*(global_loft:'cset':0:2)];
        a1=[(global_loft:'cset':1:1)+0.25*(global_loft:'cset':1:0)-0.25*(global_loft:'cset':1:2)];
        c_for(i=1,i<n-1,i=i+1,
            a0+=2*(global_loft:'cset':0:i)-(a0:(i-1));
            a1+=2*(global_loft:'cset':1:i)-(a1:(i-1))
        );
        global_loft:'a'+=a0;
        global_loft:'a'+=a1;
        c_for(i=0,i<n,i=i+1,
            global_loft:'b'+=0.5*(global_loft:'cset':0:i)+0.5*(global_loft:'cset':1:i);
        );
        global_loft:'b'=[global_loft:'b'];
        c_for(i=0,i<n-1,i=i+1,
            global_loft:'c'+=0.5*(global_loft:'a':0:i)+0.5*(global_loft:'a':1:i)
        );
        global_loft:'c'=[global_loft:'c']
    );
    if((m>2)&&(n>2), //in case you have more than 2 splines composed by more than 2 points each
        c_for(j=0,j<m,j=j+1,
            a=[(global_loft:'cset':j:1)+0.25*(global_loft:'cset':j:0)-0.25*(global_loft:'cset':j:2)];
            c_for(i=1,i<n-1,i=i+1,
                a+=2*(global_loft:'cset':j:i)-(a:(i-1))
            );
            global_loft:'a'+=a;
        );
        b=[];
        c_for(i=0,i<n,i=i+1,
            b+=(global_loft:'cset':1:i)+0.25*(global_loft:'cset':0:i)-0.25*(global_loft:'cset':2:i)
        );
        global_loft:'b'+=b;
        c_for(j=1,j<m-1,j=j+1,
            b=[];
            c_for(i=0,i<n,i=i+1,
                b+=2*(global_loft:'cset':j:i)-(global_loft:'b':(j-1):i)
            );
            global_loft:'b'+=b
        );
        c_for(j=0,j<m-1,j=j+1,
            c=[((global_loft:'b':j:1)+0.25*(global_loft:'b':j:0)-0.25*(global_loft:'b':j:2))];
            c_for(i=1,i<n-1,i=i+1,
                c+=2*(global_loft:'b':j:i)-(c:(i-1))
            );
            global_loft:'c'+=c
        )
    )
);

_draw_spline(points,number_of_tick,color)->( //a function used to render a spline given it's vector of points
    if(length(points)==1, //in case the spline has only one point (not a spline obviously, just a point)
        p1=points:0;
        draw_shape('sphere',number_of_tick,{'center'->p1+0.5,'radius'->1,'color'->color})
    );
    if(length(points)==2, //in case the spline has only 2 points (a straight line)
        p1=points:0;
        p2=points:1;
        draw_shape('line',number_of_tick,{'from'->p1+0.5,'to'->p2+0.5,'color'->color})
    );
    if(length(points)>2, //in case the spline has more than 3 points
        //first, calculating the vector of coefficients of the spline
        a=[(points:1)+0.25*(points:0)-0.25*(points:2)];
        c_for(i=1,i<length(points)-1,i=i+1,
            a+=2*(points:i)-(a:(i-1));
        );
        c_for(i=0,i<length(points)-1,i=i+1, //drawing each segment of the spline (if the spline has n points, there are n-1 segments)
			p1=get(points,i);
			c_for(t=0.05,t<1,t=t+0.05,
				p2=get(points,i)*(1-t)^2+get(a,i)*2*t*(1-t)+get(points,i+1)*t^2;
                if(p1!=p2,
                    draw_shape('line',number_of_tick,{'from'->p1+0.5,'to'->p2+0.5,'color'->color})
                );
				p1=p2;
			);
			draw_shape('line',number_of_tick,{'from'->p1+0.5,'to'->get(points,i+1)+0.5,'color'->color})
		)
    )
);

_draw_surf(number_of_tick)->( //function used to render the surface created
    if(length(global_loft:'sets'==0),return);
    if(length(global_loft:'cset'==0),return);
    m=length(global_loft:'cset');
    n=length(global_loft:'cset':0);
    c_for(j=0,j<m,j=j+1,
        points=global_loft:'cset':j;
        _draw_spline(points,number_of_tick,0x10101080)
    );
    c_for(i=0,i<n,i=i+1,
        points=[];
        if(m>1,
            c_for(j=0,j<m,j=j+1,
                points+=global_loft:'cset':j:i;
            );
            _draw_spline(points,number_of_tick,0xFFFFFFFF);
        )
    );
    [s,p]=_get_cursor_position();
    if(p==0,(
            point=global_loft:'sets':s:p;
            draw_shape('sphere',number_of_tick,{'center'->point+0.5,'radius'->1,'color'->0xCCCC00FF})
        ),(
            s=s-1;
            if(p==length(global_loft:'sets':s),
                point=global_loft:'sets':s:(p-1);
                draw_shape('sphere',number_of_tick,{'center'->point+0.5,'radius'->1,'color'->0x009999FF}),
                
                point=global_loft:'sets':s:(p-1);
                p2=global_loft:'sets':s:p;
                draw_shape('line',number_of_tick,{'from'->point+0.51,'to'->p2+0.51,'color'->0x00FFFFFF})
            )
        )     
    );
    _draw_cross(point,number_of_tick,0xFF0000FF)
);
_draw_cross(pos,number_of_tick,color)->(
    a=[1,1,1];
    b=[1,1,-1];
    c=[1,-1,-1];
    d=[1,-1,1];
    draw_shape('line',number_of_tick,{'from'->pos+0.5+a,'to'->pos+0.5-a,'color'->color});
    draw_shape('line',number_of_tick,{'from'->pos+0.5+b,'to'->pos+0.5-b,'color'->color});
    draw_shape('line',number_of_tick,{'from'->pos+0.5+c,'to'->pos+0.5-c,'color'->color});
    draw_shape('line',number_of_tick,{'from'->pos+0.5+d,'to'->pos+0.5-d,'color'->color})
);
_draw_surf_tick() -> (
    if(global_loft:'draw',
        _draw_surf(5);
        schedule(4, '_draw_surf_tick')
    )
);
_set_block(pos)->( //function for setting block and storing their preavious value in global_affected_block
    if(block(pos)!=global_loft:'material',
        if((block(pos)==global_loft:'replace')||(global_loft:'replace'==null),
            existing_block=block(pos);
	        global_loft:'affected_blocks'+=[pos,existing_block];
            set(pos,global_loft:'material')
        )
    )
);
_leng_approx(a,b,c)->( //function that approximate the length of a piece of quadratic spline given its 3 control points
    L1=0.5*(a-b);
    L2=0.5*(b-c);
    L3=0.5*(a+b)-0.5*(b+c);
    sqrt((L1:0)^2+(L1:1)^2+(L1:2)^2)+
    sqrt((L2:0)^2+(L2:1)^2+(L2:2)^2)+
    sqrt((L3:0)^2+(L3:1)^2+(L3:2)^2)
);
_paste_patch(p00,p01,p10,p11,a0,a1,b0,b1,c)->( //function used to paste a patch of the surface
    v=0;
    lvmax=max(
        _leng_approx(p00,a0,p01),
        _leng_approx(b0,c,b1),
        _leng_approx(p10,a1,p11)
    );
    lumax=max(
        _leng_approx(p00,b0,p10),
        _leng_approx(a0,c,a1),
        _leng_approx(p01,b1,p11)
    );
    if(lvmax>0, dv=0.7/lvmax, dv=1);
    if(lumax>0, du=0.7/lumax, du=1);
    while(v<=1,1000,
        u=0;
        while(u<=1,1000,
            point=(p00*(1-v)^2+2*a0*v*(1-v)+p01*v^2)*(1-u)^2+     //point of the surface s
                (2*b0*(1-v)^2+4*c*v*(1-v)+2*b1*v^2)*u*(1-u)+
                (p10*(1-v)^2+2*a1*v*(1-v)+p11*v^2)*u^2;
            p=[round(get(point,0)),round(get(point,1)),round(get(point,2))];
            schedule(0,'_set_block',p);
            if((u<1)&&(u+du>1),
                u=1,
                u=u+du
            )
        );
        if((v<1)&&(v+dv>1),
            v=1,
            v=v+dv
        )
    )
);
paste(Block,replace)->(
    global_loft:'material'=Block;
    global_loft:'replace'=replace;
    m=length(global_loft:'cset');
    n=length(global_loft:'cset':0);
    if((n>1)&&(m>1),
        c_for(i=0,i<n-1,i=i+1,
            c_for(j=0,j<m-1,j=j+1,
                _paste_patch(
                    global_loft:'cset':j:i,
                    global_loft:'cset':j:(i+1),
                    global_loft:'cset':(j+1):i,
                    global_loft:'cset':(j+1):(i+1),
                    global_loft:'a':j:i,
                    global_loft:'a':(j+1):i,
                    global_loft:'b':j:i,
                    global_loft:'b':j:(i+1),
                    global_loft:'c':j:i
                )
            )
        );
        schedule(1,'_add_to_history');
    )
);
_add_to_history()->(
	if(length(global_loft:'affected_blocks')==0,return);
	operation={'affected_position'->global_loft:'affected_blocks'};
	global_loft:'history'+=operation;
	global_loft:'affected_blocks'=[];
    print('done');
);
undo()->(
	if(length(global_loft:'history')==0,print('no mooves to undo');return);
	operation=global_loft:'history':(length(global_loft:'history')-1);
	affected_block=operation:'affected_position';
	c_for(i=0,i<length(affected_block),i=i+1,
		set(affected_block:i:0,affected_block:i:1)
	);
	delete(global_loft:'history',length(global_loft:'history')-1)
);

del(ob)->(
    if(length(global_loft:'sets')!=0,
        [s,p]=_get_cursor_position();
        if(p>0,s=s-1);
        if(ob=='spline',
            delete(global_loft:'sets':s)
        );
        if(ob=='point',
            if(length(global_loft:'sets':s)==1,
                delete(global_loft:'sets':s),
                if(p==0,
                    delete(global_loft:'sets':s:p),
                    delete(global_loft:'sets':s:(p-1))
                )
            )
        );
        if(ob=='all',
            global_loft:'sets'=[];
            global_loft:'cset'=[];
            global_loft:'a'=[];
            global_loft:'b'=[];
            global_loft:'c'=[];
            global_loft:'cursor'=0
        );
        if(global_loft:'sets'==[],
            global_loft:'draw'=false,
            _normalise_set();
        )
    )
)
