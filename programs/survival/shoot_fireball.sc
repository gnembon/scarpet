__on_player_uses_item(player, item_tuple, hand)->(
    if(get(item_tuple, 0) == 'fire_charge',
        playerPos = player ~ 'pos' + l(0, 1, 0); // Set the position where the fireball will spawn
        fireball = spawn('fireball', playerPos); // Spawn the fireball
        sound('entity.blaze.shoot', playerPos); // Play a sound

        playerPitch = player ~ 'pitch'; // Detect where the player is looking
        playerYaw = player ~ 'yaw' + 90;

        v = player ~ 'look';

        modify(fireball, 'pos', playerPos+v*2); // Move the fireball in front of the player so it doesn't cover their screen for a second when it spawns
        modify(fireball, 'nbt_merge', encode_nbt({'power' -> v*0.25})); // Set the velocity of the fireball to the direction vector
        modify(fireball, 'nbt_merge', {'Owner' -> query(player, 'nbt', 'UUID')}); // Make the fireball count as a player kill
        modify(fireball, 'fire', 20); // Seems to make it last longer?

        if(player ~ 'gamemode' == 'survival' || player ~ 'gamemode' == 'adventure', // Remove a fire charge from the selected slot or offhand if the player is in survival mode
            inventory_set(player, if(hand == 'offhand', -1, player ~ 'selected_slot'), (item_tuple: 1) - 1);
        );
    );
);