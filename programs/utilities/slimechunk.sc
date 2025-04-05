//Run the slimechunk to tell whether you are in a slime chunk
__command() ->
(
    position = query(player(), 'pos');
    is_slimechunk = in_slime_chunk(position);
    if (is_slimechunk, '§aSlime chunk!', '§cNot a slime chunk :\(');
);
