// A in game utility for creating, using, and updating command books (spellbooks)
// You can create a new spell in a book with this command
// `/spellbook <book> set <spell> </command>` 
// ie`/spellbook cat_spells set "spawn cat" /summon cat`
// You can continue adding spells to existing spellbooks. 
// `/spellbook cat_spells set "spawn Tabby" /summon cat ~ ~ ~ {CatType:2}`
// You can give yourself a copy of the spellbook with the spellbook give command,
// `/spellbook cat_spells give`
// Any changes you make to your spells will automatically update your spellbook when opened.
// You can find more info in-game with the help command',
// `/spellbook help <page>',
// Pages: (main), basics, shorthands, customize, commands

global_app_name = system_info('app_name');

__config()->{
  'command_permission' -> 'ops', 
  'commands' -> {
    '' -> ['help', 'main'],
    'help' -> ['help', 'main'],
    'help <helpPage>' -> 'help',
    'list' -> 'list_books',
    '<book> give' -> 'give_book',
    '<book> delete' -> 'delete_book',
    '<book> delete confirm' -> 'delete_book_confirm',
    '<book> set <spell> <command>' -> 'set_command',
    '<book> warp <spell>' -> 'set_warp_at_player',
    '<book> warp <spell> at <location> in <dimension>' -> 'set_warp',
    '<book> forceload <spell> <from> <to>' -> 'set_forceload_at_player',
    '<book> forceload <spell> <from> <to> in <dimension>' -> 'set_forceload',
    '<book> bot <spell> <bot>' -> 'set_bot_at_player',
    '<book> bot <spell> <bot> at <location> in <dimension>' -> 'set_bot',
    '<book> remove <spell>' -> 'delete_command',
    '<book> read' -> ['display_book', 1],
    '<book> read <spellPage>' -> 'display_book',
    '<book> color <spell> <color>' -> 'set_spell_color',
    '<book> tooltip <spell> <tooltip>' -> 'set_spell_tooltip'
  },
  'arguments' -> {
    'command' -> {'type' -> 'text', 'suggest' -> ['/tp @p x y z', '/gamerule doFireTick true']},
    'spell' -> {'type' -> 'string', 'suggest' -> ['"Which Farm Bot"', '"Warp to Spawn"', '"Fire Tick True"']},
    'helpPage' -> {'type' -> 'term', 'options' -> ['main', 'basics', 'shorthands', 'customize', 'commands']},
    'spellPage' -> {'type' -> 'int', 'suggest' -> [1,2,3,5]},
    'tooltip' -> {'type' -> 'text', 'suggest' -> ['Spawn a witch', 'Turn Fire Tick on']},
    'color' -> {'type' -> 'string', 'suggest' -> [
      'dark_red', 'red', 'gold', 'yellow', 'dark_green', 'green', 'aqua', 'dark_aqua', 'dark_blue', 
      'blue', 'light_purple', 'dark_purple', 'white', 'gray', 'dark_gray', 'black', '"#000000"', '"#ffffff"'
    ]},
    'book' -> {'type' -> 'string', 'suggester' -> _(args) -> (
      options = ['bots', 'rules', 'warps'];
      put(options, -1, _get_book_list(), 'extend');
      options;
    )},
    'from' -> {'type' -> 'columnpos'},
    'to' -> {'type' -> 'columnpos'},
    'bot' -> {'type' -> 'term', 'suggest'-> ['Alex', 'Steve']},
    'location' -> {'type' -> 'location'},
    'dimension' -> {'type' -> 'dimension'}
  }
};

_command_example(command) -> (
  'ci /'+global_app_name+' '+command
);
_command_reference(command) -> (
  'ci /'+global_app_name+' '+command
);

global_help_pages = {
  'main'-> [
    'pb \nSpellBook\n', 
    'm A utility used to create command books (spellbooks).\n',
    'm Learn the basics with: ',
    _command_example('help basics\n'),
  ],
  'basics' -> [
    'pb \nSpellBook Basics\n', 
    'm Create a new spell with this command\n',
    _command_reference('<book> set <spell> </command>'),
    'm \nie ', _command_example('cat_spells set "spawn cat" /summon cat'),
    'm \nYou can add more spells to the book\n',
    _command_example('cat_spells set "spawn Tabby" /summon cat ~ ~ ~ {CatType:2}\n'),
    'm You can give yourself a copy of the spellbook with the spellbook give command\n',
    _command_example('cat_spells give\n'),
    'm Any changes you make to the spells will automatically update your spellbook when opened.\n'
  ],
  'shorthands' -> [
    'pb \nSpellbook Shorthands\n',
    'm The spellbook command has many shorthands to make life easier.\n',
    'm \nFor example you can add a warp spell to your current location.\n',
    _command_reference('<book> warp <spell>\n'),
    'm you can also be more specific\n',
    _command_example('transporter warp "Zero" at 0 0 0 in overworld'),
    _command_reference('<book> warp <spell> at <location> in <dimension>\n'),
    'm \nThere is also a player bot shorthand that creates two spells. ',
    'm One to summon the player and the other kill the player\n',
    _command_example('farms bot "Which Hut" Dorothy\n'),
    _command_reference('<book> bot <spell> <botName> at 0 0 0 in <dimension>\n'),
    _command_reference('<book> bot <spell> <botName>\n'),
    'm \nSome farms don\'t require player based mob spawning. ',    
    'm You also have the option to use force loading instead\n',
    'm The forceload shorthand creates one spell to load the chunks and another to unload the chunks.\n',
    _command_example('farms forceload "The Iron Bucket" 32 32 64 64\n'),
    _command_reference('<book> forceload <spell> <from> <to>\n'),
    _command_reference('<book> forceload <spell> <from> <to> in <dimension>\n')
  ],
  'customize'-> [
    'pb \nSpellbook Customization\n',
    'm you can customize the color or the tooltips of spells with the following commands.\n',
    _command_reference('<book> tooltip <spell> <customTooltip>\n'),
    _command_reference('<book> color <spell> <customColor>\n'),
  ],
  'commands' -> [
    'pb \nSpellbook Commands\n',
    _command_reference('<book> set <spell> </command> '),
    'm Add any command to the spellbook\n',
    _command_reference('<book> give '),
    'm Give the player a copy of the book\n',
    _command_reference('<book> read '),
    'm List the spells within a book\n',
    _command_reference('list '),
    'm List all of the spellbooks\n',
    _command_reference('<book> delete '),
    'm Delete the whole spellbook\n',
    _command_reference('<book> remove <spell> '),
    'm Delete a spell\n',
    _command_reference('<book> tooltip <spell> <customTooltip> '),
    'm Customize a spells tooltip\n',
    _command_reference('<book> color <spell> <customColor>\n'),
    'm Customize a spells color\n',
    _command_reference('<book> warp <spell>\n'),
    'm Create a warp spell at your location\n',
    _command_reference('<book> bot <spell> <botName>\n'),
    'm Make a set of spells to summon and kill a player bot\n',
    _command_reference('<book> forceload <spell> <from> <to>\n'),
    'm Make a set of spells to load and unload chunks\n'
  ]
};


// spellbooks tack their own book version and the script version, 
// it updates a book when either of those versions no longer match.
// increment this number when you make changes to spell book rendering.
// Not all changes need to update this.
global_spellbook_version = 6;

// When you make changes to the templates you should increment the script version
global_spell_template = '{"text":"%s","color":"%s","clickEvent":{"action":"run_command","value":"%s"},"hoverEvent":{"action":"show_text","contents":"%s"}},{"text":"\\\\n"}';
global_title_template = '{"text":"%s", "bold":true},{"text":".\\\\n"}';
global_book_template = '{pages:%s, title:"%s",author:"scarpet.spellbook", version:"%s"}';

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
};



// Automagically Update Spell Books in Lecterns  
__on_player_right_clicks_block(p, item_tuple, hand, block, face, hitvec) -> (
  if(block == block('lectern'),
    
    pos = pos(block);
    block_data = block_data(pos);
    book_nbt = get(block_data, 'Book.tag');

    if(_app_is_author(book_nbt),    
      book_save = _read_book(book_nbt:'title');

      if(book_nbt:'version' != _get_version(book_save),
        put(block_data, 'Book.tag', nbt(_render_book_nbt(book_save)));
        without_updates(
          set(pos, block, {}, block_data);
        );
        _print_update_spell_book(p, [book_nbt:'title', book_nbt:'version', _get_version(book_save)])
      );
    );
  );
);

// Automagically update spell books opened in hands.
__on_player_uses_item(p, item, hand) -> (
  if( item:0 == 'written_book' && _app_is_author(item:2),
    
    book_save = _read_book(item:2:'title');
    if(item:2:'version' != _get_version(book_save),
    
      inventory_set(p, p~'selected_slot', item:1, item:0, _render_book_nbt(book_save));
      _print_update_spell_book(p, [item:2:'title', item:2:'version', _get_version(book_save)])
    )
  ) 
);

_print_update_spell_book(p, args) -> (
  _print_message(p, str('Automagically Updated %s Spellbook from %s to %s.', args));
);

_app_is_author(book_nbt) -> (
  return(book_nbt:'author'~'(nimda|scarpet)\\.spellbook');
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

_write_cache(title, book_nbt) -> (
  write_file('cache/'+_sanitize_book_title(title), 'nbt', book_nbt);
);

_read_cache(title) -> (
  return(read_file('cache/'+_sanitize_book_title(title), 'nbt'));
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
help(page) -> (
  p = player();
  print(p, format(global_help_pages:page));
  print(p, format(_display_page_links(page)));

);

_display_page_links(page) -> (
  links = ['p Pages: '];
  for(sort(keys(global_help_pages)),
    if( page == _ ,
      links += 'gi ('+ _ +')';
    ,
      links += 'ci ['+ _ +']';
      links += '!/spellbook help '+ _;
    );
    links += 'g , ';
  );
  delete(links, -1);
  links;
);
    // 'm Pages: ', ...(_display_page_links(['main', 'basics', 'shorthands', 'customize', 'commands'])) 



// Player bot shorthand
set_bot_at_player(book, spell, bot_name) -> (
  p = player();
  set_bot( book, spell, bot_name, map(p~'pos', round(_)), p~'dimension');
);

set_bot(book, spell, bot_name, location, dimension) -> (
  location = join(' ', location);
  _set_commands( book, [{
    'title'-> spell+' +',
    'command'-> str('/player %s spawn at %s facing 1 1 in %s', bot_name, location, dimension ),
    'tooltip'-> str('Spawn %s bot at %s in %s', bot_name, location, dimension)
  },{
    'title'-> spell+' -',
    'command'-> str('/player %s kill', bot_name),
    'tooltip'-> str('Kill %s bot in %s', bot_name, dimension)
  }]);
);

// forceload shorthand
set_forceload_at_player(book, spell, from, to) -> (
  set_forceload(book, spell, from, to, player()~'dimension');
);

set_forceload(book, spell, from, to, dimension) -> (
  from = join(' ', from);
  to = join(' ', to);
  _set_commands( book, [{
    'title'->spell+' +', 
    'command'-> str('/execute in %s run forceload add %s %s', dimension, from, to),
    'tooltip'-> str('forceload chunks from %s to %s in %s', from, to, dimension)
  },{
    'title'-> spell+' -', 
    'command'-> str('/execute in %s run forceload remove %s %s', dimension, from, to),
    'tooltip'-> str('unload chunks from %s to %s in %s', from, to, dimension)
  }]);
);

// Warp Shorthand
set_warp_at_player(book, spell) -> (
  p = player();
  set_warp(book, spell, map(p~'pos', round(_)), p~'dimension');
);

set_warp(book, spell, location, dimension) -> (
  location = join(' ', location);
  _set_commands( book, [{
    'title'-> spell, 
    'command'-> str('/execute as @p in %s run tp %s', dimension, location ),
    'tooltip'-> str('warp to %s in %s', location, dimension)
  }]);
);



list_books() -> (
  p = player();
  display = [];
  
  for( _get_book_list(),
    display += str('m %s', _, -1);
    display += 'g , ';
  );
  
  delete(display, -1);

  _print_title(p, 'Spellbook List');
  print(p, format( display ));
);

delete_book(book) -> (
  print(player(), format(
    str('m Are you sure you want to delete the %s spellbook for good? ', book), 
    'ci [confirm]', str('!/spellbook %s delete confirm', book)
  ));
);

delete_book_confirm(book) -> (
  delete_file('books/'+book, 'json');
  _print_message(player(), str('Burned the %s spellbook to ashes.', book));
);

_get_book_list() -> map( list_files('books/', 'json'), get(split('/',_), -1) );

display_book(book_name, page) -> (
  p = player();
  book = _read_book(book_name);
  spells = _sort_by_title(book:'spells');
  if(length(spells) < 1, 
    _print_message(p,str('The %s book has no spells to read.', book_name));
    return();
  );
  last_page = ceil(length(spells) / global_spells_per_page);
  page = min(max(page, 1), last_page);
  
  print(p, format( 
    _display_title(book_name+' spells '), 
    str('mi page %d/%d', page, last_page)
  ));

  for(slice(spells, (page - 1) * global_spells_per_page, min( length(spells), page * global_spells_per_page)),
    _print_spell_editor(p, _, book_name);
  );
  
);

_print_message(p, message) -> print(p, format('m '+message));

_display_title(text) -> str('pb \n%s', text);

_print_title(p, text) -> print(p, format(str('pb \n%s', text)));

_print_spell_editor(p, spell, book) -> print(p, format(
  [
    'mb ' + spell:'title' + ' ',
    'l [run] ', '!'+spell:'command',
    'q [edit] ', str('?/spellbook %s set "%s" %s', book, spell:'title', spell:'command'),
    'r [remove] ', str('?/spellbook %s remove "%s"', book, spell:'title'),
    'g  ' + (spell:'tooltip' || '[tooltip]'), str('?/spellbook %s tooltip "%s" %s', book, spell:'title', spell:'tooltip' || ''),
    'ci \n' + spell:'command' +' ', '^ci Copy', '&' + spell:'command' 
  ]
));

give_book(name) -> (
  book = _read_book(name);
  p = player();
  _print_message(p, 
    run(str('/give %s written_book%s', query(p, 'command_name'), _render_book_nbt(book))):1
  );
);

delete_command(book_name, spell) -> (
  book = _read_book(book_name);
  if( delete(book:'spells':spell),
    _write_book(book);
    _print_message(player(), str('Removed the "%s" spell from the %s book.', spell, book_name))
  ,
    _print_message(player(), str('Unknown spell "%s" in %s.', spell, book_name))
  );
);



set_spell_color(book_name, spell, color) -> (
  set_spell_item(book_name, spell, 'color', color)
);

set_spell_tooltip(book_name, spell, tooltip) -> (
  set_spell_item(book_name, spell, 'tooltip', tooltip)
);

set_spell_item(book_name, spell, key, value) -> (
  p = player();
  book = _read_book(book_name);
  book:'spells':spell:key = value;
  _print_message(p, str('%s spell "%s" %s set.', book_name, spell, key));
  _print_spell_editor(p, book:'spells':spell, book_name);
  _write_book(book);
);



set_command(book_name, spell, command) -> (
  _set_commands(book_name, [{
    'title'->spell,
    'command'-> command
  }]);
);

_set_commands(book_name, spells) -> (
  p = player();
  book = _read_book(book_name);
  for(spells,
    spell = _:'title';
    book:'spells':spell = _;
    _print_message(p, str('%s spell "%s" set.', book_name, spell) );
    _print_spell_editor(p, _, book_name);
  );
  _write_book(book);
);

// Book Rendering Functions 
_render_single_spell(spell, default_color) -> (
  return(str(global_spell_template,
    spell:'title',
    spell:'color' || default_color,
    spell:'command',
    spell:'tooltip' || spell:'command'
  ));
);

_sort_by_title(spells) -> (
  return( sort_key(values(spells), _:'title') );
);

_render_pages(book) -> (
  spells = _sort_by_title(book:'spells');
  pages = [];
  a = 0;
  l = length(spells);
  while(a<length(spells),50,
    page = [str(global_title_template, book:'title')];
    loop( min(global_spells_per_page,l) ,
      put(page, null, _render_single_spell(spells:a, _shuffle_color(a)));
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

_get_version(book) -> (
  return(str('v%d.%d', global_spellbook_version, book:'vbook'));
);

_render_book_nbt(book) -> (
  v = _get_version(book);
  cache = _read_cache(book:'title');
  if(cache && cache:'version' == v,
    return( cache );
  ,
    render = nbt(str(
        global_book_template, 
        _render_pages(book), 
        book:'title',
        v
    ));
    _write_cache(book:'title', render);
    return( render );
  );
);

_shuffle_color(color_idx) -> (
  return(global_shuffle_colors:color_idx);
);

// To clear the cache while testing.
clear_cache() -> (
  cache_files = list_files('cache/', 'nbt');
  for( cache_files, 
    delete_file(_, 'nbt')
  );
);


// Migrations, invoke the following methods to apply changes in the data structures.

// Move old books from app root into books folder
// run /script in spellbook invoke migrate_books_folder_location()
migrate_books_folder_location() -> (
  old_files = list_files('./', 'json');
  for(old_files, 
    book = read_file(_, 'json');
    write_file('books/'+_, 'json', book);
    delete_file(_, 'json');
  );
  print(str('Migrated [ %s ] books from <app>/ to <app>/books.', join(', ',old_files) ));
);

// Change the spell data structure to allow for custom tooltips and custom colors to be defined.
// run /script in spellbook invoke migrate_spells_data_structure
migrate_spells_data_structure() -> (
  book_files = list_files('books/', 'json');
  for(book_files,
    book = read_file(_, 'json');
    spells = book:'spells';
    for( pairs(book:'spells'),
      title = _:0;
      command = _:1;
      book:'spells':title = {'title'-> title, 'command'-> command};
    );
    write_file('books/'+book:'title', 'json', book);
    print(str('Migrated %s spellbook.', book:'title'));
  );
);

