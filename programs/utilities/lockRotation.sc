///////////////////////////////////////////////////////////////////////////////
// Rotation Lock
// v1.0.2
// scarpet script written by "TernaryC"
// This description last updated 07/27/2023
// 
// This script aims to recreate the "lock rotation" feature of the Forge mod
//  "Quark", as implemented in all versions of the mod since 1.14.4
//
// This script uses no source code from the "Quark" mod, nor does is it a port
//   of that mod or it's modules.
//
// By running the command "/lockrotation", the player records their current
//   facing direction, as well as other data related to block placement.
// From then on, upon placing a block, the script will attempt to reorient the
//   block to face the recorded placement data.
//
// The recorded data can be removed by typing the command "/lockrotation clear",
//   or by locking into an identical rotation to the one already recorded (such
//   as running the command twice in a row).
//
// The command "/lockrotation show" can be used to display the current
//   recorded rotation data.
//
///////////////////////////////////////////////////////////////////////////////

__on_start() ->
(
    global_lockProfile = null;
	global_rotated = null;
);
__command() -> _requestLock();

// Front Facing
show() ->
(
    details = _getDetails();
    print(format('ig ' + details));
    return();
);
_requestLock() ->
(
    _getCurrentState();
    if (global_lockProfile == null || global_lockProfile:0 == null,
        _tooltip(format(' Rotation lock ', 'b disabled'));
    ,
        _tooltip(format(' Rotation lock ', 'b enabled'));
    );
    return();
);
_getDetails() ->
(
    if (global_lockProfile == null,
        return('[No lock set]')
    );
    details = '[Locked: ' + global_lockProfile:2;
    if (global_lockProfile:0 != global_lockProfile:2,
        details += ' (' + global_lockProfile:0 + ')';
    );
    half = _halfSide(global_lockProfile:1);
    if (half == null, half = 'no');
    details += ', ' + half + ' half]';
    return(details);
);
clear() ->
(
    global_lockProfile = null;
    _tooltip(format(' Rotation lock ', 'b disabled'));
    return();
);

// Utility
_tooltip(text, outer(p)) ->
(
    display_title(p, 'actionbar', text);
);
_sign(num) ->
(
	return(num / abs(num));
);
_getDir(yaw) ->
(
    yaw += 45;
    dir = 'south';
    if (yaw > 90,  dir = 'west');
    if (yaw > 180, dir = 'north');
    if (yaw < 0,   dir = 'east');
    if (yaw < -90, dir = 'north');
    return(dir);
);
_dirAxis(direction) ->
(
    if (_contains(['north', 'south'], direction),
        return('z');
    , _contains(['west', 'east'], direction),
        return('x');
    , 
        return('y');
    );
);
_flipDir(direction) ->
(
    dirs = ['north', 'west', 'up', 'south', 'east', 'down'];
    i = dirs ~ direction + 3;
    if (i > 5, i += -6);
    return(dirs:i);
);
_castDif(b, raycast) ->
(
	blockpos = pos(b);
	blockpos:0 += 0.5;
	blockpos:1 += 0.5;
	blockpos:2 += 0.5;
	
	difpos = [raycast:0 - blockpos:0, raycast:1 - blockpos:1, raycast:2 - blockpos:2];
	
	truepos = [];
	for (difpos, truepos:_i = ceil(abs(difpos:_i) - 0.49999) * _sign(difpos:_i));
	
	return(truepos);
);
_castSide(b, raycast) ->
(
	truepos = _castDif(b, raycast);
	
	direction = null;
	if (truepos:0 == -1, direction = 'west');
	if (truepos:0 ==  1, direction = 'east');
	if (truepos:1 == -1, direction = 'down');
	if (truepos:1 ==  1, direction = 'up');
	if (truepos:2 == -1, direction = 'north');
	if (truepos:2 ==  1, direction = 'south');
	
	return(direction);
);
_halfSide(half) ->
(
    if (half == 0, return('bottom'));
    if (half == 1, return('top'));
    return(null);
);
_contains(list, value) -> return(list ~ value != null);
_isVertible(block) ->
(
    if (_contains(block, 'shulker_box'), return(true));
    if (_contains(['end_rod', 'lightning_rod', 'grindstone'], block), return(true));
    return(false);
);
_isBustable(block) ->
(
    //Blocks that shouldn't be rotated lest they float or break
    if (_contains(block, 'torch'), return(true));
    if (_contains(block, '_bud'), return(true));
    if (_contains(block, 'button'), return(true));
    if (_contains(block, '_bed'), return(true));
    if (_contains(block, 'coral_fan'), return(true));
    if (_contains(block, 'wall_head'), return(true));
    if (_contains(['amethyst_cluster', 'bell', 'ladder', 'lever', 'pointed_dripstone', 'tripwire_hook'], block), return(true));
    return(false);
);
_2to3(coord, third) ->
(
	vector = [];
	j = 0;
	loop(3,
		if (_ == third:1,
			vector:_ = third:0;
		,
			vector:_ = coord:j;
			j += 1;
		);
	);
	return(vector);
);
_linetobox(point1, point2, xoff, yoff) ->
(
	return([point2:0 + xoff, point2:1 + yoff]);
);

// App Methods
_displayProfile(raycast, blockhit, profile) ->
(
	if (raycast == null, return());
	
	b_at = pos(blockhit);
	b_cent = [];
	for (b_at, b_cent:_i = _ + 0.5);
	b_op = [];
	for (b_at, b_op:_i = _ + 1);
	
	hitdir = _castDif(blockhit, raycast);
	sidepos = [];
	for (b_cent, sidepos:_i = _ + (hitdir:_i * 0.505));
	
	filt = -1;
	for (hitdir, if (_ != 0, filt = _i; break()));
	third = [sidepos:filt, filt];
	cent = [];
	for (sidepos, if(_i != third:1, cent += _));
	
	corner = [[null,null], [null,null], [null,null], [null,null]];
	corner:0:0 = cent:0 - 0.45;
	corner:0:1 = cent:1 - 0.45;
	corner:1:0 = cent:0 - 0.45;
	corner:1:1 = cent:1 + 0.45;
	corner:2:0 = cent:0 + 0.45;
	corner:2:1 = cent:1 + 0.45;
	corner:3:0 = cent:0 + 0.45;
	corner:3:1 = cent:1 - 0.45;
	
	if (profile:1 != -1,
		y = 0;
		if (third:1 == 2, y = 1);
		if (profile:1 == 0,
			for(corner, if(_:y > sidepos:1, corner:_i:y += -0.45));
		,
			for(corner, if(_:y < sidepos:1, corner:_i:y +=  0.45));
		);
	);
	
	d = 20;   //duration
	w = 0.15; //line thickness
	draw_shape('box', d,
		'from', _2to3(corner:0, third),
		'to',   _2to3(_linetobox(corner:0, corner:1, w, 0), third),
		'fill', 4294967295
	);
	draw_shape('box', d,
		'from', _2to3(corner:1, third),
		'to',   _2to3(_linetobox(corner:1, corner:2, 0, -w), third),
		'fill', 4294967295
	);
	draw_shape('box', d,
		'from', _2to3(corner:2, third),
		'to',   _2to3(_linetobox(corner:2, corner:3, -w, 0), third),
		'fill', 4294967295
	);
	draw_shape('box', d,
		'from', _2to3(corner:3, third),
		'to',   _2to3(_linetobox(corner:3, corner:0, 0, w), third),
		'fill', 4294967295
	);
	undef(w);
);
_getCurrentState(outer(p)) ->
(
    newProfile = [null, null, null];
    
    direction = query(p, 'facing');
    flat_dir = _getDir(query(p, 'yaw'));
    
    raycast = query(p, 'trace', 5, 'blocks', 'exact');
	rayhit  = query(p, 'trace', 5, 'blocks');
    if (raycast != null,
        hit_dir = _castSide(rayhit, raycast);
		
        half = abs(floor((raycast:1 - floor(raycast:1)) * 2));
        if (_dirAxis(direction) == 'y',
            half = -1;
        , raycast:1 < 0,
            half = 1 - half;
        );
        
		flat = _flipDir(hit_dir);
		if (_contains(['up', 'down'], flat), flat = flat_dir);
		
        newProfile = [hit_dir, half, _flipDir(flat)];
    ,
        newProfile = [direction, -1, flat_dir];
    );
    
    if (global_lockProfile != null && newProfile == global_lockProfile,
        global_lockProfile = null;
    ,
		_displayProfile(raycast, rayhit, newProfile);
        global_lockProfile = newProfile;
    );
);
_getIdealState(block) ->
(
    state = block_state(block);
    if(_isBustable(block), return(state));
    
    direction = global_lockProfile:0;
    half      = global_lockProfile:1;
    flat_dir  = global_lockProfile:2;
    if (global_lockProfile == null, return(state));
    
    if (_contains(state, 'axis'), state:'axis' = _dirAxis(direction));
    if (_contains(state, 'type'),
        //Slab Type
        if (_contains(['top', 'bottom'], state:'type') && half != -1,
            state:'type' = _halfSide(half);
        );
    );
    if (_contains(state, 'facing'),
        state:'facing' = flat_dir; //Generic
        //Hopper exception
        if (block == 'hopper',
            state:'facing' = _flipDir(flat_dir);
            if (direction == 'up', state:'facing' = 'down');
        );
        //Vertibles exception
        if (_isVertible(block),
            state:'facing' = direction;
            //Grindstone handling
            if (block == 'grindstone',
                state:'face' = 'wall';
                if (direction == 'up', state:'face' = 'floor');
                if (direction == 'down', state:'face' = 'ceiling');
            );
        );
        //Stairs addendum
        if (_contains(state, 'half'),
            if (_contains(['top', 'bottom'], state:'half'),
				if (state:'open' == null, state:'facing' = _flipDir(flat_dir));
                if (half != -1, state:'half' = _halfSide(half));
            );
        );
    );
    
    return(state);
);
                                                                               
__on_player_places_block(player, item_tuple, hand, block) ->
(
    properties = _getIdealState(block);
    set(pos(block), block, properties);
	if (_contains(block, 'stairs'), global_rotated = block);
);
__on_tick() ->
(
	if(global_rotated != null,
		//sleep(1000);
		//for(neighbours(global_rotated),
		//	print(pos(_));
		//	update(pos(_));
		//);
		//update(pos(global_rotated));
		upos = [pos(global_rotated):0 + 1, pos(global_rotated):1, pos(global_rotated):2];
		ublock = block(upos);
		set(upos, 'air');
		place_item('stone', upos);
		set(upos, ublock);
		global_rotated = null;
	);
);