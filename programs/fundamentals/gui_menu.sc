

__command() -> (
    gui = new_gui_menu(global_Test);
    call_gui_menu(gui, player());
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
global_static_buttons='buttons';

global_Test={
    'inventory_shape'->'generic_3x3',
    'title'->format('db Test GUI menu!'),
    global_static_buttons->{
        0->['red_stained_glass', _(button)->print('Pressed the red button!')],
        4->['green_stained_glass', _(button)->print(str('Clicked with %s button', if(button, 'Right', 'Left')))]
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

    {
        'inventory_shape'->inventory_shape, //shape of the inventory, copied from above
        'title'->gui_screen:'title', //Fancy GUI title
        'on_created'->_(screen, outer(gui_screen))->(// Fiddling with the screen after it's made to add fancy visual bits
            for(gui_screen:global_static_buttons,
                inventory_set(screen, _, 1, gui_screen:global_static_buttons:_:0)
            );
        ),
        'callback'->_(screen, player, action, data, outer(gui_screen), outer(inventory_size))->(//This is where most of the action happens
            if(action=='quick_move', //disabling quick move cos it messes up the GUI, and there's no reason to allow it
                return('cancel')
            );
            
            slot = data:'slot';

            if(action=='pickup', //This is equivalent of clicking (button action)

                if(has(gui_screen:global_static_buttons, slot), //Plain, vanilla button
                    call(gui_screen:global_static_buttons:slot:1, data:'button')
                );
            );

            if(slot<inventory_size,
                'cancel', //preventing the player from tampering with GUI slots
            );
        )
    }
);

call_gui_menu(gui_menu, player)->(
    screen = create_screen(player, gui_menu:'inventory_shape', gui_menu:'title', gui_menu:'callback');
    call(gui_menu:'on_created', screen)
);
