//A Balanced way to get player heads in survival.

// stay loaded
__config() -> (
   m(
      l('stay_loaded','true')
   )
);

__on_player_dies(player) -> (
  if(rand(1) <= 0.3,
    xv = rand(0.5)-0.25;
    yv = rand(0.5);
    zv = rand(0.5)-0.25;

    motion = '[' + xv + 'd, ' + yv + 'd, ' + zv + 'd' + ']';
    data = '{Motion: ' + motion + ', Item: {id: "minecraft:player_head", Count:1b, tag:{SkullOwner: "' + player + '"}}}, PickupDelay: 3s';
    spawn('item', pos(player), data);
  );
)
