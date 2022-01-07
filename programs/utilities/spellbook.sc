// A utility for creating command books

__config()->{
  'command_permission' -> 'ops', 
  'commands' -> {
    '' -> 'help',
    '<book> give' -> 'give_book',
    '<book> update' -> 'give_book',
    '<book> set <title> <command>' -> 'set_command',
    '<book> remove <title>' -> 'delete_command',
    '<book> read' -> 'display_book'
  },
  'arguments' -> {
    'page' -> {'type' -> 'int', 'min' -> 0, 'max' -> 100},
    'line' -> {'type' -> 'int', 'min' -> 0, 'max' -> 14},
    'command' -> {'type' -> 'string', 'suggest' -> ['"/bar"', '"/tp @p x y z"', '"/gamerule doFireTick true"']},
    'title' -> {'type' -> 'string', 'suggest' -> ['Foo', '"Warp to Spawn"', '"Fire Tick: true"']},
    'book' -> {'type' -> 'string', 'suggest' -> ['incantations', 'warps', 'zones']},
  }
};

global_script_version = 4;
// title, color, command
global_spell_template = '{"text":"[%s]","color":"%s","clickEvent":{"action":"run_command","value":"%s"}},{"text":"\\\\n"}';
global_title_template = '{"text":"Nimda %s", "bold":true},{"text":".\\\\n"}';

global_spells_per_page = 6;

global_shuffle_colors = [
  'dark_red',
  'gold',
  'dark_green',
  'dark_aqua',
  'dark_blue',
  'dark_purple'
];


// pages: strings/array/nbt, title, author
global_book_template = '{pages:%s, title:"Nimda %s %s",author:"%s"}';

global_base_book_data = {
  'vbook' -> 0,
  'vscript' -> global_script_version,
  'author' -> 'server',
  'subject' -> '',
  'title' -> '',
  'spells' -> {},
  'render' -> {
    'v' -> 'v0.0',
    'nbt' -> ''
  }
};


__on_player_right_clicks_block(p, item_tuple, hand, block, face, hitvec) -> (
  if(block == block('lectern'),

    print(p, block);
    // print(p, face);
    // state = block_state(block, property)
    // block_tags(block)
    pos = pos(block);
    nbt = block_data(pos);
    print(p, pos);
    print(p, nbt);
    // print(p, hitvec);
    // print(p,query(p, 'trace'));
    //  block(x, y, z) 
  );
);


_read_book(name) -> (
  file = read_file(name, 'json');
  if( file, 
    return(file);
  ,
    file = global_base_book_data;
    file:'title' = name;
    file:'subject' = name;
    return(file);
  );
);

_write_book(book) -> (
  book:'vscript' = global_script_version;
  book:'vbook' = book:'vbook' + 1;
  write_file(book:'title', 'json', book);
);

_write_render(book) -> (
  write_file(book:'title', 'json', book);
);

display_book(book_name) -> (
  book = _read_book(book_name);
  p = player();
  print(p, str('%s spells:', book_name));
  for(pairs(book:'spells'),
    print(p,str('[ %s ]( %s )', _:0, _:1));
  );
);

give_book(name) -> (
  book = _read_book(name);
  p = player();
  print(p, str('/give %s written_book%s', query(p, 'command_name'), _render_book_nbt(book)));
  print(p, 
    run(str('/give %s written_book%s', query(p, 'command_name'), _render_book_nbt(book))):1
  );
);

delete_command(book_name, title) -> (
  book = _read_book(book_name);
  delete(book:'spells':title);
  _write_book(book);
);


set_command(book_name, title, command) -> (
  p = player();
  book = _read_book(book_name);
  book:'spells':title = command;
  print(p, str('%s spell set: [ %s ]( %s ).', book_name, title, command));
  _write_book(book);
);

_render_single_spell(title, command, color) -> (
  return(str(global_spell_template,title,color,command));
);

_render_pages(book) -> (
  spells = pairs(book:'spells');
  pages = [];
  a = 0;
  l = length(spells);
  while(a<length(spells),50,
    page = [str(global_title_template, book:'title')];
    loop( min(global_spells_per_page,l) ,
      put(page, null, _render_single_spell(spells:a:0, spells:a:1, _shuffle_color()));
      a += 1;
    );
    l = l - global_spells_per_page;
    put(pages, null, page);
    if( l < 1, break() );  
    

  );
  return(str('[%s]',
    str('\'[%s]\'', join(']\',\'[', pages)) 
  ));
);

_render_version(book) -> (
  return(str('v%d.%d', global_script_version, book:'vbook'));
);

_render_book_nbt(book) -> (
  v = _render_version(book);
  print(player(), global_script_version);
  print(player(), v);
  print(player(), book:'render':'v');
  print(player(), book:'render':'v' == v);
  if(book:'render' && book:'render':'v' == v,
    return( book:'render':'nbt' );
  ,
    book:'render' = {
      'v' -> v,
      'nbt' -> str(
        global_book_template, 
        _render_pages(book), 
        book:'title', 
        v, 
        book:'author'
      )
    };
    _write_render(book);
    return( book:'render':'nbt' );
  );
);

_shuffle_color() -> (
  return(global_shuffle_colors:rand(length(global_shuffle_colors) - 1));
);

help() -> (
  print(player(), '
A utiltiy used to create command books.

/spellbook <book> command <title> <"command">
/spellbook warps command "Spawn" "/tp @p 0 64 0"
/spellbook warps command "Teleport to Spawn" "/tp @p 1173 28 0"
/spellbook <book> set <page> <line> <"command">
  ');
);




