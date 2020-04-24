__command()->(
    print('This camera acts the same as /c, except it will tp you back to the start location, and remember previous enchantments!');
    print('    /cam c: Will put you into spectator mode and give you night vision and conduit power to see better');
    print('    /cam s: Will put you back into survival, give you back any conduit power and night vision effects you had before');
    print('    /cam tpback true/false: If true, will tp you back to the start location, if not will not');
    print('    /cam checkvalid true/false: If true, will enable some checks to be made for changing into spectator, like drowning or burning');
    print('    /cam checkspecific check, true/false: Will toggle those checks on or off for turning into spectator');
    print('    /cam checks: Will print all available checks, along with their default and actual values');
);

global_prevnv=l(0,0);//Night vision duration, Night vision amplification
global_prevcp=l(0,0);//Conduit power duration, Conduit power amplification
global_tpback=true;
global_checkvalid=true;
global_checkspecific=m(
    l('falling',true),
    l('suffocating',true),
    l('burning',true)
);//you can add more checks in the future

c()->(
    player=player();
    valid=__valid(player);//will return null if you can go into spectator mode
    if(valid,return('You cannot go into spectator while '+valid));
    run('gamemode spectator '+player);
    global_prevnv=query(player,'effect','night_vision');
    global_prevcp=query(player,'effect','conduit_power');
    modify(player, 'effect', 'night_vision', 0, 0, false, false);//remove pre-existing effects
    modify(player, 'effect', 'conduit_power', 0, 0, false, false);//cos otherwise its buggy
    modify(player, 'effect', 'night_vision', 1000000, 255, false, false);
    modify(player, 'effect', 'conduit_power', 1000000, 255, false, false);
    global_pos=pos(player);
    return(null)
);

s()->(
    player=player();
    if(!query(player,'gamemode_id'),return('Can\'t use command in survival!'));//cos survvial is 0, so you cant use to to teleport back to a spot
    run('gamemode survival '+player);
    modify(player, 'effect', 'night_vision', get(global_prevnv,1), get(global_prevnv,1), true, true);
    modify(player, 'effect', 'conduit_power', get(global_prevcp,1), get(global_prevcp,1), true, true);
    if(global_tpback,modify(player,'pos',global_pos));//tps you back to start
    return(null)
);

tpback(tpback)->global_tpback=tpback;
checkvalid(checkvalid)->global_checkvalid=checkvalid;
checkspecific(check,val)->global_checkspecific:check:1=val;//I think its the correct syntax

checks()->(
    player=player();
    print('All currently available checks to turn into spectator mode');
    print('- falling '+global_checkspecific:0);
    print('- suffocating '+global_checkspecific:1);
    print('- burning '+global_checkspecific:2);
);

__valid(player)->(
    cause = null
    if(!global_checkvalid, return cause);// if checking for validity is off, return null
    if(global_checkspecific:0&& !(query(player,'nbt','OnGround')||player~'jumping'),cause='falling');//falling, but not jumping
    if(global_checkspecific:1&& query(player,'nbt','Air')<300,cause='suffocating');//suffocating, and also bobbing up and down in water, eg. waterlogged trapdoor
    if(global_checkspecific:2&& player~'burning',Scause='burning');//burning, and any others?
    return cause
);

//Conditions for cheating (copy pastad code from Minerva Datapack where I originally devised these checks)
//execute unless data entity @s {Dimension:0} run scoreboard players set @s valid 0 <- dunno about this one