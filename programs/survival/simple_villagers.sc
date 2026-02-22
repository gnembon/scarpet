// 
// by Gnottero
// (Carpet Mod 1.4.169)
// Allows the player to pickup villagers by shift-right clicking on them
// Compatible with minecraft 1.21
//


// [Begin] Scarpet Events

__on_player_interacts_with_entity(player, entity, hand) -> (
    if (!query(player, 'sneaking'), return());
    if (hand != 'mainhand', return());
    if (query(player, 'holds') != null, return());
    if (query(entity, 'type') != 'villager', return());
    itemizeVillager(entity);
);

__on_player_right_clicks_block(player, item_tuple, hand, block, face, hitvec) -> (
    if (!query(player, 'sneaking'), return());
    if (hand != 'mainhand', return());
    if (item_tuple:0 != 'clock', return());
    if (item_tuple:2:'components.minecraft:custom_data.VillagerData' == null, return());
    placeVillager(player, item_tuple, block, face);
);

// [End] Scarpet Events

// [Begin] Custom Functions

itemizeVillager(entity) -> (
    villagerData = query(entity, 'nbt');
    clockItem = spawn('item', pos(entity), str('{Item:{id:"minecraft:clock",count:1,components:{"minecraft:enchantment_glint_override":true,"minecraft:lore":[{"color":"gray","italic":false,"text":"- Profession: %s"},{"color":"gray","italic":false,"text":"- Level: %d"}],"minecraft:item_name":"Packed Villager","minecraft:custom_data":{VillagerData: %s}}}}', title(replace(villagerData:'VillagerData.profession', 'minecraft:', '')), villagerData:'VillagerData.level', villagerData));
    particle('cloud', pos(entity) + [0, query(entity, 'eye_height'), 0]);
    modify(entity, 'remove');
);

placeVillager(player, item_tuple, block, face) -> (
    entity = spawn('villager', pos_offset(pos(block), face, 1) + [0.5, 0, 0.5], item_tuple:2:'components.minecraft:custom_data.VillagerData');
    particle('cloud', pos(entity) + [0, query(entity, 'eye_height'), 0]);
    modify(player, 'swing');
    inventory_set(player, query(player, 'selected_slot'), 0);
);

// [End] Custom Functions