//tpUtils.sc
//Adds homes and warps for quick teleportation.
//By SurfingDude_

global_max_homes = 20; //Edit to allow more/less homes
global_operator_permission_level = 2;

__config()->{
    'scope' -> 'global',
    'commands' ->
    {   
        'home add <nameh>' -> '_hadd',
        'home tp <hlocation>' -> '_htp',
        'home remove <hlocation>' -> '_hremove',
        'home list' -> '_hlist',
        'warp add <namew> <description>' -> '_wadd',
        'warp tp <wlocation>' -> '_wtp',
        'warp remove <wlocation>' -> '_wremove',
        'warp menu' -> '_wmenu'
    },
    'arguments' ->
    {
        'hlocation' -> {
            'type' -> 'text',
            'suggester' -> _(args) -> (
                keys(global_hdata:str(player()~'uuid'))
            ),
            'case_sensitive' -> false
        },

        'nameh' -> {
            'type' -> 'text',
            'suggest' -> ['Spawn','Base','End Portal','Nether Portal'],
            'case_sensitive' -> false
        },

        'namew' -> {
            'type' -> 'string',
            'suggest' -> ['Spawn','Marketplace','Arena'],
            'case_sensitive' -> false
        },

        'description' -> {
            'type' -> 'text'
        },

        'wlocation' -> {
            'type' -> 'text',
            'suggester' -> _(args) -> (
                keys(global_wdata)
            ),
            'case_sensitive' -> false
        }
    }
};

//Initializing homes and warps data
data = read_file('dataFile','JSON');
if(has(data,'hdata'),
    global_hdata = data:'hdata',
    global_hdata = {}
);
if(has(data,'wdata'),
    global_wdata = data:'wdata',
    global_wdata = {}
);

__on_close()->(
    data = {
        'hdata' -> global_hdata,
        'wdata' -> global_wdata
    };
    write_file('dataFile','JSON',data);
);


//Home functions

_hadd(name) -> (
    
    player=player();
    uuid = str(player~'uuid');

    if(length(global_hdata:uuid)>global_max_homes,
        print(player,format('r Max numbers of homes achieved. Please update/delete an existing one.'));
        return();
    );
    if(global_hdata:uuid==null,global_hdata:uuid={});
    global_names = keys(global_hdata:uuid);
    if(global_names~name!=null, print(player,format(str('r %s is already in use. Therefore, updating the old home to new location. \n',name))));

    global_hdata:uuid:name={};
    global_hdata:uuid:name:'location' = pos(player);
    global_hdata:uuid:name:'pitch' = player~'pitch';
    global_hdata:uuid:name:'yaw' = player~'yaw';
    global_hdata:uuid:name:'dimension' = player~'dimension';
    print(player,format('e Succesfully saved home to location with name: '+name));
);

_htp(name) -> (

    info = global_hdata:str(player()~'uuid'):name;
    if(info==null,
        print(player,format('r \nThe specefied location does not exist. Please use home list to know your current list'));
        return();
    );
    dimension = info:'dimension';
    pitch = info:'pitch';
    yaw = info:'yaw';
    location = info:'location';

    run(str('execute in %s run tp %s %f %f %f %f %f',dimension,player(),location:0,location:1,location:2,yaw,pitch));

);

_hremove(name) -> (
    if(global_hdata:str(player()~'uuid'):name==null,
        print(player,format('r \nThe specefied location does not exist. Please use home list to know your current list'));
        return();
    );
    delete(global_hdata:str(player()~'uuid'),name);
    print(player(),format(str('e \n%s was succesfully removed from your home list.',name)));
);

_hlist() -> (

    info = global_hdata:str(player()~'uuid');
    if(length(keys(info))==0,print(player(),format('r You have not set any homes yet.')));
    print(player(),format('qub \nList of Available Homes: \n'));
    for(keys(info),

        loc = info:_:'location';
        [x, y, z] = map(loc, floor(_));
        dim = info:_:'dimension';
        if(dim=='the_end',
            dim='End';
            color='m',
        //if else nether
            dim=='the_nether',
            dim='Nether';
            color='r',
        //else
            dim='Overworld';
            color='e'
        );
        print(player(),format(str('%s Name: %s, Location: %s %s %s, Dimension: %s',color,_,x,y,z,dim),str('!/%s home tp %s',system_info('app_name'),_),str('^di Click here to tp to: %s',_)));
    );
);

//Warps

_wadd(name,description)->(

    player=player();

    if(player~'permission_level'<global_operator_permission_level,
        print(player,format('r You do not have the permsission to execute this command. If you believe this to be an error, please contact server admin.'));
        return();
    );

    global_names = keys(global_wdata);
    if(global_names~name!=null, print(player,format(str('r %s is already in use. Therefore, updating the warp home to new location. \n',name))));
    if(global_wdata:name==null,global_wdata:name={});

    global_wdata:name:'location' = pos(player);
    global_wdata:name:'pitch' = player~'pitch';
    global_wdata:name:'yaw' = player~'yaw';
    global_wdata:name:'dimension' = player~'dimension';
    global_wdata:name:'description'= description;

    //Extra stuff that is only availble to people with fake inv screens
    if(_screenCheck(),
        global_wdata:name:'item'= query(player,'holds','mainhand'):0;
    );
    print(player,format('e Succesfully saved warp to location with name: '+name));

);

_wremove(name)->(

    player=player();
    if(player~'permission_level'<global_operator_permission_level,
        print(player,format('r You do not have the permsission to execute this command. If you believe this to be an error, please contact server admin.'));
        return();
    );

    if(global_wdata:name==null,
        print(player,format('r \nThe specefied location does not exist. Please use warp menu to know about current warps'));
        return();
    );
    delete(global_wdata,name);
    print(player,format(str('e \n%s was succesfully removed from warp list.',name)));

);

_wtp(name) -> (

    player=player();
    info = global_wdata:name;
    if(info==null,
        print(player,format('r \nThe specefied location does not exist. Please use warp menu to know about current warps'));
        return();
    );
    dimension = info:'dimension';
    pitch = info:'pitch';
    yaw = info:'yaw';
    location = info:'location';

    run(str('execute in %s run tp %s %f %f %f %f %f',dimension,player,location:0,location:1,location:2,yaw,pitch));

);

_wmenu() -> (

    player= player();
    if(_screenCheck(),
        warpCount = length(keys(global_wdata));
        rowCount = ceil(warpCount/9);

        menu= create_screen(player,str('generic_9x%s',rowCount),format('tb Available Warps'),_(screen,player,action,data) -> (
            
            if(action=='pickup' || action== 'swap' || action=='quick_move' || action== 'throw' || action== 'pickup_all',
                itemNbt= inventory_get(screen,data:'slot'):2;
                itemName= parse_nbt(itemNbt:'display':'Name'):0:'text';
                _wtp(itemName);
                close_screen(screen);
            );
        ));

        if(screen_property(menu,'open'),

            slot=0;
            for(keys(global_wdata),

                info = global_wdata:_;
                iType=info:'item';
                if(iType==null,iType='barrier');
                des=info:'description';
                loc=info:'location';
                [x, y, z] = map(loc, floor(_));
                dim= info:'dimension';
                if(dim=='the_end',
                    color='magenta',
                //if else nether
                dim=='the_nether',
                    color='red',
                //else
                    color='green'
                );
                item=str('%s{display:{Name:\'[{"text":"%s","color":"%s","italic":false}]\',Lore:[\'[{"text":"Location: %s %s %s","color":"blue","italic":false}]\',\'[{"text":"Description:","color":"white","italic":false}]\',\'[{"text":"%s","italic":false}]\']}}',iType,str(_),color,x,y,z,des);
                inventory_set(menu,slot,1,item);
                slot+=1;
            );
        
        );

    );


);


//utility
_screenCheck() -> (
    sVersion= split('\\+',system_info('scarpet_version')):0; //fetches first part before the +, eg 1.4.57
    vNumbers=split('\\.',sVersion);
    [main, sub, x] = map(vNumbers,number(_));
    if(main>1, 
  true,
  main == 1,
  if(sub>4,
    true,
    sub==4,
    x >= 56,
    false
  ),
  false
)
);
