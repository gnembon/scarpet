//just a sponge work on lava like water
//by _GieR


__config() -> (
   m(
      l('stay_loaded','true')
   )
);


__on_player_places_block(player, item_tuple, hand, block)->(
    if(hand=='mainhand' && item_tuple:0=='sponge' && any(neighbours(pos(block)), _=='lava'),
        set(pos(block),'wet_sponge');
        count = 0;
        recursion = 0;
        sponge(l(pos(block)), recursion, count)
    )
);

sponge(coord_list, recursion, count)->(
    lava = {};
    if(recursion < 7 && count < 65,
        for(coord_list,
            if(count == 65, break());
            for(neighbours(_), 
                if(count == 65, break());
                if(_ == 'lava' && !has(lava, pos(_)),
                    lava += pos(_);
                    count +=1;
                )      
            )
        )
    );           
    if(length(lava)!=0,
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
