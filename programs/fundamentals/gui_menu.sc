
__on_player_swings_hand(player, hand)-> (
    item = player~'holds':0;
    if(hand=='mainhand',
        if(item=='brewing_stand',
            call_gui_menu(global_Test_GUI, player),
            item=='stick',
            call_gui_menu(global_Test_pages_GUI, player)
        )        
    )
);
 
//Config

global_inventory_sizes={
    'generic_3x3'->9,
    'generic_9x1'->9,
    'generic_9x2'->18,
    'generic_9x3'->27,
    'generic_9x4'->36,
    'generic_9x5'->45,
    'generic_9x6'->54
};

//Certain names are subject to change, so instead I'll store them in global variables while I'm still fiddling with exact nomenclature
global_static_buttons='static_buttons';
global_dynamic_buttons='dynamic_buttons';
global_storage_slots='storage_slots';
global_pages='pages';
global_main_page='main_page_title';
global_current_page='current_page';
global_page_switcher='change_page_buttons';

global_Test={
    'inventory_shape'->'generic_3x3',
    'title'->format('db Test GUI menu!'),
    global_static_buttons->{
        0->['red_stained_glass_pane', _(player, button)->print(player, 'Pressed the red button!')],
        4->['green_stained_glass_pane', _(player, button)->print(player, str('Clicked with %s button', if(button, 'Right', 'Left')))]
    },
    global_dynamic_buttons->{
        1->[ //Blue button to black button
            'blue_stained_glass_pane',
            _(screen, player, button)->inventory_set(screen, 1, 1, if(inventory_get(screen, 1):0=='blue_stained_glass_pane', 'black_stained_glass_pane', 'blue_stained_glass_pane'));
        ],
        
        6->[ //Turns the slot above purple
            'lime_stained_glass_pane',
            _(screen, player, button)->(
                inventory_set(screen, 3, 1, if(inventory_get(screen, 3)==null, 'purple_stained_glass_pane', 'air'));
            )
        ],
    },
    global_storage_slots->{ //These slots can be used for storage by the player
        8->['stone', 4, null], //This is simply the first item that will be available in the slot, it will subsequently be overwritten by whatever the player places in that slot
        5, 2 //leaving this blank makes the slot blank
    }
};

global_Test_pages={
    'inventory_shape'->'generic_3x3',
    'title'->format('db Test GUI with pages!'),
    global_main_page->'main_page',
    global_current_page->null,
    global_pages->{
        'main_page'->{
            global_static_buttons->{
                0->['red_stained_glass_pane', _(player, button)->print(player, 'Pressed the red button!')],
                4->['green_stained_glass_pane', _(player, button)->print(player, str('Clicked with %s button', if(button, 'Right', 'Left')))]
            },
            global_storage_slots->{ //These slots can be used for storage by the player
                8->['stone', 4, null], //This is simply the first item that will be available in the slot, it will subsequently be overwritten by whatever the player places in that slot
                5, 2 //leaving this blank makes the slot blank
            },
            global_page_switcher->{
                3->['cyan_stained_glass', 'second_page']
            }
        },
        'second_page'->{
            global_dynamic_buttons->{
                1->[ //Blue button to black button
                    'blue_stained_glass_pane',
                    _(screen, player, button)->inventory_set(screen, 1, 1, if(inventory_get(screen, 1):0=='blue_stained_glass_pane', 'black_stained_glass_pane', 'blue_stained_glass_pane'));
                ],

                6->[ //Turns the slot above purple
                    'lime_stained_glass_pane',
                    _(screen, player, button)->(
                        inventory_set(screen, 3, 1, if(inventory_get(screen, 3)==null, 'purple_stained_glass_pane', 'air'));
                    )
                ],
            },
            global_page_switcher->{
                4->['cyan_stained_glass', 'main_page']
            }
        }
    }
};

new_gui_menu(gui_screen)->( //Stores GUI data in intermediary map form, so the programmer can call them at any time with call_gui_menu() function
    if(type(gui_screen)!='map' || !has(gui_screen, 'inventory_shape'),
        throw('Invalid gui creation: '+gui_screen)
    );

    inventory_shape = gui_screen:'inventory_shape';

    inventory_size = global_inventory_sizes:inventory_shape;

    if(inventory_size==0,
        throw('Invalid gui creation: Must be one of '+keys(global_inventory_sizes)+', not '+inventory_shape)
    );

    if(has(gui_screen, global_pages) && !has(gui_screen:global_pages, gui_screen:global_main_page),
        throw('Tried to create a GUI Menu, but did not find a main page with the name '+gui_screen:global_main_page)
    );

    gui_screen:global_current_page = gui_screen:global_main_page;

    {
        'inventory_shape'->inventory_shape, //shape of the inventory, copied from above
        'title'->gui_screen:'title', //Fancy GUI title
        'on_created'->_(screen, outer(gui_screen))->__create_gui_screen(screen, gui_screen),
        'callback'->_(screen, player, action, data, outer(gui_screen), outer(inventory_size))->(
            gui_page=if(has(gui_screen, global_pages),
                gui_screen:global_pages:(gui_screen:global_current_page),
                gui_screen //If there is no page functionality, just use the screen map as the page
            );

            slot = data:'slot'; //Grabbing slot, this is the focus of the action

            if(action=='pickup', //This is equivalent of clicking (button action)
                if(has(gui_page:global_static_buttons, slot), //Plain, vanilla button
                    call(gui_page:global_static_buttons:slot:1, player, data:'button'),
                    has(gui_page:global_dynamic_buttons, slot), //A more exciting button
                    call(gui_page:global_dynamic_buttons:slot:1, screen, player, data:'button'),
                    has(gui_page:global_page_switcher, slot), //Switching screens
                    gui_screen:global_current_page = gui_page:global_page_switcher:slot:1;
                    for(gui_page:global_storage_slots, //Saving storage slots when switching screens
                        gui_page:global_storage_slots:_ = inventory_get(screen, _);
                    );
                    loop(inventory_size, //Clearing inventory before switching
                        inventory_set(screen, _, 0)
                    );
                    __create_gui_screen(screen, gui_screen)
                );
            );

            //Saving items in storage slots when closing
            if(action=='close',
                for(gui_page:global_storage_slots,
                    gui_page:global_storage_slots:_ = inventory_get(screen, _);
                );
            );

            //Disabling quick move cos it messes up the GUI, and there's no reason to allow it
            //Also preventing the player from tampering with button slots
            //Unless the slot is marked as a storage slot, in which case we allow it
            if((action=='quick_move'||slot<inventory_size)&&!has(gui_page:global_storage_slots,slot),
                'cancel'
            );
        ),
    }
);

call_gui_menu(gui_menu, player)->( //Opens the screen to the player, returns screen for further manipulation
    screen = create_screen(player, gui_menu:'inventory_shape', gui_menu:'title', gui_menu:'callback');
    call(gui_menu:'on_created', screen);
    screen
);

__create_gui_screen(screen, gui_screen)->(// Fiddling with the screen right after it's made to add fancy visual bits
    gui_page=if(has(gui_screen, global_pages),
        gui_screen:global_pages:(gui_screen:global_current_page),
        gui_screen //If there is no page functionality, just use the screen map as the page
    );
    for(gui_page:global_static_buttons,
        inventory_set(screen, _, 1, gui_page:global_static_buttons:_:0)
    );
    for(gui_page:global_dynamic_buttons,
        inventory_set(screen, _, 1, gui_page:global_dynamic_buttons:_:0)
    );
    for(gui_page:global_storage_slots,
        [item, count, nbt] = gui_page:global_storage_slots:_ || ['air', 0, null];
        inventory_set(screen, _, count, item, nbt)
    );
    for(gui_page:global_page_switcher,
        inventory_set(screen, _, 1, gui_page:global_page_switcher:_:0)
    );
);


global_Test_GUI = new_gui_menu(global_Test);
global_Test_pages_GUI = new_gui_menu(global_Test_pages);
