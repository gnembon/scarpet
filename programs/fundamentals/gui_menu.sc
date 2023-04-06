

__command() -> (
    gui = new_gui_menu(global_Test);
    call_gui_menu(gui, player());
);

test()->(
    screen = create_screen(player(),'generic_9x6',format('db Button Screen'),_(screen, player, action, data) -> (
        //print(str('Action:\nAction: %s\nData: %s',action,data));
        if(action=='quick_move'&&data:'slot'>=54,
            return('cancel')
        );
        if(action=='pickup',
            print(str('Action:\nAction: %s\nData: %s',action,data)); //for testing
        );
        //print(data);
        if(data:'slot'==10,
            'cancel'//prevent tampering with slot 10
        );
    ));
    inventory_set(screen, 10, 1, 'stone')
);

global_inventory_sizes={
    'generic_3x3'->9,
    'generic_9x1'->9,
    'generic_9x2'->18,
    'generic_9x3'->27,
    'generic_9x4'->36,
    'generic_9x5'->45,
    'generic_9x6'->54
};

global_Test={
    'inventory_shape'->'generic_3x3',
    'title'->format('db Test GUI menu!'),
    'buttons'->{
        0->['green_stained_glass', _(button)->print(str('Clicked with %s button', if(button, 'Right', 'Left')))]
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
            for(gui_screen:'buttons',
                inventory_set(screen, _, 1, gui_screen:'buttons':_:0)
            );
        ),
        'callback'->_(screen, player, action, data, outer(gui_screen), outer(inventory_size))->(//This is where most of the action happens
            if(action=='quick_move', //disabling quick move cos it messes up the GUI, and there's no reason to allow it
                return('cancel')
            );
            
            slot = data:'slot';

            if(has(gui_screen:'buttons', slot) && action=='pickup', //This is equivalent of clicking (button action)
                call(gui_screen:'buttons':slot:1, data:'button')
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
