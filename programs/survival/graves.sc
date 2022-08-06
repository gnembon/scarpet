//Graves.sc
//Pretty much a 1 to 1 copy of Universal Graves mod by PatBox except some features like holograms and ability to telport to graves.
//Provides all major functions like, auto collect, grave locking, gui interface to pick items
//Please use debug subcommand if a grave suddenly disappears on you 
//By SurfingDude_

__config()->{
    'stay_loaded' -> true,
    'scope' -> 'global',
    'requires' -> {
        'carpet' -> '>=1.4.57'
    },
    'commands' -> {
        '' -> _() -> (
            print(player(),format('cu Graves\n','e Makes a grave when you die.\nThe three subcommands are: \n','m debug: ',' Use it when a grave/player head is missing from world.\n','y resetData: ',' Only allowed by admins. Deletes all available grave data.\n','b settings: ',' Only available to admins. Change the grave app settings through cli.'));
            null
        ),
        'settings requestTime <TimeInSeconds>' -> '_reqTime',
        'settings allowRequests <TrueOrFalse>' -> '_allowReq',
        'settings allowOwnerOnly <TrueOrFalse>' -> '_ownerOnly',
        'settings operatorPermissionLevel <PermissionLevel>' -> '_opPerms',
        // Actual Subcommands
        'resetData' -> '_resetData',
        'debug' -> '_debug'
    },
    'arguments' -> {
        'TimeInSeconds' -> {
            'type' -> 'int',
            'suggest' -> [10,20,30,45,60],
            'min' -> 1,
            'max' -> 10000
        },
        'TrueOrFalse' -> {
            'type' -> 'bool',
            'suggest' -> [true,false]
        },
        'PermissionLevel' -> {
            'type' -> 'int',
            'suggest' -> [2,4]
        }
    }
};

gdata_file = read_file('gdata','JSON');
if(gdata_file == null,
    global_gdata = {},
    //else when file already exists
    global_gdata = gdata_file;
);

settings_file = read_file('settings','JSON');
if(settings_file == null,
    global_settings = {
        'requestTime' -> 30,
        'allowRequests' -> true,
        'allowOnlyOwner' -> false,
        'operatorPermissionLevel' -> 2
    },
    //else when settings exist
    global_settings = settings_file;
);


//Player Events

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec)->(
	
    if(block!='player_head' || hand!='mainhand',return());
    ownership = block_data(pos(block)) ~ str(player) != null;
    if(ownership, _owner_clicked(player,block));
    if(!ownership && !global_settings:'allowOnlyOwner', _not_owner_clicked(player,block));
);

__on_player_breaks_block(player, block)->(

    if(block != 'player_head' || player~'gamemode'!='survival', return());
    nbt = nbt(block_data(block));

    //Replace the broken player head
    schedule(0,_(outer(player),outer(nbt)) -> (
        if(player~'gamemode'!='survival', return());
        run(str('setblock %s %s %s minecraft:player_head{ExtraType:"%s"}',nbt:'x',nbt:'y',nbt:'z',nbt:'SkullOwner':'Name'));
    ));

    //Delete the player head item that will drop as entity
    schedule(0, _(outer(pos(block))) -> (
	    
        coords = pos(block);
        //Found 2 to be reliable experimentally, needs more testing
        dx = 2;
        dy = 2;
        dz = 2;
        selector_item = str('@e[type=item,x=%d,y=%d,z=%d,dx=%d,dy=%d,dz=%d,nbt={Item:{id:"minecraft:player_head",Count:1b}}]',coords:0,coords:1,coords:2,dx,dy,dz);
        item = entity_selector(selector_item); 
        if(length(item)!=0, modify(entity_selector(selector_item):0,'remove'));

    ));
);

__on_explosion_outcome(pos, power, source, causer, mode, fire, blocks, entities)->(
    for(blocks,
        if(_ == 'player_head',
            posG = pos(_);
            posG:0 = floor(posG:0);
            posG:1 = floor(posG:1);
            posG:2 = floor(posG:2);
            schedule(50, _(outer(posG)) -> (
                set(posG,'air');
                run(str('setblock %s %s %s minecraft:player_head{ExtraType:"%s"}',posG:0,posG:1,posG:2,global_gdata:str(posG):'name'));
            ));
	   ))
);

__on_player_dies(player)->(

    if(!inventory_has_items(player), return());

    //To do: Nearest air block
    if(air(pos(player)),
        gravepos = pos(player),
        //else
        gravepos = pos_offset(pos(player),'up');
    );

    gravepos:0 = floor(gravepos:0);
    gravepos:2 = floor(gravepos:2);
    if(pos(player):1<0, gravepos:1 = top('motion',gravepos:0,0,gravepos:2) + 1); 

    print(player,format(str('nb A grave has been formed at %d, %d, %d in %s',gravepos:0,gravepos:1,gravepos:2,player~'dimension')));
    
    // Make the grave
    set(gravepos,'air');
    run(str('setblock %s %s %s minecraft:player_head{ExtraType:"%s"}',gravepos:0,gravepos:1,gravepos:2,player));

    item_count=0;
    global_gdata:str(gravepos)={};

    c_for(i=0,i<inventory_size(player),i+=1,

        if(inventory_get(player,i)!=null,

            slotItem= inventory_get(player,i);
            global_gdata:str(gravepos):str(i) = slotItem;
            item_count+=1;
            _saveGData();
        
        );
    
    );

    global_gdata:str(gravepos):'name'= str(player);
    global_gdata:str(gravepos):'xp_level'= query(player,'xp_level');
    global_gdata:str(gravepos):'xp_progress'= query(player,'xp_progress');
    global_gdata:str(gravepos):'locked'= true;
    global_gdata:str(gravepos):'item_count'= item_count;
    global_gdata:str(gravepos):'canRequest'= true;
    global_gdata:str(gravepos):'dimension' = player~'dimension';
    _saveGData();

    run(str('clear %s', player));
    modify(player,'xp_level',0);
    modify(player,'xp_progress',0);
);



//Grave Click Functions

_not_owner_clicked(player,block) -> (

    owner= global_gdata:str(pos(block)):'name';
    if(player('all')~owner==null || !global_settings:'allowRequests' , global_gdata:str(pos(block)):'locked' = false);
    
    //locked
    if(global_gdata:str(pos(block)):'locked' && global_gdata:str(pos(block)):'canRequest' && global_settings:'allowRequests',
    
        global_gdata:str(pos(block)):'canRequest'=false;

        schedule( global_settings:'requestTime' *20 ,_(outer(block)) -> 

            //Enable requesting again only after some specefic time
            global_gdata:str(pos(block)):'canRequest'= true
        );

        gdata=global_gdata:str(pos(block));
        requester = player;

        requestScreen=create_screen(owner,'generic_3x3',str('%s\'s Request',requester), _(screen,player,action,data,outer(gdata),outer(requester)) -> (
            if(action=='pickup' && data:'slot'==4,
                gdata:'locked'=false;
                close_screen(screen);
                print(requester,format('e Your request has been accepted. Please click the grave again.'));
            );
        ));
        if(screen_property(requestScreen,'open'),

            item='lime_concrete{display:{Name:\'{"text":"[Accept]","color":"green","italic":"false","bold":"true"}\'}}';
            inventory_set(requestScreen,4,1,item);
        
        ),

        global_gdata:str(pos(block)):'locked' && !global_gdata:str(pos(block)):'canRequest',
            print(player,format(str('br Permission denied. Try again after %d seconds of first request.',global_settings:'requestTime')));    
    );


    //not locked
    if(!global_gdata:str(pos(block)):'locked',

        gdata=global_gdata:str(pos(block));
        item_count=gdata:'item_count';
        
        if(gdata:'name'==null,
            run(str('setblock %d %d %d minecraft:air',pos(block):0, pos(block):1, pos(block):2));
            return();
        );


        deathCrate= create_screen(player,'generic_9x5',str('%s\'s Death Crate',gdata:'name'),_(screen,player,action,data,outer(gdata),outer(block)) ->(

            if(action=='pickup' || action== 'swap' || action=='quick_move' || action== 'throw' || action== 'pickup_all',
                _delete_data_slot(gdata,data:'slot');
                datafound=false;

                c_for(i=0,i<41,i+=1,if(gdata:str(i)!=null, datafound=true));

                if(datafound==false,
                    _delete_data(pos(block));
                    run(str('setblock %d %d %d minecraft:air',pos(block):0, pos(block):1, pos(block):2)); //for some reason /set doesnt seem to work. I might be doing bonk stuff somewhere
                );
            );
        ));


        if(screen_property(deathCrate,'open'),
            c_for(i=0,i<41,i+=1,
                item=gdata:str(i);
                inventory_set(deathCrate,i,item:1,item:0,item:2);
            );       
        );
        if(!gdata:'opened',modify(player,'add_xp',global_gdata:str(pos(block)):'xp'));
        gdata:'opened'=true;
    );
);

_owner_clicked(player,block) -> (

    gdata=global_gdata:str(pos(block));
    itemCount=0;
    c_for(i=0,i<inventory_size(player),i+=1,
        item=global_gdata:str(pos(block)):str(i);
        if(item!=null && inventory_get(player,i)==null,
            inventory_set(player,i,item:1,item:0,item:2);
            _delete_data_slot(gdata,str(i)),
         // when something is in slot
            item!=null && inventory_get(player,i)!=null,
            itemCount+=1;
        );
    );

    if(itemCount == 0,
        modify(player,'xp_level',gdata:'xp_level');
        modify(player,'xp_progress',gdata:'xp_progress');
        set(pos(block),'air');
        _delete_data(pos(block));
        return();          
    );

    deathCrate= create_screen(player,'generic_9x5',str('%s\'s Death Crate',gdata:'name'),_(screen,player,action,data,outer(gdata),outer(block)) ->(

            if(action=='pickup' || action== 'swap' || action=='quick_move' || action== 'throw' || action== 'pickup_all',
                _delete_data_slot(gdata,data:'slot');
                datafound=false;
            c_for(i=0,i<41,i+=1,if(gdata:str(i)!=null, datafound=true));

            if(datafound==false,
            _delete_data(pos(block));
            modify(player,'xp_level',gdata:'xp_level');
            modify(player,'xp_progress',gdata:'xp_progress');
            run(str('setblock %d %d %d minecraft:air',pos(block):0, pos(block):1, pos(block):2));
            );
            );

        ));

    if(screen_property(deathCrate,'open'),

            c_for(i=0,i<41,i+=1,
                item=gdata:str(i);
                inventory_set(deathCrate,i,item:1,item:0,item:2);
            );      
        );
);




//Utility Functions

_saveGData() -> (
    delete_file('gdata','JSON');
    write_file('gdata','JSON',global_gdata);
);

_saveSData() -> (
    delete_file('settings','JSON');
    write_file('settings','JSON',global_settings);
);

_delete_data(pos) -> (

    delete(global_gdata,str(pos));
    delete_file('gdata','JSON');
    _saveGData();

);

_delete_data_slot(data,slotNum) -> (

    delete(data,str(slotNum));
    delete_file('gdata','JSON');
    _saveGData();

);

_checkPermLevel(player) -> (

    if(player~'permission_level'< global_settings:'operatorPermissionLevel',
        print(player,format('r You do not have the permsission to execute this command. If you believe this to be an error, please contact server admin.'));
        return(false),
        return(true)
    );
);

//Commands

//Debug will replace all player heads that were destroyed for one reason or other. It will also print all replaced grave positions to player
_debug() -> (

    player = player(); 

    print(player,format('r Debuging started. The user is also requested to claim any grave they have, reagardless of how many items it contains. Any graves that were destroyed for one reason or other will be replaced. Coordinates of all non claimed graves: \n'));

    locationStrings=keys(global_gdata);
    coordsList=[];
    for(locationStrings,

        s= replace(str(_),'\\[','');
        s=replace(s,'\\]','');
        s=replace(s,' ','');
        coords= split('\\,',s);

        x=number(coords:0);
        y=number(coords:1);
        z=number(coords:2);
        pos=[x,y,z];
        owner= global_gdata:str(pos):'name';
        dim = global_gdata:str(pos):'dimension';

        if(owner == player()~'name',
            run(str('setblock %s %s %s minecraft:air',x,y,z));
            run(str('setblock %s %s %s minecraft:player_head{ExtraType : "%s"}',x,y,z,owner));
            print(player,format(str('e %s,%s,%s in %s\n',x,y,z,dim)));
        );
    );
    print(player,format('c If you know why the head disappeared. Please make an issue on github.'));
    null;
);

_resetData() -> (
    
    player = player();
    if(!_checkPermLevel(player), return());
    screen=create_screen(player,'generic_3x3',format('r Confirm Deletion'), _(screen,player,action,data) -> (
            if(action=='pickup' && data:'slot'==4,
                global_gdata = {};
                delete_file('gdata','JSON');
                _saveGData();
                close_screen(screen);
                print(player,format('rb Grave Data has been deleted....'))
            );
        ));
    if(screen_property(screen,'open'),
            item='red_concrete{display:{Name:\'{"text":"Beware, this will result in permanent loss of data","color":"red","italic":"false","bold":"true"}\'}}';
            inventory_set(screen,4,1,item);  
    );
);

_reqTime(time) -> (
    if(!_checkPermLevel(player()), return());
    global_settings:'requestTime' = time;
    print(player(),format('e Changed Request Time to: ',time));
    _saveSData();
);

_allowReq(value) -> (
    if(!_checkPermLevel(player()), return());
    global_settings:'allowRequests' = value;
    print(player(),format('e Changed Allow Request to: ',value));
    _saveSData();
);

_ownerOnly(value) -> (
    if(!_checkPermLevel(player()), return());
    global_settings:'allowOnlyOwner' = value;
    print(player(),format('e Changed Allow Owner Only to: ',value));
    _saveSData();
);

_opPerms(level) -> (
    if(!_checkPermLevel(player()), return());
    global_settings:'operatorPermissionLevel' = level;
    print(player(),format('e Changed Operator Permission Level to: ',level));
    _saveSData();
);
