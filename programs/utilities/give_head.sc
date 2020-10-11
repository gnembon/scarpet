///
// Give Head
// by BisUmTo
// (Carpet Mod 1.4.9)
//
// Adds /give_head command to easly get player_heads.
//
// /give_head value 'base64value' => Values can be found on websites like https://minecraft-heads.com/custom-heads
// /give_head texture 'textureURL' => TextureURL is the final part of http://textures.minecraft.net/texture/... urls
// /give_head nickname 'SkullOwner' => SkullOwner is the nickname of the player
///

__config() -> {'scope'->'global'};
__command() -> '/give_head value \'base64value\'\
/give_head texture \'textureURL\'
/give_head nickname \'SkullOwner\'';

value(valore) -> (
    run(str(
        '/give %s minecraft:player_head{SkullOwner:{Id:[I;%d,%d,%d,%d],Properties:{textures:[{Value:"%s"}]}}}',
        player()~'command_name',
        rand(2147483647),
        rand(2147483647),
        rand(2147483647),
        rand(2147483647),
        valore
    ))
);

global_utf16 = {'"'->34,'.'->46,'/'->47,'0'->48,'1'->49,'2'->50,'3'->51,'4'->52,'5'->53,'6'->54,'7'->55,'8'->56,'9'->57,':'->58,'I'->73,'K'->75,'N'->78,'S'->83,'a'->97,'b'->98,'c'->99,'d'->100,'e'->101,'f'->102,'h'->104,'i'->105,'l'->108,'m'->109,'n'->110,'p'->112,'r'->114,'s'->115,'t'->116,'u'->117,'x'->120,'{'->123,'}'->125};
_utf16(c) -> (
    return(global_utf16:c)
);

global_base64chars = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'];
_encode_base64(s) -> (
    r = ''; 
    p = ''; 
    c = length(s) % 3;
    p += '='*(3-c);
    s += '\0'*(3-c);
    s_list=split('',s);
    c_for(c=0,c<length(s),c+=3,
        n = _utf16(s_list:c) * 2^16 + _utf16(s_list:(c+1)) * 2^8 + _utf16(s_list:(c+2));
        n = [floor(n / 2^18) % 64, floor(n / 2^12) % 64, floor(n / 2^6) % 64, n % 64]; 
        r += global_base64chars:(n:0) + global_base64chars:(n:1) + global_base64chars:(n:2) + global_base64chars:(n:3)
    );
    return(slice(r,0, length(r) - length(p)) + p);
);

texture(valore) -> (
    run(str(
        '/give %s minecraft:player_head{SkullOwner:{Id:[I;%d,%d,%d,%d],Properties:{textures:[{Value:"%s"}]}}}',
        player()~'command_name',
        rand(2147483647),
        rand(2147483647),
        rand(2147483647),
        rand(2147483647),
        _encode_base64(str('{"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/%s"}}}',valore))
    ))
);

nickname(nome) -> (
    run(str(
        '/give %s minecraft:player_head{SkullOwner:"%s"}',
        player()~'command_name',
        nome
    ))
)
