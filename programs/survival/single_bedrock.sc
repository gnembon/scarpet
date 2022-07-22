global_DIM = 'the_nether';
global_CHUNK_SIZE = 16;
global_DRAW_RADIUS = 1;
global_INTERVAL = 20;
global_DURATION = global_INTERVAL + 2;
global_TOP_Y = 127;
global_CHECK_HEIGHT = 4;
global_BLOCK_NAME = 'bedrock';
global_DRAW_HEIGHT = 6;
global_COLOR = 0x00ff0050;

__config() -> {
  'command_permission' -> 'players'
};

global_enabled = false;
global_player = null;
global_from_bottom = false;
global_last_chunk_x = null;
global_last_chunk_z = null;
global_shapes = null;

__command() -> (
  global_player = player();
  global_enabled = !global_enabled;
  _clear_cache();
  if(global_enabled, (
    in_dimension(global_DIM, schedule(0, '_tick'));
    print('Enabled');
  ),
    print('Disabled');
  );
  null;
);
bottom() -> (
  global_from_bottom = !global_from_bottom;
  _clear_cache();
  if(global_from_bottom, print('Enabled bottem'), print('Disabled bottem'));
  null;
);

_clear_cache() -> (
  global_last_chunk_x = null;
  global_last_chunk_z = null;
  global_shapes = null;
);

_tick() -> (
  if(global_enabled &&
    global_player~'dimension' == global_DIM,
    [x, y, z] = pos(global_player);
    chunkX = floor(x / global_CHUNK_SIZE);
    chunkZ = floor(z / global_CHUNK_SIZE);
    if(chunkX != global_last_chunk_x || chunkZ != global_last_chunk_z,
      global_last_chunk_x = chunkX;
      global_last_chunk_z = chunkZ;
      global_shapes = [];
      c_for(cx = chunkX - global_DRAW_RADIUS, cx <= chunkX + global_DRAW_RADIUS, cx += 1,
        c_for(cz = chunkZ - global_DRAW_RADIUS, cz <= chunkZ + global_DRAW_RADIUS, cz += 1,
          _find_in_chunk(cx, cz, global_shapes);
        );
      );
    );
    if(global_enabled, draw_shape(global_shapes));
  );
  if(global_enabled, schedule(global_INTERVAL, '_tick'), _clear_cache());
);

_find_in_chunk(chunkX, chunkZ, shapes) -> (
  chunkStartX = chunkX * global_CHUNK_SIZE;
  chunkStartZ = chunkZ * global_CHUNK_SIZE;
  c_for(x = chunkStartX, x < chunkStartX + global_CHUNK_SIZE, x += 1,
    c_for(z = chunkStartZ, z < chunkStartZ + global_CHUNK_SIZE, z += 1,
      if(block(x, global_TOP_Y, z) == global_BLOCK_NAME &&
        all(rect([x, global_TOP_Y - 1, z], [0, global_CHECK_HEIGHT - 1, 0], [0, 0, 0]), _ != global_BLOCK_NAME),
        from = [x, global_TOP_Y - global_DRAW_HEIGHT, z];
        if(global_from_bottom, from:1 = 0);
        shapes += ['box', global_DURATION, 'from', from, 'to', [x + 1, global_TOP_Y + 1, z + 1], 'color', global_COLOR, 'fill', global_COLOR];
      );
    );
  );
);
