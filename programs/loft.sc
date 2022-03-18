__config() -> {
    'stay_loaded'->true,
    'commands'->{
        'wand'->['wand'],
        'paste <Block> <replace>'->['paste'],
        'paste <Block>'->['paste',null],
        'paste'->['paste','diamond_block',null],
        'undo'->['undo']
    },
    'arguments'->{
        'Block'->{'type'->'term', 'suggest'->[
			'white_concrete','yellow_concrete','orange_concrete','purple_concrete',
			'lime_concrete','red_concrete','green_concrete','magenta_concrete',
			'brown_concrete','gray_concrete','light_gray_concrete','blue_concrete',
			'light_blue_concrete','pink_concrete','cyan_concrete','black_concrete'
			]
		},
        'replace'->{'type'->'term','suggest'->['air','water','lava']}
    }
};

global_tool='phantom_membrane';
global_block='diamond_block';
global_replace=null;
global_sets=[]; //array containing current information about the m splines created
global_points=[]; //vector of maximum n points of the current spline that is being created by the player
global_cset=[]; //grid of nxm points created based on global_sets by the function _normalise_set()
global_a=[]; //grid of a coefficients needed for bezier surfaces
global_b=[]; //grid of b coefficients needed for bezier surfaces
global_c=[]; //grid of c coefficients needed for bezier surfaces
global_affected_block=[]; //vector containing information about block that the script changed in the last paste operation
global_history=[]; //vector containing information about block that the script changed in the last N paste operation

wand()->(
    print('the wand is:');
    print(global_tool);
);

__on_player_breaks_block(player, block)->( //starts a new spline
    item_tuple = query(player,'holds',hand='mainhand'):0;
    if(item_tuple==global_tool,
        schedule(0, _(outer(block)) -> set(pos(block), block));
        _draw_surf(1);
        global_points=[];
        global_points+=pos(block);
        global_sets:(length(global_sets))=global_points;
        _normalise_set();
        _draw_surf(10000)
    );
    if(item_tuple!=global_tool,
        if(global_sets!=[],
            _draw_surf(1);
            print('operation cancelled');
            global_sets=[];
            global_cset=[];
            global_points=[]
        )
    )
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec)->( //expands the spline adding new points
    if((get(item_tuple,0)==global_tool)&&(hand=='mainhand')&&(global_points!=[]),
        _draw_surf(1);
        global_points+=pos(block);
        delete(global_sets,-1);
        global_sets:(length(global_sets))=global_points;
        _normalise_set();
        _draw_surf(10000)
    );
    if(((get(item_tuple,0)!=global_tool)&&(hand=='mainhand')),
        if(global_sets!=[],
            _draw_surf(1);
            print('operation cancelled');
            global_sets=[];
            global_cset=[];
            global_points=[]
        )
    )
);

//this function transform the global_sets in a rectangoular grid (m*n) creating the loft using the created splines
//it defines global_a, global_b and global_c array of coefficient points needed for the defination of the surfaces
_normalise_set()->( 
    global_cset=[];
    m=length(global_sets); //m is the number of splines contained in global_sets
    n=length(global_sets:0); //n will be the max number of points found in one of the splines
    c_for(i=1,i<m,i=i+1, 
        if(length(global_sets:i)>n,
            n=length(global_sets:i)
        )
    );
    if(n<=1, //in case each spline is composed by only one point (so not really a spline in this case)
        global_cset=global_sets;
    );
    if(n>1,
        c_for(i=0,i<m,i=i+1, //calculate new_points, a vector of n points for each splines that has less than n points
            if(n>length(global_sets:i),
                new_points=[];
                //in case spline had 1 point
                if(1==length(global_sets:i), 
                    c_for(j=0,j<n,j=j+1,
                        new_points+=global_sets:i:0
                    )
                );
                //in case spline had 2 points
                if(2==length(global_sets:i),
                    c_for(j=0,j<n,j=j+1,
                        new_points+=(global_sets:i:0)+((global_sets:i:1)-(global_sets:i:0))*j/(n-1);
                    )
                );
                //in case spline had more than 2 points (this operation may distort a little the spline)
                if(2<length(global_sets:i), 
                    a=[];
                    a+=0.25*(global_sets:i:0)-0.25*(global_sets:i:2)+(global_sets:i:1);
                    c_for(j=1,j<length(global_sets:i)-1,j=j+1,
                        a+=2*(global_sets:i:j)-a:(j-1);
                    );
                    r=(length(global_sets:i)-1)/(n-1);
                    c_for(j=0,j<n-1,j=j+1,
                        t=(j*r)%1;
                        k=j*r-t;
                        new_points+=(global_sets:i:k)*(1-t)^2+2*(a:k)*t*(1-t)+(global_sets:i:(k+1))*t^2
                    );
                    new_points+=global_sets:i:(-1)
                );
                global_cset+=new_points
            );
            if(n==length(global_sets:i),
                global_cset+=global_sets:i
            )
        )
    );
    //now we have global_cset containing all the point of the surface (a grid of m*n points)
    //global_cset contains m splines each spline is composed by n points
    //then we want to calculate all the control points of the surfaces global_a, global_b and global_c
    global_a=[]; //will be a m*(n-1) grid of coefficient points
    global_b=[]; //will be a (m-1)*n grid of coefficient points
    global_c=[]; //will be a (m-1)*(n-1) grid of coefficient points
    if((n==2)&&(m==2),
        global_a=[[0.5*(global_cset:0:0)+0.5*(global_cset:0:1)],[0.5*(global_cset:1:0)+0.5*(global_cset:1:1)]];
        global_b=[[0.5*(global_cset:0:0)+0.5*(global_cset:1:0),0.5*(global_cset:0:1)+0.5*(global_cset:1:1)]];
        global_c=[[0.5*(global_a:0:0)+0.5*(global_a:1:0)]]
    );
    if((n==2)&&(m>2), //in case you have m spline each formed by only 2 points (so m lines...)
        c_for(j=0,j<m,j=j+1,
            global_a+=[0.5*(global_cset:j:0)+0.5*(global_cset:j:1)]
        );
        global_b+=[(global_cset:1:0)+0.25*(global_cset:0:0)-0.25*(global_cset:2:0),
                   (global_cset:1:1)+0.25*(global_cset:0:1)-0.25*(global_cset:2:1)];
        global_b:0:1=(global_cset:1:1)+0.25*(global_cset:0:1)-0.25*(global_cset:2:1);
        c_for(j=1,j<m-1,j=j+1,
            global_b+=[2*(global_cset:j:0)-(global_b:(j-1):0),2*(global_cset:j:1)-(global_b:(j-1):1)];
        );
        c_for(j=0,j<m-1,j=j+1,
            global_c+=[0.5*(global_b:j:0)+0.5*(global_b:j:1)]
        )
    );
    if((m==2)&&(n>2), //in case you have 2 splines each composed by more than 2 points
        a0=[(global_cset:0:1)+0.25*(global_cset:0:0)-0.25*(global_cset:0:2)];
        a1=[(global_cset:1:1)+0.25*(global_cset:1:0)-0.25*(global_cset:1:2)];
        c_for(i=1,i<n-1,i=i+1,
            a0+=2*(global_cset:0:i)-(a0:(i-1));
            a1+=2*(global_cset:1:i)-(a1:(i-1))
        );
        global_a+=a0;
        global_a+=a1;
        c_for(i=0,i<n,i=i+1,
            global_b+=0.5*(global_cset:0:i)+0.5*(global_cset:1:i);
        );
        global_b=[global_b];
        c_for(i=0,i<n-1,i=i+1,
            global_c+=0.5*(global_a:0:i)+0.5*(global_a:1:i)
        );
        global_c=[global_c]
    );
    if((m>2)&&(n>2), //in case you have more than 2 splines composed by more than 2 points each
        c_for(j=0,j<m,j=j+1,
            a=[(global_cset:j:1)+0.25*(global_cset:j:0)-0.25*(global_cset:j:2)];
            c_for(i=1,i<n-1,i=i+1,
                a+=2*(global_cset:j:i)-(a:(i-1))
            );
            global_a+=a;
        );
        b=[];
        c_for(i=0,i<n,i=i+1,
            b+=(global_cset:1:i)+0.25*(global_cset:0:i)-0.25*(global_cset:2:i)
        );
        global_b+=b;
        c_for(j=1,j<m-1,j=j+1,
            b=[];
            c_for(i=0,i<n,i=i+1,
                b+=2*(global_cset:j:i)-(global_b:(j-1):i)
            );
            global_b+=b
        );
        c_for(j=0,j<m-1,j=j+1,
            c=[((global_b:j:1)+0.25*(global_b:j:0)-0.25*(global_b:j:2))];
            c_for(i=1,i<n-1,i=i+1,
                c+=2*(global_b:j:i)-(c:(i-1))
            );
            global_c+=c
        )
    )
);

_draw_spline(points,number_of_tick)->( //a function used to render a spline given it's vector of points
    if(length(points)==1, //in case the spline has only one point (not a spline obviously, just a point)
        p1=points:0;
        draw_shape('sphere',number_of_tick,{'center'->p1+0.5,'radius'->1})
    );
    if(length(points)==2, //in case the spline has only 2 points (a rect line)
        p1=points:0;
        p2=points:1;
        draw_shape('line',number_of_tick,{'from'->p1+0.5,'to'->p2+0.5})
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
                    draw_shape('line',number_of_tick,{'from'->p1+0.5,'to'->p2+0.5})
                );
				p1=p2;
			);
			draw_shape('line',number_of_tick,{'from'->p1+0.5,'to'->get(points,i+1)+0.5})
		)
    )
);

_draw_surf(number_of_tick)->( //function used to render the surface created
    m=length(global_cset);
    n=length(global_cset:0);
    if((m==1)||(n==1),
        _draw_spline(global_points,number_of_tick)
    );
    if((m>1)&&(n>1),
        c_for(j=0,j<m,j=j+1,
            points=global_cset:j;
            _draw_spline(points,number_of_tick)
        );
        c_for(i=0,i<n,i=i+1,
            points=[];
            c_for(j=0,j<m,j=j+1,
                points+=global_cset:j:i;
            );
            _draw_spline(points,number_of_tick);
        )
    )
);

_set_block(pos)->( //function for setting block and storing their preavious value in global_affected_block
    if(block(pos)!=global_block,
        if((block(pos)==global_replace)||(global_replace==null),
            existing_block=block(pos);
	        global_affected_block+=[pos,existing_block];
            set(pos,global_block)
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
    global_block=Block;
    global_replace=replace;
    m=length(global_cset);
    n=length(global_cset:0);
    if((n>1)&&(m>1),
        c_for(i=0,i<n-1,i=i+1,
            game_tick(10);
            c_for(j=0,j<m-1,j=j+1,
                _paste_patch(
                    global_cset:j:i,
                    global_cset:j:(i+1),
                    global_cset:(j+1):i,
                    global_cset:(j+1):(i+1),
                    global_a:j:i,
                    global_a:(j+1):i,
                    global_b:j:i,
                    global_b:j:(i+1),
                    global_c:j:i
                )
            )
        );
        schedule(1,'_add_to_history');
        _draw_surf(1);
        global_sets=[];
        global_cset=[];
        global_points=[]
    )
);

_add_to_history()->(
	if(length(global_affected_block)==0,return);
	operation={'affected_position'->global_affected_block};
	global_history+=operation;
	global_affected_block=[];
    print('done');
);

undo()->(
	if(length(global_history)==0,print('no mooves to undo');return);
	operation=global_history:(length(global_history)-1);
	affected_block=operation:'affected_position';
	c_for(i=0,i<length(affected_block),i=i+1,
		set(affected_block:i:0,affected_block:i:1)
	);
	delete(global_history,length(global_history)-1)
);
