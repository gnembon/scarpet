//allows player to revive dead corals with water bottles
//By Ghoulboy

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_right_clicks_block (player, item_tuple,hand, block,face,hitvec) -> (
    // player holds a water bottle and targets a dead coral block
    if(item_tuple:0=='potion'&& block ~ '^dead_\\w+_coral_block$' && item_tuple:2:'Potion' == 'minecraft:water',
        position = pos(block);
        // set to dead version of the block
        set(position,block-'dead_');
        if(player~'gamemode'!='creative',
            // use water bottle
            if (item_tuple:1 > 1,
                // cheaty stacked water bottle
                inventory_set(player,if(hand=='mainhand',query(player,'selected_slot'),40),item_tuple:1-1);
                spawn('item', pos(player), '{Item:{id:"minecraft:glass_bottle",Count:1b},PickupDelay:0}');
            , // else vanilla - one water bottle
                inventory_set(player,if(hand=='mainhand',query(player,'selected_slot'),40),1,'glass_bottle');
            );
        );
        // coral blocks schedule check on them 60+rand(40) blocks to make sure they are property supported
        schedule(60+rand(40), _(outer(position)) -> block_tick(position));
    );
);
