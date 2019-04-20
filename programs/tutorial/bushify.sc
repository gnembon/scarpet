if(!rand(4) && air(_) && for(neighbours(_), !air(_)) > 1,
    set(_,
        first(neighbours(_),material(_)=='leaves') || block('oak_leaves[persistent=true]')
    )
)

/script run 
$ bushify_block(b) -> (
$    if(!rand(4) && air(b) && for(neighbours(b), !air(_)) > 1,
$        set(b,
$            first(neighbours(b),material(_)=='leaves') || block('oak_leaves[persistent=true]')
$        )
$    )
$);
$
$'Spawns random bushez in a volume of blocks';
$ bushify_area(from_x, from_y, from_z, to_x, to_y, to_z) -> (
$    volume(from_x, from_y, from_z, to_x, to_y, to_z,
$        bushify_block(_)
$    )
$)