//just a sponge work on lava like water
//by _GieR


__config() -> (
   m(
      l('stay_loaded','true')
   )
);


__on_player_places_block(player, item_tuple, hand, block)->(
    if(hand != 'mainhand' || item_tuple:0 != 'sponge' || all(neighbours(pos(block)),_!='lava'), return());
    set(pos(block),'wet_sponge');
    global_count = 0;
    global_recursion = 0;
    sponge(l(pos(block)));
);

sponge(coord_list)->(
    lava = [];
    for(coord_list,
        coord = _;
        for(neighbours(coord), 
            if(_ == 'lava' && global_recursion<=7 && global_count<=64,
                now_pos = pos(_);
                if(all(lava, _!=now_pos),
                    put(lava, null, pos(_));
                    global_count +=1;
                )
            )      
        )
    );     
    if(global_lava!=[] && global_recursion<=7 && global_count<=65,
        for(lava, set(_, 'air'));
        global_recursion += 1;
        sponge(lava);       
    )
)