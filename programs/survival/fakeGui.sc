// 原作者: Scarpet
// 修改: 猴子
// 建議: 企鵝
// 翻譯: xy

__config() -> {
    'scope' -> 'global',
    'stay_loaded' -> true,
    'requires' -> {
        'carpet' -> '>=1.4.57'
    },
    'commands' -> {
        '' -> _() -> print(i18n(player(), 'usage')),
        '<player>' -> _(fakeplayer) -> (
            fakeplayer = player(fakeplayer);
            if (
                fakeplayer~'player_type' == 'fake' && player() != fakeplayer,
                __page('menu', player(), fakeplayer),
                display_title(player(), 'actionbar', '§4§L'+i18n(player(), 'not_fake')),
            );
        ),
    },
    'arguments' -> {
        'player' -> {
            'type' -> 'players',
            'single' -> true,
            'suggest' -> filter(player('all'), _~'player_type' == 'fake'),
        }
    },
};

global_languages = {
    'zh_tw' -> {
        'usage' -> '\n如何使用:\n    右鍵點擊假人\n或是:\n    執行指令 /fakegui <player>',
        'click_once' -> '點擊一次',
        'timing_click' -> '間隔點擊',
        'keep_clicking' -> '持續點擊',
        'menu' -> '菜單',
        'who_menu' -> ' 的菜單',
        'left_click' -> '左鍵點擊',
        'right_click' -> '右鍵點擊',
        'backpack' -> '背包',
        'switch_off-hand_items' -> '副手切換',
        'jump' -> '跳',
        'stop' -> '停止假人動作',
        'remove' -> '移除假人',
        'not_fake' -> '他/它 不是假人',
        'Toolbar' -> '工具欄',
        'equipment_bar' -> '裝備欄',
        'attack_mode' -> '攻擊模式',
        'use_mode' -> '使用模式',
        'set_interval' -> '設定間隔',
        'add' -> '添加',
        'reduce' -> '減少',
    },
    'en_us' -> {
        'usage' -> '\nUsage:\n    Right click on the fake player\nor:\n    Execute command /fakegui <player>',
        'click_once' -> 'Click once',
        'timing_click' -> 'Timeing click',
        'keep_clicking' -> 'Keeping click',
        'menu' -> 'Menu',
        'who_menu' -> '\'s menu',
        'left_click' -> 'Left click',
        'right_click' -> 'Right click',
        'backpack' -> 'Backpack',
        'switch_off-hand_items' -> 'Switch off-hand items',
        'jump' -> 'Jump',
        'stop' -> 'Stop the Fake player',
        'remove' -> 'remove the Fake player',
        'not_fake' -> 'This player is not the Fake player.',
        'Toolbar' -> 'Toolbar',
        'equipment_bar' -> 'Equipment bar',
        'attack_mode' -> 'Attack mode',
        'use_mode' -> 'Use mode',
        'set_interval' -> 'Set interval',
        'add' -> 'Add',
        'reduce' -> 'Reduce',
    },
    'zh_cn' -> {
        'usage' -> '\n如何使用:\n    右键点击假人\n或是:\n    执行指令 /fakegui <player>',
        'click_once' -> '点击一次',
        'timing_click' -> '间隔点击',
        'keep_clicking' -> '持续点击',
        'menu' -> '菜单',
        'who_menu' -> ' 的菜单',
        'left_click' -> '左键点击',
        'right_click' -> '右键点击',
        'backpack' -> '背包',
        'switch_off-hand_items' -> '副手切换',
        'jump' -> '跳',
        'stop' -> '停止假人动作',
        'remove' -> '移除假人',
        'not_fake' -> '他/它 不是假人',
        'Toolbar' -> '工具栏',
        'equipment_bar' -> '装备栏',
        'attack_mode' -> '攻击模式',
        'use_mode' -> '使用模式',
        'set_interval' -> '设定间隔',
        'add' -> '添加',
        'reduce' -> '减少',
    },
};

global_slotmap = [[-1,7],[-2,0],[-3,1],[-4,2],[-5,3]];
global_fakeplayers_selected_slot = m();
global_fakeplayersscreen = m();
global_counts = m();
global_models(player) -> l(
    nbt('{ name: \'once\', title: \'§f§'+i18n(player, 'click_once')+'\', item: \'minecraft:minecart\'}'),
    nbt('{ name: \'continuous\', title: \'§f§L'+i18n(player, 'keep_clicking')+'\', item: \'minecraft:apple\'}'),
    nbt('{ name: \'interval\', title: \'§f§L'+i18n(player, 'timing_click')+'\', item: \'minecraft:repeater\'}'),
);

__page(type_, creativeplayer, fakeplayer) -> (
    if (
        // 菜單
        type_ == 'menu', (
            screen = create_screen(creativeplayer, 'generic_9x3', fakeplayer~'display_name'+i18n(creativeplayer, 'who_menu'), _(screen, player, action, data, outer(fakeplayer)) -> (
                slot = data:'slot';
                if(
                    action == 'close', (
                        global_fakeplayersscreen:fakeplayer = null;
                        drop_item(screen, -1);
                    ),
                    action == 'pickup', 
                    if (
                        slot > 9*3-1, null,
                        (
                            newPage = command = null;

                            if (
                                slot == 16, command = 'kill',
                                slot == 15, command = 'stop',
                                slot == 14, command = 'jump',
                                slot == 13, command = 'swapHands'
                            );

                            if (
                                command != null, (
                                    global_fakeplayersscreen:fakeplayer = null;
                                    run('player ' + (fakeplayer~'command_name') + ' ' + command);
                                    soundDell(player);
                                    close_screen(screen);
                                ),
                                slot == 12, newPage = 'bag',
                                slot == 11, newPage = 'use',
                                slot == 10, newPage = 'attack',
                            );

                            if (newPage != null, (
                                soundDell(player);
                                close_screen(screen);
                                __page(newPage, player, fakeplayer);
                            ));
                            return('cancel'),
                        ),
                    ),
                );
            ));
            global_fakeplayersscreen:fakeplayer = [screen, models];

            for (l(
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'left_click')+'\', item: \'minecraft:wooden_sword\'}'),
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'right_click')+'\', item: \'minecraft:cooked_porkchop\'}'),
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'backpack')+'\', item: \'minecraft:chest\'}'),
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'switch_off-hand_items')+'\', item: \'minecraft:magenta_glazed_terracotta\'}'),
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'jump')+'\', item: \'minecraft:rabbit_foot\'}'),
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'stop')+'\', item: \'minecraft:barrier\'}'),
                nbt('{ title: \'§f§L'+i18n(creativeplayer, 'remove')+'\', item: \'minecraft:tnt\'}'),
            ), if (_ != null, inventory_set(screen, _i+9+1, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))));
            setAir(screen);
        ),
        // 背包菜單
        type_ == 'bag', (
            models = [
                nbt('{ slot: 0, item: \'white_shulker_box\', title: \''+i18n(creativeplayer, 'backpack')+'\' }'),
                nbt('{ slot: 1, item: \'minecraft:wooden_pickaxe\', title: \''+i18n(creativeplayer, 'toolbar')+'\' }'),
                nbt('{ slot: 2, item: \'minecraft:leather_chestplate\', title: \''+i18n(creativeplayer, 'equipment_bar')+'\' }'),
            ];
            screen = create_screen(creativeplayer, 'generic_9x3', i18n(creativeplayer, 'backpack')+i18n(creativeplayer, 'menu'), _(screen, player, action, data, outer(fakeplayer)) -> (
                if (
                    action == 'close', (
                        global_fakeplayersscreen:fakeplayer = null;
                        drop_item(screen, -1);
                    ), 
                    action == 'pickup', (
                        slot = data:'slot';
                        if (
                            slot > 9*3-1, null,
                            // 背包
                            slot == 11, (
                                close_screen(screen);
                                models = map(range(27), [_+9, _+9]);
                                screen = create_screen(player, 'generic_9x4', i18n(creativeplayer, 'backpack'), _(screen, player, action, data, outer(fakeplayer), outer(models)) -> (
                                    slot = data:'slot';
                                    if (
                                        action == 'close', (
                                            global_fakeplayersscreen:fakeplayer = null;
                                            drop_item(screen, -1);
                                        ),
                                        slot >= 0 && slot < 9, return('cancel'),
                                        action == 'slot_update', if (
                                            slot >= 9 && slot <= 35, setPlayerInventory(models, screen, fakeplayer),
                                            slot >= 36 && slot <= 62, setScreenInventory(models, screen, fakeplayer),
                                        );
                                    );
                                ));
                                global_fakeplayersscreen:fakeplayer = [screen, models];
                                setScreenInventory(models, screen, fakeplayer);
                                inventory_set(screen, 4, 1, 'minecraft:player_head', nbt('{SkullOwner:"'+fakeplayer+'",display:{Name:\'"'+fakeplayer~'display_name'+'"\'},HideFlags:3}'));
                                setAir(screen, 9*1);
                            ),
                            // 工具欄
                            slot == 13, (
                                close_screen(screen);
                                models = map(range(9), [_, _+9]);
                                screen = create_screen(player, 'generic_9x2', i18n(creativeplayer, 'toolbar'), _(screen, player, action, data, outer(fakeplayer), outer(models)) -> (
                                    slot = data:'slot';
                                    if (
                                        action == 'close', (
                                            global_fakeplayersscreen:fakeplayer = null;
                                            drop_item(screen, -1);
                                        ),
                                        action == 'slot_update', if (
                                            slot >= 9 && slot < 18, setPlayerInventory(models, screen, fakeplayer),
                                            slot >= 45 && slot <= 53, setScreenInventory(models, screen, fakeplayer)
                                        ),
                                        action == 'pickup' && slot >= 0 && slot < 9, (
                                            setSelected(screen, fakeplayer, slot);
                                            return('cancel');
                                        ),
                                    );
                                ));
                                global_fakeplayersscreen:fakeplayer = [screen, models, 'toolbar'];

                                setScreenInventory(models, screen, fakeplayer);
                                for (range(9), inventory_set(screen, _, 1, 'minecraft:structure_void', null));
                                global_fakeplayers_selected_slot:fakeplayer = -1;
                            ),
                            // 裝備欄
                            slot == 15, (
                                soundDell(player);
                                close_screen(screen);
                                models = [
                                    [40, 1, nbt('{ item: \'minecraft:shield\' }')],
                                    [39, 4, nbt('{ item: \'minecraft:leather_helmet\' }')],
                                    [38, 5, nbt('{ item: \'minecraft:leather_chestplate\' }')],
                                    [37, 6, nbt('{ item: \'minecraft:leather_leggings\' }')],
                                    [36, 7, nbt('{ item: \'minecraft:leather_boots\' }')]
                                ];
                                screen = create_screen(player, 'generic_9x2', i18n(player, 'equipment_bar'), _(screen, player, action, data, outer(fakeplayer), outer(models)) -> (
                                    slot = data:'slot';
                                    if (
                                        action == 'close', (
                                            global_fakeplayersscreen:fakeplayer = null;
                                            drop_item(screen, -1);
                                        ),
                                        slot >= 0 && slot <= 9*2 && !first(map(models, _:1), _ == slot), return('cancel'),
                                        action == 'slot_update', if (
                                            first(map(models, _:1), _ == slot), setPlayerInventory(models, screen, fakeplayer),
                                            slot >= 36 && slot <= 40, setScreenInventory(models, screen, fakeplayer),
                                        );
                                    );
                                ));
                                global_fakeplayersscreen:fakeplayer = [screen, models];

                                setScreenInventory(models, screen, fakeplayer);
                                for (models, inventory_set(screen, (_:1)+9, 1, (_:2):'item', null));

                                fromListSetAir(screen, filter(range(2*9), n=_;if (first(map(models, _:1), _ == n) == null, true, false)));
                            )
                        );
                    ),
                );
                return('cancel');
            ));
            global_fakeplayersscreen:fakeplayer = [screen, models];

            for (models, inventory_set(screen, _:'slot'*2+9+2, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}')));
            setAir(screen, 9*3);
        ),
        // 使用
        type_ == 'use', (
            screen = create_screen(creativeplayer, 'generic_9x3', i18n(creativeplayer, 'use_mode'), _(screen, player, action, data, outer(fakeplayer)) -> (
                slot = data:'slot';
                if (
                    action == 'close', (
                        global_fakeplayersscreen:fakeplayer = null;
                        drop_item(screen, -1);
                    ),
                    action == 'pickup', if (
                        slot > 9*3-1, null,
                        getClickType(slot, screen, player, 'player ' + (fakeplayer~'command_name') + ' use '),
                    ),
                );
                return('cancel');
            ));
            global_fakeplayersscreen:fakeplayer = [screen, models];

            for (global_models(creativeplayer), if (_ != null, inventory_set(screen, _i*2+9+2, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))));
            setAir(screen);
        ),
        // 攻擊
        type_ == 'attack', (
            screen = create_screen(creativeplayer, 'generic_9x3', i18n(creativeplayer, 'attack_mode'), _(screen, player, action, data, outer(fakeplayer)) -> (
                slot = data:'slot';
                if(
                    action == 'close', (
                        global_fakeplayersscreen:fakeplayer = null;
                        drop_item(screen, -1);
                    ),
                    action == 'pickup', if (
                        slot > 9*3-1, null,
                        getClickType(slot, screen, player, 'player ' + (fakeplayer~'command_name') + ' attack ');
                    ),
                );
                return('cancel');
            ));
            global_fakeplayersscreen:fakeplayer = [screen, models];

            for (global_models(creativeplayer), if (_ != null, inventory_set(screen, _i*2+9+2, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))));
            setAir(screen);
        ),
    );
);
getClickType(slot, screen, creativeplayer, base_cmd) -> if (
    slot < 11 || slot > 15, null, (
        type_ = null;
        soundDell(creativeplayer);
        if (
            slot == 11, type_ = 'once',
            slot == 13, type_ = 'continuous',
            slot == 15, speedPage(creativeplayer, base_cmd),
        );
        if (type_ != null, (
            run(base_cmd + type_);
            close_screen(screen);
        ));
    ),
);
speedPage(creativeplayer, base_cmd) -> (
    global_counts:creativeplayer = 10;
    models = l(
        nbt('{ title: \'§f§L'+i18n(creativeplayer, 'add')+' 10\', slot: 9, add: +10 }'),
        nbt('{ title: \'§f§L'+i18n(creativeplayer, 'add')+' 1\', slot: 11, add: +1}'),
        nbt('{ title: \''+global_counts:creativeplayer+'\', slot: 13 }'),
        nbt('{ title: \'§f§L'+i18n(creativeplayer, 'reduce')+' 1\', slot: 15, add: -1 }'),
        nbt('{ title: \'§f§L'+i18n(creativeplayer, 'reduce')+' 10\', slot: 17, add: -10 }'),
        nbt('{ title: \'§e'+i18n(creativeplayer, 'done')+'\', slot: 22, item: \'minecraft:redstone_lamp\' }'),
    );
    screen = create_screen(creativeplayer, 'generic_9x3', i18n(creativeplayer, 'set_interval'), _(screen, player, action, data, outer(models), outer(base_cmd)) -> (
        tick = global_counts:player;
        if (
            action == 'close', (
                global_fakeplayersscreen:fakeplayer = null;
                drop_item(screen, -1);
            ),
            action == 'pickup', (
                slot = data:'slot';
                if (
                    slot > 9*3-1, null, (
                        for (filter(models, _:'add'), if (tick+_:'add' < 1e10-1 && tick+_:'add' > 0,
                            inventory_set(screen, _:'slot', 1, 'minecraft:structure_void', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'));
                            if (slot == _:'slot', tick += _:'add'),
                            inventory_set(screen, _:'slot', 1, 'minecraft:barrier', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'));
                        ));

                        if (
                            slot == 22, (
                                soundDell(player);
                                run(base_cmd + 'interval ' + tick);
                                close_screen(screen);
                            ), tick != global_counts:player, soundDell(player)
                        );
                        inventory_set(screen, 13, 1, 'minecraft:structure_void', nbt('{display:{Name:\'"'+tick+'"\'},HideFlags:3}'));
                    ),
                );
            ),
        );
        global_counts:player = tick;
        return('cancel');
    ));
    global_fakeplayersscreen:fakeplayer = [screen, models];

    for(models, if (_ != null,
        inventory_set(screen, _:'slot', 1, _:'item' || 'minecraft:structure_void', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))
    ));
    setAir(screen);
);
soundDell(player) -> sound('block.note_block.bell', player~'pos');
setAir(screen, ...options) -> fromListSetAir(screen, range(options:0 || 9*3));
fromListSetAir(screen, listIndex) -> 
    for (listIndex, if (
        inventory_get(screen, _) == null,
        inventory_set(screen, _, 1, 'minecraft:light_gray_stained_glass_pane', nbt('{display:{Name:\'""\'},HideFlags:3}'))
    ));
setScreenInventory(models, screen, fakeplayer) -> if (models, for (models, (
    [playerSlot, screenSlot] = [_:0, _:1];
    [item, count, nbt] = inventory_get(fakeplayer, playerSlot) || ['minecraft:air', 0, null];
    inventory_set(screen, screenSlot, count, item, nbt);
)));
setPlayerInventory(models, screen, fakeplayer) -> if (models, for (models, (
    [playerSlot, screenSlot] = [_:0, _:1];
    [item, count, nbt] = inventory_get(screen, screenSlot) || ['minecraft:air', 0, null];
    inventory_set(fakeplayer, playerSlot, count, item, nbt);
)));
setSelected(screen, fakeplayer, slot) -> (
    for (range(9), inventory_set(screen, _, 1, 'minecraft:structure_void', null));
    inventory_set(screen, slot, 1, 'minecraft:barrier', nbt('{Enchantments:[{id:void}]}'));
    run('player ' + fakeplayer~'command_name' + ' hotbar ' + (number(slot)+1));
);
i18n(player, key) -> return(str((global_languages:(player~'language')):key || (global_languages:'en_us'):key));
create_datapack('invupd', 
    {
        'readme.txt' -> ['This datapack is built by carpet script','this data pack is created by scarpet','please dont touch it'],
        'data' -> {
            'chyx' -> {
                'advancements' -> {
                    'xd.json '-> {
                        'rewards' -> {'function' -> 'chyx:invupd'},
                        'criteria' -> {
                            'example' -> {
                                'trigger' -> 'minecraft:inventory_changed',
                                'conditions' -> {}
                            }
                        }
                    }
                },
                'functions' -> {
                    'invupd.mcfunction' -> 'script run signal_event(\'invupd\', null, player())\nadvancement revoke @s only chyx:xd'
                }
            }
        }
    }
);
handle_event('invupd', _(fakeplayer) -> (
    screen = global_fakeplayersscreen:fakeplayer;
    if(screen, setScreenInventory(screen:1, screen:0, fakeplayer));
));
__on_player_disconnects(fakeplayer, reason)->(
    screen = global_fakeplayersscreen:fakeplayer;
    if(screen, (
        drop_item(screen:0, -1);
        close_screen(screen:0);
    ));
);
__on_player_interacts_with_entity(creativeplayer, fakeplayer, hand) -> if (
    creativeplayer~'player_type' != 'fake' && creativeplayer != fakeplayer && fakeplayer~'player_type' == 'fake' && hand == 'mainhand',
    __page('menu', creativeplayer, fakeplayer),
);
__on_tick() -> for(filter(player('all'), _~'player_type' == 'fake'),
    selected_slot = _~'selected_slot';
    if((old_slot = global_fakeplayers_selected_slot:_) != null &&
        old_slot != selected_slot,
        if (
            global_fakeplayersscreen:_ && global_fakeplayersscreen:_:2 == 'toolbar',
            setSelected(global_fakeplayersscreen:_:0, fakeplayer, selected_slot)
        );
    );
    global_fakeplayers_selected_slot:_ = selected_slot
);
