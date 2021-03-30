///
// Silky Blockstates
// by BisUmTo
// (Carpet Mod 1.4.20)
//
// While sneaking, the silk_touch enchantment saves the Blockstates and the Blockdata of mined blocks.
// It doesn't apply to containers and blacklisted blocks.
///

__config() -> {'stay_loaded' -> true, 'scope' -> 'global'};

global_drop_in_creative = true;
global_create_item_whitelist = ['^spawner$', '^cake$'];
global_preserve_block_state_blacklist = ['_bed$', '_door$', '^sticky_piston$', '^piston$', '^bee_nest$', '_banner$', '^beehive$', '^redstone_wire$'];
global_preserve_block_data_blacklist = ['^bee_nest$', '^beehive$', '^campfire$', '^soul_campfire$', '^lectern$', '^jukebox$', '_banner$', '^player_head$', '_bed$'];
global_need_forced_placement = ['^spawner$','_sign$'];

// returns a map with all block_properties of the block and the relative value
//block_state(block) -> (
//    keys = block_properties(block) ;
//    values = map(keys, property(block, _));
//    m = {};
//    for(keys, m:_ = values:_i);
//    return(m);
//);

// returns the escaped version of the string (\->\\, d->\d)
escape_string(string,d) -> (
    replace(replace(string, '\\\\', '\\\\\\\\'), d, '\\\\'+d)
);

// returns true if the block is repleaceable while trying to place a block in its position
global_replaceable = {'air', 'bubble_column', 'cave_air', 'crimson_roots', 'dead_bush', 'fern', 'fire', 'grass', 'large_fern', 'lava','nether_sprout','sea_grass', 'soul_fire', 'structure_void', 'tall_grass', 'vines', 'void_air', 'warped_roots', 'water'};
replaceable(block) ->
     has(global_replaceable, str(block)) || block == 'snow' && property(block, 'layers') == 1;

// adds string as new Lore line to the dropped-item with the given uuid
_add_item_lore(uuid, string, color) -> (
    run(str('data modify entity %s Item.tag.display.Lore append value \'{"text":"%s","color":"%s"}\'',
        uuid,
        replace(escape_string(escape_string(string,'"'),'\''),'\\\\\\\\u00a7','\\u00a7'),
        color
    ));
);

// returns a formatted string of a block property (like in the F3 menu)
_formatted_property(key, value) -> (
    if(
        value == 'true', value='\\u00a7atrue\\u00a7r',
        value == 'false', value='\\u00a7cfalse\\u00a7r',
    );
    key + ': ' + value
);

// returns the list of dropped-items that could be dropped on this tick by mining the given block
_block_item(block) -> (
    entity_selector(str(
        '@e[type=item,limit=1,x=%d,y=%d,z=%d,dx=1,dy=1,dz=1,sort=nearest,nbt={Item:{id:"minecraft:%s",Count:1b},Age:0s}]',
        pos(block):0-1, pos(block):1-1, pos(block):2-1, replace(block, 'wall_', '')
    ));
);

// returns the nbt to assign to spawn a dropped-item of the given block
_nbt_block_item(block) -> (
    str('{Item:{id:"minecraft:%s",Count:1b},PickupDelay:10,Motion:[%f,.2,%f]}', replace(block, 'wall_', ''), rand(0.2) - 0.1, rand(0.2) - 0.1)
);

// merges the block's blockstates into the dropped-item spawned
_preserve_block_state(player, block) -> (
    blockstate = block_state(block);
    if(block ~ 'wall_' != null, blockstate:'wall'='true');
    encode_blockstate = encode_nbt(if(blockstate, blockstate, return()));
    item = _block_item(block);
    if(!item,
        if((_match_any(block, global_create_item_whitelist) || (global_drop_in_creative && player ~ 'gamemode' == 'creative')),
            item = [spawn('item', block, _nbt_block_item(block))],
            return()
        )
    );
    modify(item:0, 'nbt_merge', str('{Item:{tag:{Silked:1b,BlockStateTag:%s}}}', encode_blockstate));
    uuid = item:0 ~ 'command_name';
    //_add_item_lore(uuid, '\\u00a7lBlockStateTag:\\u00a7r', 'gray');
    for(blockstate, _add_item_lore(uuid, _formatted_property(_, blockstate:_), 'gray'))
);

// merges the block's blockdata into the dropped-item spawned
_preserve_block_data(player, block, blockdata) -> (
    if(!blockdata, return());
    item = _block_item(block);
    if(!item,
        if((_match_any(block, global_create_item_whitelist) || (global_drop_in_creative && player ~ 'gamemode' == 'creative')),
            item = [spawn('item', block, _nbt_block_item(block))],
            return()
        )
    );
    modify(item:0, 'nbt_merge', str('{Item:{tag:{Silked:1b,BlockEntityTag:%s}}}', blockdata));
    uuid = item:0 ~ 'command_name';
    //_add_item_lore(uuid, '\\u00a7lBlockEntityTag:\\u00a7r', 'gray');
    c_for(i = 0, i < length(blockdata), i += 50,
        _add_item_lore(uuid, slice(blockdata, i, i + 50), 'dark_gray')
    )
);

// returns true if the player holds a tool enchanted with the given enchant
_holds_enchant(player, enchant) -> (
    item = player ~ 'holds';
    if(!item || !(nbt = item:2), return(false));
    if(!(ench = get(nbt, 'Enchantments[]')), return(false));
    if(type(ench) != 'list', ench = [ench]);
    for(ench, if (_: 'id' == 'minecraft:' + enchant, return(true)));
    false
);

// return true if the string match at least one regex of the given list
_match_any(string, list) -> (
    for(list, if(string ~ _, return(true)));
    false
);

// return true if that item_tuple needs a forced placement
__need_forced_placement(item_tuple) -> (
    [item, count, nbt] = item_tuple;
    if(_match_any(item, global_need_forced_placement) && nbt:'Silked', return(true));
    if(nbt:'Silked' && nbt:'BlockStateTag{}':'wall', true, false)
);

// inject into the dropped-item the custom nbt
__on_player_breaks_block(player, block) ->
if(player ~ 'sneaking' && _holds_enchant(player, 'silk_touch'),
    blockdata = block_data(block);
    container_size = inventory_size(block);
    // wait for the dropped-item to spawn
    schedule(0, _(outer(player), outer(block), outer(blockdata), outer(container_size)) -> (
        if(!_match_any(block, global_preserve_block_state_blacklist),
            _preserve_block_state(player, block);
        );
        if(!_match_any(block, global_preserve_block_data_blacklist) && !container_size,
            _preserve_block_data(player, block, blockdata);
        )
    ))
);

// fixes the BlockEntityTag for deopped players and the wall_ version of blocks
__on_player_places_block(player, item_tuple, hand, block) -> 
if(__need_forced_placement(item_tuple),
    [item, count, nbt] = item_tuple;
    if(!nbt || (!nbt:'BlockStateTag{}' && !nbt:'BlockEntityTag{}'), return());
    // if not in blacklist, get the blockstate from item's nbt and format it correctly
    blockstate = if(_match_any(item, global_preserve_block_state_blacklist), encode_nbt(block_state(block)), nbt:'BlockStateTag{}');
    blockstate = if(blockstate, '[' + slice(replace(blockstate, ':', '='), 1, length(blockstate) - 1) + ']', '');
    // handles the wall_ version of the block
    block_id = if(blockstate ~ ',?wall="true"' != null, 
        blockstate = replace(blockstate, ',?wall="true"', '');
        replace(item,'(^|_)(?=[^_]+$)','$1wall_'),
        item
    );
    // if not in blacklist, get the data from item's nbt and format it correctly
    data = if(_match_any(item, global_preserve_block_data_blacklist), block_data(block), nbt:'BlockEntityTag{}');
    data = if(data, data, '');
    // sets the block with correct blockstate and data
    set(block, block_id + blockstate + data)
)
