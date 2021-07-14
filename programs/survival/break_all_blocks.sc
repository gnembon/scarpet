global_blocks = {
  'end_portal_frame' -> 15,
  'bedrock' -> 0,
};

__on_player_clicks_block(player, block, face) ->
(
   step = global_blocks:str(block);
   if (step == 0,
      set(pos(block), 'air'); // instamine
   , step != null,
      schedule(step, '_break', player, pos(block), str(block), step, 1);
   )
);

_break(player, pos, name, step, lvl) ->
(
   current = player~'active_block';
   if (current != name || pos(current) != pos,
      modify(player, 'breaking_progress', null);
   ,
      modify(player, 'breaking_progress', lvl);
      if (lvl >= 10, set(pos, 'air'));
      schedule(step, '_break', player, pos, name, step, lvl+1)
   );
)
