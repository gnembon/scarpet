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
    count = 0;
    recursion = 0;
    sponge(l(pos(block)), recursion, count);
);

sponge(coord_list, recursion, count)->(
    lava = {};
    for(coord_list,
        coord = _;
        for(neighbours(coord), 
            if(!has(lava, pos(_)) && recursion<=7 && count<=64,
                if(_ == 'lava',
                    lava += pos(_);
                    count +=1;
                )
            )      
        )
    );     
    if(lava!={} && recursion<=7 && count<=65,
        for(lava, 
            set(_, 'air');
            for(neighbours(_),
                if(_ == 'lava', update(pos(_)))
            )
        );
        recursion += 1;
        sponge(lava, recursion, count);       
    )
)
