// A in game utility for creating, using, and updating command books (spellbooks)

__config()->{
  'command_permission' -> 'ops', 
  'commands' -> {
    '' -> 'help',
    'help' -> 'help',
    'list' -> 'list_books',
    '<book> give' -> 'give_book',
    '<book> update' -> 'give_book',
    '<book> set <title> <command>' -> 'set_command',
    '<book> warp <title>' -> 'set_warp_at_player',
    '<book> warp <title> at <location> in <dimension>' -> 'set_warp',
    '<book> forceload <title> <from> <to>' -> 'set_forceload_at_player',
    '<book> forceload <title> <from> <to> in <dimension>' -> 'set_forceload',
    '<book> remove <title>' -> 'delete_command',
    '<book> read' -> 'display_book'
  },
  'arguments' -> {
    'command' -> {'type' -> 'text', 'suggest' -> ['bar', 'tp @p x y z', 'gamerule doFireTick true']},
    'title' -> {'type' -> 'string', 'suggest' -> ['Foo', '"Warp to Spawn"', '"Fire Tick: true"']},
    'tooltip' -> {'type' -> 'string', 'suggest' -> ['"at x y z"', '"Fire Tick true"']},
    'book' -> {'type' -> 'string', 'suggest' -> ['bots', 'warps', 'zones', 'farms', 'rules']},
    'from' -> {'type' -> 'columnpos'},
    'to' -> {'type' -> 'columnpos'},
    'location' -> {'type' -> 'location'},
    'dimension' -> {'type' -> 'dimension'},
  }
};


// spellbooks tack their own book version and the script version, 
// it updates a book when either of those versions no longer match.
// increment this number when you make changes to spell book rendering.
// Not all changes need to update this.
global_spellbook_version = 5;

// When you make changes to the templates you should increment the script version
global_spell_template = '{"text":"%s","color":"%s","clickEvent":{"action":"run_command","value":"%s"},"hoverEvent":{"action":"show_text","contents":"%s"}},{"text":"\\\\n"}';
global_title_template = '{"text":"%s", "bold":true},{"text":".\\\\n"}';
global_book_template = '{pages:%s, title:"%s",author:"nimda.spellbook.%s"}';

// Pick a spell color at random.
global_shuffle_colors = [
  'dark_red',
  'gold',
  'dark_green',
  'dark_aqua',
  'dark_blue',
  'dark_purple'
];

global_spells_per_page = 6;

// Default Book Data
global_base_book_data = {
  'vbook' -> 0,
  'vscript' -> global_spellbook_version,
  'title' -> '',
  'spells' -> {},
  'render' -> {
    'v' -> 'v0.0',
    'nbt' -> ''
  }
};


// Automagically Update Spell Books in Lecterns  
__on_player_right_clicks_block(p, item_tuple, hand, block, face, hitvec) -> (
  if(block == block('lectern'),
    pos = pos(block);
    block_data = block_data(pos);
    book_nbt = get(block_data, 'Book.tag');
    if(book_nbt:'author'~'nimda\\.spellbook',    
      book_save = _read_book(book_nbt:'title');
      v = book_nbt:'author'~'v\\d+\\.\\d+';
      if(v != _render_version(book_save),
        put(block_data, 'Book.tag', nbt(_render_book_nbt(book_save)));
        without_updates(
          set(pos, block, {}, block_data);
        );
        print(p, str('Automagically Updated %s Spell Book from %s to %s.', book_nbt:'title', v, book_save:'render':'v'));
      );
    );
  );
);

// Automagically update spell books opened in hands.
__on_player_uses_item(p, item, hand) -> (
  // If the item is a spellbook
  if( item:0 == 'written_book' && item:2:'author'~'nimda\\.spellbook', 
    book_save = _read_book(item:2:'title');
    v = item:2:'author'~'v\\d+\\.\\d+';
    if(v != _render_version(book_save),
      slot = if(hand=='mainhand', player~'selected_slot', -1);
      inventory_set(p, p~'selected_slot', item:1, item:0, _render_book_nbt(book_save));
      print(p, str('Automagically Updated %s Spell Book from %s to %s.', item:2:'title', v, book_save:'render':'v'));
    )
  ) 
);


// Standardize reading and writing functions
_read_book(name) -> (
  file = read_file('books/'+name, 'json');
  if( file, 
    return(file);
  ,
    file = global_base_book_data;
    file:'title' = name;
    return(file);
  );
);

_write_book(book) -> (
  book:'vscript' = global_spellbook_version;
  book:'vbook' = book:'vbook' + 1;

  write_file('books/'+_sanitize_book_title(book:'title'), 'json', book);
);

_write_render(book) -> (
  write_file('books/'+_sanitize_book_title(book:'title'), 'json', book);
);

// Because part of the in game book nbt is used to write a file 
// remove any weird characters from the spellbook title.
// This is probably a non-issue because 
// scarpet seems to handles file security well, but just in case.
// This would present a issue for non-roman characters. 
_sanitize_book_title(title) -> (
  return(replace(title, '[^A-z0-9_-]', '_'));
);

// Command Methods
help() -> (
  print(player(), '
A utility used to create command books (spell books).

Add or override a spell in a book.
  /spellbook <book> set <title> <"command">
  /spellbook farms set "spawn slime farm bot" execute in overworld run player SlimeBot spawn at -50.50 82 24
  /spellbook farms set "kill slime farm bot" player SlimeBot kill
  /spellbook farms give

Give the player a spellbook.
  /spellbook <book> give

Remove a spell.
  /spellbook <book> remove <spell>

List all spells in a book.
  /spellbook <book> read

List all the spellbooks
  /spellbook list

');
);


set_forceload_at_player(book, title, from, to) -> (
  set_forceload(book, title, from, to, player()~'dimension');
);

set_forceload(book, title, from, to, dimension) -> (
  set_command( book, title+' +', str('execute in %s run forceload add %s %s', dimension, join(' ', from), join(' ', to)));
  set_command( book, title+' -', str('execute in %s run forceload remove %s %s', dimension, join(' ', from), join(' ', to)));
);

set_warp_at_player(book, title) -> (
  p = player();
  set_warp(book, title, p~'pos', p~'dimension');
);

set_warp(book, title, location, dimension) -> (
  set_command( book, title, str('execute as @p in %s run tp %s', dimension, join(' ', location)));
);

list_books() -> (
  books = list_files('books/', 'json');
  p = player();
  for(books,
    print(p,str('[ %s ]', get(split('/',_), -1) ));
  );
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
    run(str('/give %s written_book%s', query(p, 'command_name'), _render_book_nbt(book))):1
  );
);

delete_command(book_name, spell) -> (
  book = _read_book(book_name);
  if( delete(book:'spells':spell),
    _write_book(book);
    print(player(), str('Removed the [ %s ] spell.', spell))
  ,
    print(player(), str('Unknown spell [ %s ].', spell))
  );
);

set_command(book_name, title, command) -> (
  p = player();
  book = _read_book(book_name);
  book:'spells':title = '/' + command;
  print(p, str('%s spell set: [ %s ]( %s ).', book_name, title, command));
  _write_book(book);
);




// Book Rendering Functions 
_render_single_spell(title, command, color) -> (
  return(str(global_spell_template,title,color,command, command));
);

_render_pages(book) -> (
  spells = sort_key(pairs(book:'spells'), _:0 );
  pages = [];
  a = 0;
  l = length(spells);
  while(a<length(spells),50,
    page = [str(global_title_template, book:'title')];
    loop( min(global_spells_per_page,l) ,
      put(page, null, _render_single_spell(spells:a:0, spells:a:1, _shuffle_color(a)));
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
  return(str('v%d.%d', global_spellbook_version, book:'vbook'));
);

_render_book_nbt(book) -> (
  v = _render_version(book);
  if(book:'render' && book:'render':'v' == v,
    return( book:'render':'nbt' );
  ,
    book:'render' = {
      'v' -> v,
      'nbt' -> str(
        global_book_template, 
        _render_pages(book), 
        book:'title', 
        v
      )
    };
    _write_render(book);
    return( book:'render':'nbt' );
  );
);

_shuffle_color(color_idx) -> (
  return(global_shuffle_colors:color_idx);
);



// Move old books from app root into books folder
// run /script in spellbook invoke migrate_books_folder_location
migrate_books_folder_location() -> (
  old_files = list_files('./', 'json');
  for(old_files, 
    book = read_file(_, 'json');
    write_file('books/'+_, 'json', book);
    delete_file(_, 'json');
  );
  return(str('Migrated [ %s ] books from <app>/ to <app>/books.', join(', ',old_files) ));
);


