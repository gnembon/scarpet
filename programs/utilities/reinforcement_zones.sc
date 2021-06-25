__on_player_interacts_with_entity(player, entity, hand) -> if( (etype = entity~'type'; etype == 'drowned' || etype == 'zombie') && hand == 'mainhand' && player~'holds':0 == 'stick',
   ep = map(pos(entity), floor(_))+0.5;
   shapes = [];
   for(range(-1,2),x=_; for(range(-1,2),y=_; for(range(-1,2),z=_;
      fc = [x,y,z]*7+0.5; //
      tc = [x,y,z]*40+0.5;
      ff = map(fc, min(_, tc:_i) )-0.5;
      tt = map(fc, max(_, tc:_i) )+0.5;
      shapes += ['box', 72000, 'from', ff, 'to', tt,'color', 0x007777ff, 'fill', 0x00777722, 'follow', entity, 'snap', 'dxdydz']
   ) ) );
   draw_shape(shapes);
);