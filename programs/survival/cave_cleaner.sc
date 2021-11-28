// Kill zombies/drowneds holding naturally dropped items within 50 blocks of the player
// Reimplementing cave cleaner from Hermitcraft Season 8 since it's not public
// Only effective on spectator player

// Currently zombies/drowneds are not necessarily in cave to be killed 
// And I have no plan to implement "if in cave" checks

// Be ware zombies/drowneds that users want to keep might be removed if they are holding items listed in the if condition
// I have no plan to implement any fix to this either

__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__command() -> (
    if (query(player(),'gamemode') != 'spectator',
        exit('You must be in spectator mode to run cave cleaner');
    );
    count = 0;
    l (x, y, z) = pos(player());
    for (zombies = entity_area('zombie', x,y,z, 50,50,50), 
        holding = query(_, 'holds', 'mainhand');
        if (holding != null, 
            holding_item = get(holding,0);
            if (holding_item == 'string' || holding_item == 'rotten_flesh' || holding_item == 'bone' || holding_item == 'arrow' || holding_item == 'spider_eye' || holding_item == 'glass_bottle' || holding_item == 'gunpowder' || holding_item == 'sugar' || holding_item == 'stick' || holding_item == 'glowstone_dust' || holding_item == 'redstone' || holding_item == 'egg' , 
                modify(_, 'remove');
                count = count + 1
            )
        )
    );
    for (drowneds = entity_area('drowned', x,y,z, 50,50,50), 
        holding = query(_, 'holds', 'mainhand');
        if (holding != null, 
            holding_item = get(holding,0);
            if (holding_item == 'string' || holding_item == 'rotten_flesh' || holding_item == 'bone' || holding_item == 'arrow' || holding_item == 'spider_eye' || holding_item == 'glass_bottle' || holding_item == 'gunpowder' || holding_item == 'sugar' || holding_item == 'stick' || holding_item == 'glowstone_dust' || holding_item == 'redstone' || holding_item == 'egg' , 
                modify(_, 'remove');
                count = count + 1
            )
        )
    );
    print ('Successfully removed ' + count + ' zombie(s)/drowned(s).');
    null
)
