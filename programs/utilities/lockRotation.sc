///////////////////////////////////////////////////////////////////////////////
// Rotation Lock
// v1.0.1
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
_castSide(b, raycast) ->
(
	blockpos = pos(b);
	blockpos:0 += 0.5;
	blockpos:1 += 0.5;
	blockpos:2 += 0.5;
	
	difpos = [raycast:0 - blockpos:0, raycast:1 - blockpos:1, raycast:2 - blockpos:2];
	
	truepos = [];
	for (difpos, truepos:_i = ceil(abs(difpos:_i) - 0.49999) * _sign(difpos:_i));
	
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
    if (_contains(['end_rod', 'lightning_rod'], block), return(true));
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

// App Methods
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
        
		flat = hit_dir;
		if (_contains(['up', 'down'], flat), flat = flat_dir);
		
        newProfile = [hit_dir, half, _flipDir(flat)];
    ,
        newProfile = [direction, -1, flat_dir];
    );
    
    if (global_lockProfile != null && newProfile == global_lockProfile,
        global_lockProfile = null;
    ,
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
            state:'type' = halfSide(half);
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
            if (_contains(['top', 'bottom'], state:'half') && half != -1,
                state:'half' = _halfSide(half);
            );
        );
    );
    
    return(state);
);
                                                                               
__on_player_places_block(player, item_tuple, hand, block) ->
(
    properties = _getIdealState(block);
    set(pos(block), block, properties);
);