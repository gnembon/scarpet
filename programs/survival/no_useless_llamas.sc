// Have you ever thought "hay I could a llama" only to find 5 llamas with only 3 slots.
// This app makes every llama have a full sized inventory. Simply click on the llama with a chest.


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

