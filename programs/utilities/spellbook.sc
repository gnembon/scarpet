// A utility for creating command books

__config()->{
  'command_permission' -> 'ops', 
  'commands' -> {
    '' -> 'help',
    '<book> give' -> 'give_book',
    '<book> update' -> 'give_book',
    '<book> command <title> <command>' -> 'set_command',
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

// title, color, command
global_spell_template = '{"text":"[%s]","color":"%s","clickEvent":{"action":"run_command","value":"%s"}},{"text":"\\\\n","color":"reset"}';

global_spells_per_page = 6;

global_shuffle_colors = [
  'dark_red',
  'red',
  'gold',
  'yellow',
  'dark_green',
  'green',
  'aqua',
  'dark_aqua',
  'dark_blue',
  'blue',
  'light_purple',
  'dark_purple'
];

// pages: strings/array/nbt, title, author
global_book_template = '{pages:%s, title:"%s",author:"%s"}';

global_base_book_data = {
  'vbook' -> 0,
  'vscript' -> '0.1',
  'author' -> 'server',
  'subject' -> '',
  'title' -> '',
  'spells' -> {}
};


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

_write_book(name, book) -> (
  book:'vbook' = book:'vbook' + 1;
  write_file(name, 'json', book);
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
  print(p, 
    run(str('/give %s written_book%s', query(p, 'command_name'), _render_book_nbt(book))):1;
  );
);

set_command(book_name, title, command) -> (
  p = player();
  book = _read_book(book_name);
  book:'spells':title = command;
  print(p, str('%s spell set: [ %s ]( %s ).', book_name, title, command));
  _write_book(book_name, book);
);

_render_single_spell(title, command, color) -> (
  return(str(global_spell_template,title,color,command));
);

_render_pages(spells) -> (
  pages = [];
  a = 0;
  l = length(spells);
  while(a<length(spells),50,
    page = [];
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

_render_book_nbt(book_data) -> (
  return(str(global_book_template, _render_pages(pairs(book_data:'spells')), book_data:'title', book_data:'author'));
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


