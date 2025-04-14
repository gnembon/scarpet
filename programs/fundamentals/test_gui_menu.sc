import('gui_menu', 'new_gui_menu', 'call_gui_menu', '_switch_page', '_get_gui_page');

__on_player_swings_hand(player, hand)-> (
    item = player~'holds':0;
    if(hand=='mainhand',
        if(item=='blaze_rod',
            call_gui_menu(global_Test_GUI, player),
            item=='stick',
            call_gui_menu(global_Test_pages_GUI, player)
        )        
    )
);


global_Test={
    'inventory_shape'->'generic_3x3',
    'title'->format('db Test GUI menu!'),
    'static_buttons'->{
        0->['red_stained_glass_pane', _(player, button)->print(player, 'Pressed the red button!')],
        4->['green_stained_glass_pane', _(player, button)->print(player, str('Clicked with %s button', if(button, 'Right', 'Left')))]
    },
    'dynamic_buttons'->{
        1->[ //Blue button to black button
            'blue_stained_glass_pane',
            _(screen, player, button)->inventory_set(screen, 1, 1, if(inventory_get(screen, 1):0=='blue_stained_glass_pane', 'black_stained_glass_pane', 'blue_stained_glass_pane'));
        ],
        
        6->[ //Turns the slot above purple
            ['lime_stained_glass_pane', 'Flicky!'],
            _(screen, player, button)->(
                inventory_set(screen, 3, 1, if(inventory_get(screen, 3)==null, 'purple_stained_glass_pane', 'air'));
            )
        ],
    },
    'storage_slots'->{ //These slots can be used for storage by the player
        8->['stone', 4, null], //This is simply the first item that will be available in the slot, it will subsequently be overwritten by whatever the player places in that slot
        5 //leaving this blank makes the slot blank
    },
    'dynamic_storage_slots'->{ //Whenever the slot is modified, call that function
        2->[[air, 0, null], _(player, screen, slot, item)->(
            print(player, str('Modified slot %s, now holds %s', slot, item))
        )]
    },
    'additional_screen_callback'->_(screen, player, action, data, gui_screen)->if(data:'slot'==3, //Printing all actions in slot 3
        print(player, str('Action: %s, Data: %s', action, data)),
        data:'slot'==7, //Cancelling all action in slot 7
        'cancel'
    )
};

global_Test_pages={
    'inventory_shape'->'generic_9x2',
    'title'->format('db Test GUI with pages!'),
    'main_page_title'->'main_page',
    'pages'->{
        'main_page'->{
            'title'->format('c Test GUI menu main page'),
            'navigation_buttons'->{
                0->['anvil', 'anvil_page'],
                1->['beacon', 'beacon_page'],
                2->['blast_', 'blast__page'],
                3->['brewing_stand', 'brewing_stand_page'],
                4->['cartography_table', 'cartography_table_page'],
                5->['crafting_table', 'crafting_page'],
                6->['enchanting_table', 'enchantment_page'],
                7->['', '_page'],
                8->['grindstone', 'grindstone_page'],
                9->['hopper', 'hopper_page'],
                10->['lectern', 'lectern_page'],
                11->['loom', 'loom_page'],
                12->['emerald', 'merchant_page'],
                13->['shulker_box', 'shulker_box_page'],
                14->['smithing_table', 'smithing_page'],
                15->['smoker', 'smoker_page'],
                16->['stonecutter', 'stonecutter_page'],
            }
        },
        'anvil_page'->{
            'title'->format('c Test GUI menu anvil page'),
            'inventory_shape'->'anvil',
            'navigation_buttons'->{
                1->['air', 'main_page']
            },
            'on_init'->_(screen, player)->print(str('Screen %s, Player %s', screen, player)),
            'storage_slots'->{0, 2}, //Allows player to place item in first slot for renaming, and take modified item out of last slot
            'on_anvil_modify_item'->_(player, screen, item_name, repair_cost)->print(player, str('Renaming item to %s, costing %s levels', item_name, repair_cost))
        },
        'beacon_page'->{
            'title'->format('c Test GUI menu beacon page (hidden)'), //You don't see this title
            'inventory_shape'->'beacon',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'blast_furnace_page'->{
            'title'->format('c Test GUI menu blast_furnace page'),
            'inventory_shape'->'blast_furnace',
            'navigation_buttons'->{
                0->['air', 'main_page']
            },
            'on_select_crafting_recipe'->_(player, screen, recipe, craft_all)->print(player, str('Selected %s recipe, %s to craft all', recipe, if(craft_all, 'tried', 'did not try')))
        },
        'brewing_stand_page'->{
            'title'->format('c Test GUI menu brewing_stand page'),
            'inventory_shape'->'brewing_stand',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'cartography_table_page'->{
            'title'->format('c Test GUI menu cartography_table page'),
            'inventory_shape'->'cartography_table',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'crafting_page'->{
            'title'->format('c Test GUI menu crafting page'),
            'inventory_shape'->'crafting',
            'navigation_buttons'->{
                0->['air', 'main_page']
            },
            'on_select_crafting_recipe'->_(player, screen, recipe, craft_all)->print(player, str('Selected %s recipe, %s to craft all', recipe, if(craft_all, 'tried', 'did not try')))
        },
        'enchantment_page'->{
            'title'->format('c Test GUI menu enchantment page'),
            'inventory_shape'->'enchantment',
            'navigation_buttons'->{
                0->['air', 'main_page']
            },
            'on_init'->_(screen, player)->(
                print('Seed: '+screen_property(screen, 'enchantment_seed'));
                screen_property(screen, 'enchantment_power_1', 3);
                screen_property(screen, 'enchantment_id_1', 3);
                screen_property(screen, 'enchantment_level_1', 3);
                screen_property(screen, 'enchantment_power_2', 10);
                screen_property(screen, 'enchantment_id_2', 0);
                screen_property(screen, 'enchantment_level_2', 2);
                screen_property(screen, 'enchantment_power_3', 6);
                screen_property(screen, 'enchantment_id_3', 7);
                screen_property(screen, 'enchantment_level_3', 1),
            ),
            'on_select_enchantment'->_(screen, player, cost, enchantment_id, level)->print(player, str('Selected enchantment %s level %s, costing %s', enchantment_id, level, cost))
        },
        'furnace_page'->{
            'title'->format('c Test GUI menu furnace page'),
            'inventory_shape'->'furnace',
            'navigation_buttons'->{
                0->['air', 'main_page']
            },
            'on_select_crafting_recipe'->_(player, screen, recipe, craft_all)->print(player, str('Selected %s recipe, %s to craft all', recipe, if(craft_all, 'tried', 'did not try')))
        },
        'grindstone_page'->{
            'title'->format('c Test GUI menu grindstone page'),
            'inventory_shape'->'grindstone',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'hopper_page'->{
            'title'->format('c Test GUI menu hopper page'),
            'inventory_shape'->'hopper',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'lectern_page'->{
            'title'->format('c Test GUI menu lectern page (hidden)'), //You don't see this title
            'inventory_shape'->'lectern',
            'storage_slots'->{ //You can't interact with this slot in a lectern, but this is where the book goes
                0->['written_book', 1, encode_nbt({'title'->'-','author'->'-','pages'->['[{"text":"Hello World"}]','[{"text":"Hello Second Page"}]','[{"text":"Hello Third Page"}]',]})]
            },
            'on_flip_page'->_(player, screen, button, page)->print(player, str('Flipped %s to page %s', if(button==1, 'backwards', 'forwards'), page)),
            'on_take_book'->_(player, screen)->print(player, 'Took the book'),
            
            'additional_screen_callback'->_(screen, player, action, data, gui_screen)->if(data:'button'==3, //Using Take Book button to switch back to main page without taking the book
                _switch_page(gui_screen, _get_gui_page(gui_screen), 'main_page', screen, player);
                'cancel'
            )
        },
        'loom_page'->{
            'title'->format('c Test GUI menu loom page'),
            'inventory_shape'->'loom',
            'navigation_buttons'->{
                2->['air', 'main_page'] //Using banner pattern slot to switch back to main page
            },
            'storage_slots'->{0, 1, 3}, //allowing to put in a banner and dye and take out the output
            'on_select_banner_pattern'->_(player, screen, button, pattern)->print(player, str('Selected pattern %s: %s', button, pattern))
        },
        'merchant_page'->{
            'title'->format('c Test GUI menu merchant page'),
            'inventory_shape'->'merchant',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'shulker_box_page'->{
            'title'->format('c Test GUI menu shulker_box page'),
            'inventory_shape'->'shulker_box',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'smithing_page'->{
            'title'->format('c Test GUI menu smithing page'),
            'inventory_shape'->'smithing',
            'navigation_buttons'->{
                0->['air', 'main_page']
            }
        },
        'smoker_page'->{
            'title'->format('c Test GUI menu smoker page'),
            'inventory_shape'->'smoker',
            'navigation_buttons'->{
                0->['air', 'main_page']
            },
            'on_select_crafting_recipe'->_(player, screen, recipe, craft_all)->print(player, str('Selected %s recipe, %s to craft all', recipe, if(craft_all, 'tried', 'did not try')))
        },
        'stonecutter_page'->{
            'title'->format('c Test GUI menu stonecutter page'),
            'inventory_shape'->'stonecutter',
            'navigation_buttons'->{ //todo add different way to switch back to main page
                //0->['air', 'main_page']
            },
            'storage_slots'->{0, 1},
            'on_select_stonecutting_pattern'->_(player, screen, button, pattern)->print(player, str('Selected pattern %s: %s', button, pattern))
        },
    }
};

global_Test_GUI = new_gui_menu(global_Test);
global_Test_pages_GUI = new_gui_menu(global_Test_pages);
