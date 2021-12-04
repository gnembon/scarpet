// Any Llama Clicked with a chest will have a full sized Inventory


// Max out the inventory size of the lama 
_set_max_llama_storage(llama) -> (
    modify(llama, 'nbt_merge', nbt('{Strength: 5}'));
);

// detect when a player clicks a llama with a chest
__on_player_interacts_with_entity(p, entity, hand) -> (
    if(entity~'type'=='llama' && p~'holds':0 == 'chest',
        _set_max_llama_storage(entity);
    );
);