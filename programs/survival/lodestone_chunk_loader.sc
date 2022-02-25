// Lodestones are Loadstones aka chunk loaders.
// When a player clicks on a lodestone while sneaking it marks that chunk to be force loaded
// and if they click on it again the unmarks that chunk.
// Breaking the loadstone also unmarks the chunk.

// Note that with this script players can unmark forceload chunks set by admins.

global_chunk_regex = '\\[[\\d,\\s]+\\]';

_block_forceload_chunk(block, mode) -> (
    pos = pos(block);
    return( in_dimension(block, run(str('/forceload %s %d %d', mode, pos:0, pos:2 ))) );
);

_block_toggle_forceload(p, block) -> (
    if( _block_forceload_chunk(block, 'query'):0,
        _block_unload_chunk(p,block);
    ,
        _block_load_chunk(p,block);
    );
);

_app_message(p, template, params) -> (
    print(p, format(str(template, params)));
);

_block_load_chunk(p, block) -> (
    _app_message(p, 'm Lodestone marked chunk %s for force loading', 
        str(_block_forceload_chunk(block, 'add'):1) ~ global_chunk_regex
    );
    sound( 'minecraft:block.amethyst_block.place', block );
);

_block_unload_chunk(p, block) -> (
    _app_message(p, 'p Lodestone unmarked chunk %s for force loading', 
        str(_block_forceload_chunk(block, 'remove'):1) ~ global_chunk_regex
    );
    sound( 'minecraft:block.amethyst_block.break', block );
    sound( 'minecraft:item.trident.return', block );
);

__on_player_breaks_block(p, block) -> (
    if( block == 'lodestone' && _block_forceload_chunk(block, 'query'):0,
        _block_unload_chunk(p, block);
    );
);

__on_player_right_clicks_block(p, item, hand, block, face, hitvec) -> (
    if( block == 'lodestone' && hand == 'mainhand' && p ~ 'sneaking',
        _block_toggle_forceload(p, block);
    );
);

