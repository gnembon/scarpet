// 原作者: Scarpet
// 修改: 猴子
// 建議: 企鵝

__config() -> {
    'scope' -> 'global',
    'stay_loaded' -> true,
    'requires' -> {
        'carpet' -> '>=1.4.57'
    },
    'commands' -> {
        '' -> _() -> print('\n使用方式:\n  對這假人按下右鍵\n或是:\n  執行指令/fakeGui <player>'),
        '<player>' -> _(fakeplayer) -> (
            fakeplayer = player(fakeplayer);
            if (
                fakeplayer~'player_type' != 'fake' && player() != fakeplayer,
                return(display_title(player(), 'actionbar', '§4§L等等他不是假人...')),
                __page('menu', player(), fakeplayer),
            );
        ),
    },
    'arguments' -> {
        'player' -> {'type' -> 'players', 'single' -> true}
    },
};

global_slotmap = [[-1,7],[-2,0],[-3,1],[-4,2],[-5,3]];
global_fakeplayers_selected_slot = m();
global_fakeplayersscreen = m();
global_counts = m();
global_models = l(
    nbt('{ name: \'once\', title: \'§f§L點擊一次\', item: \'minecraft:minecart\'}'),
    nbt('{ name: \'continuous\', title: \'§f§L持續點擊\', item: \'minecraft:apple\'}'),
    nbt('{ name: \'interval\', title: \'§f§L定時點擊\', item: \'minecraft:repeater\'}'),
);

__page(type_, creativeplayer, fakeplayer) -> (
    // 菜單
    if (type_ == 'menu', (
        screen = create_screen(creativeplayer, 'generic_9x3', fakeplayer~'command_name'+' 的菜單', _(screen, player, action, data, outer(fakeplayer)) -> (
            if(action == 'close', (
                global_fakeplayersscreen:fakeplayer = null;
                drop_item(screen, -1);
                return();
            ));
            if (action == 'pickup', (
                slot = data:'slot';
                if (slot > 9*3-1, return());
                newPage = command = null;

                if (slot == 16, command = 'kill');
                if (slot == 15, command = 'stop');
                if (slot == 14, command = 'jump');
                if (slot == 13, command = 'swapHands');

                if (command != null, (
                    global_fakeplayersscreen:fakeplayer = null;
                    run('player ' + (fakeplayer~'command_name') + ' ' + command);
                    soundDell(player);
                    close_screen(screen);
                    return();
                ));

                if (slot == 12, newPage = 'bag');
                if (slot == 11, newPage = 'use');
                if (slot == 10, newPage = 'attack');

                if (newPage != null, (
                    soundDell(player);
                    close_screen(screen);
                    __page(newPage, player, fakeplayer);
                    return();
                ));
            ));

            return('cancel');
        ));
        global_fakeplayersscreen:fakeplayer = [screen, models];

        for (l(
            nbt('{ title: \'§f§L使用左鍵\', item: \'minecraft:wooden_sword\'}'),
            nbt('{ title: \'§f§L使用右鍵\', item: \'minecraft:cooked_porkchop\'}'),
            nbt('{ title: \'§f§L背包菜單\', item: \'minecraft:chest\'}'),
            nbt('{ title: \'§f§L左右手互換\', item: \'minecraft:magenta_glazed_terracotta\'}'),
            nbt('{ title: \'§f§L跳\', item: \'minecraft:rabbit_foot\'}'),
            nbt('{ title: \'§f§L停止\', item: \'minecraft:barrier\'}'),
            nbt('{ title: \'§f§L假人退出\', item: \'minecraft:tnt\'}'),
        ), if (_ != null, inventory_set(screen, _i+9+1, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))));
        setAir(screen);
        return();
    ));

    // 背包菜單
    if (type_ == 'bag', (
        models = [
            nbt('{ slot: 0, item: \'white_shulker_box\', title: \'背包\' }'),
            nbt('{ slot: 1, item: \'minecraft:wooden_pickaxe\', title: \'工具欄\' }'),
            nbt('{ slot: 2, item: \'minecraft:leather_chestplate\', title: \'裝備欄\' }'),
        ];
        screen = create_screen(creativeplayer, 'generic_9x3', '背包菜單', _(screen, player, action, data, outer(fakeplayer)) -> (
            if (action == 'close', (
                global_fakeplayersscreen:fakeplayer = null;
                drop_item(screen, -1);
                return();
            ));
            if (action == 'pickup', (
                slot = data:'slot';
                if (slot > 9*3-1, return());
                if (
                    // 背包
                    slot == 11, (
                        close_screen(screen);
                        models = map(range(27), [_+9, _+9]);
                        screen = create_screen(player, 'generic_9x4','工具欄', _(screen, player, action, data, outer(fakeplayer), outer(models)) -> (
                            if (action == 'close', (
                                global_fakeplayersscreen:fakeplayer = null;
                                drop_item(screen, -1);
                                return();
                            ));
                            slot = data:'slot';
                            if (slot >= 0 && slot < 9, return('cancel'));
                            if (action == 'slot_update',
                                if (
                                    slot >= 9 && slot <= 35, setPlayerInventory(models, screen, fakeplayer),
                                    slot >= 36 && slot <= 62, setScreenInventory(models, screen, fakeplayer),
                                );
                            );
                        ));
                        global_fakeplayersscreen:fakeplayer = [screen, models];
                        setScreenInventory(models, screen, fakeplayer);
                        inventory_set(screen, 4, 1, 'minecraft:player_head', nbt('{SkullOwner:"'+fakeplayer+'",display:{Name:\'"'+fakeplayer+' 的包包"\'},HideFlags:3}'));
                        setAir(screen, 9*1);
                        return();
                    ),
                    // 工具欄
                    slot == 13, (
                        close_screen(screen);
                        models = map(range(9), [_, _+9]);
                        screen = create_screen(player, 'generic_9x2','工具欄', _(screen, player, action, data, outer(fakeplayer), outer(models)) -> (
                            if (action == 'close', (
                                global_fakeplayersscreen:fakeplayer = null;
                                drop_item(screen, -1);
                                return();
                            ));
                            slot = data:'slot';
                            if (
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
                        return();
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
                        screen = create_screen(player, 'generic_9x2','裝備欄', _(screen, player, action, data, outer(fakeplayer), outer(models)) -> (
                            if (action == 'close', (
                                global_fakeplayersscreen:fakeplayer = null;
                                drop_item(screen, -1);
                                return();
                            ));
                            slot = data:'slot';
                            if (slot >= 0 && slot <= 9*2 && !first(map(models, _:1), _ == slot), return('cancel'));
                            if (action == 'slot_update',(
                                if (
                                    first(map(models, _:1), _ == slot), setPlayerInventory(models, screen, fakeplayer),
                                    slot >= 36 && slot <= 40, setScreenInventory(models, screen, fakeplayer),
                                );)
                            );
                        ));
                        global_fakeplayersscreen:fakeplayer = [screen, models];

                        setScreenInventory(models, screen, fakeplayer);
                        for (models, inventory_set(screen, (_:1)+9, 1, (_:2):'item', null));

                        fromListSetAir(screen, filter(range(2*9), n=_;if (first(map(models, _:1), _ == n) == null, true, false)));
                    )
                );
            ));

            return('cancel');
        ));
        global_fakeplayersscreen:fakeplayer = [screen, models];

        for (models, inventory_set(screen, _:'slot'*2+9+2, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}')));
        setAir(screen, 9*3);
        return();
    ));
    // 使用
    if (type_ == 'use', (
        screen = create_screen(creativeplayer,'generic_9x3','使用模式', _(screen, player, action, data, outer(fakeplayer)) -> (
            if (action == 'close', (
                global_fakeplayersscreen:fakeplayer = null;
                drop_item(screen, -1);
                return();
            ));

            if (action == 'pickup', (
                slot = data:'slot';
                if (slot > 9*3-1, return());
                getClickType(slot, screen, player, 'player ' + (fakeplayer~'command_name') + ' use ');
            ));

            return('cancel');
        ));
        global_fakeplayersscreen:fakeplayer = [screen, models];


        for (global_models, if (_ != null, inventory_set(screen, _i*2+9+2, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))));
        setAir(screen);
        return();
    ));

    // 攻擊
    if (type_ == 'attack', (
        screen = create_screen(creativeplayer,'generic_9x3','攻擊模式', _(screen, player, action, data, outer(fakeplayer)) -> (
            if(action == 'close', (
                global_fakeplayersscreen:fakeplayer = null;
                drop_item(screen, -1);
                return();
            ));

            if (action == 'pickup', (
                slot = data:'slot';
                if (slot > 9*3-1, return());
                getClickType(slot, screen, player, 'player ' + (fakeplayer~'command_name') + ' attack ');
            ));

            return('cancel');
        ));
        global_fakeplayersscreen:fakeplayer = [screen, models];


        for (global_models, if (_ != null, inventory_set(screen, _i*2+9+2, 1, _:'item', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))));
        setAir(screen);
        return();
    ));
);
getClickType(slot, screen, creativeplayer, base_cmd) -> (
    type_ = null;
    if (slot < 11 || slot > 15, return());
    soundDell(creativeplayer);
    if (
        slot == 11, type_ = 'once',
        slot == 13, type_ = 'continuous',
        slot == 15, (
            speedPage(creativeplayer, base_cmd);
            return();
        ),
    );
    if (type_ != null, return(
        close_screen(screen);
        run(base_cmd + type_);
    ));
);
speedPage(creativeplayer, base_cmd) -> (
    global_counts:creativeplayer = 10;
    models = l(
        nbt('{ title: \'§f§L加10\', slot: 9, add: +10 }'),
        nbt('{ title: \'§f§L加1\', slot: 11, add: +1}'),
        nbt('{ title: \''+global_counts:creativeplayer+'\', slot: 13 }'),
        nbt('{ title: \'§f§L減1\', slot: 15, add: -1 }'),
        nbt('{ title: \'§f§L減10\', slot: 17, add: -10 }'),
        nbt('{ title: \'§e完成\', slot: 22, item: \'minecraft:redstone_lamp\' }'),
    );
    screen = create_screen(creativeplayer,'generic_9x3','設定定時', _(screen, player, action, data, outer(models), outer(base_cmd)) -> (
        tick = global_counts:player;
        if (action == 'close', (
            global_fakeplayersscreen:fakeplayer = null;
            drop_item(screen, -1);
            return();
        ));
        if (action == 'pickup', (
            slot = data:'slot';
            if (slot > 9*3-1, return());

            for (filter(models, _:'add'),
                if (tick+_:'add' < 1e10-1 && tick+_:'add' > 0,
                    inventory_set(screen, _:'slot', 1, 'minecraft:structure_void', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'));
                    if (slot == _:'slot', tick += _:'add'),
                    inventory_set(screen, _:'slot', 1, 'minecraft:barrier', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'));
                );
            );

            if (slot == 22, (
                soundDell(player);
                run(base_cmd + 'interval ' + tick);
                return(close_screen(screen));
            ));
            if (tick != global_counts:player, soundDell(player));
            inventory_set(screen, 13, 1, 'minecraft:structure_void', nbt('{display:{Name:\'"'+tick+'"\'},HideFlags:3}'));
        ));
        global_counts:player = tick;
        return('cancel');
    ));
    global_fakeplayersscreen:fakeplayer = [screen, models];

    for(models,
        if (_ != null,
            inventory_set(screen, _:'slot', 1, _:'item' || 'minecraft:structure_void', nbt('{display:{Name:\'"'+_:'title'+'"\'},HideFlags:3}'))
        )
    );
    setAir(screen);
);
soundDell(player) -> sound('block.note_block.bell', player~'pos');
setAir(screen, ...options) -> fromListSetAir(screen, range(options:0 || 9*3));
fromListSetAir(screen, listIndex) -> 
    for (listIndex, if (
        inventory_get(screen, _) == null,
        inventory_set(screen, _, 1, 'minecraft:light_gray_stained_glass_pane', nbt('{display:{Name:\'"你看不到我"\'},HideFlags:3}'))
    ));
setScreenInventory(models, screen, fakeplayer) -> (
    if (models,
        for (models, (
            [playerSlot, screenSlot] = [_:0, _:1];
            [item, count, nbt] = inventory_get(fakeplayer, playerSlot) || ['minecraft:air', 0, null];
            inventory_set(screen, screenSlot, count, item, nbt);
        ))
    );
);
setPlayerInventory(models, screen, fakeplayer) -> (
    if (models,
        for (models, (
            [playerSlot, screenSlot] = [_:0, _:1];
            [item, count, nbt] = inventory_get(screen, screenSlot) || ['minecraft:air', 0, null];
            inventory_set(fakeplayer, playerSlot, count, item, nbt);
        ))
    );
);
setSelected(screen, fakeplayer, slot) -> (
    print(screen);
    for (range(9), inventory_set(screen, _, 1, 'minecraft:structure_void', null));
    inventory_set(screen, slot, 1, 'minecraft:barrier', nbt('{Enchantments:[{id:void}]}'));
    run('player ' + fakeplayer~'command_name' + ' hotbar ' + (number(slot)+1));
);

create_datapack('invupd', 
    {
        'readme.txt' -> ['該數據包由 carprt script 建置','原作者: scarlet', '改作: 猴子'],
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
__on_player_interacts_with_entity(creativeplayer, fakeplayer, hand) -> (
    if(
        global_fakeplayersscreen:fakeplayer,
        return(),
        hand != 'mainhand' ||
        creativeplayer~'player_type' == 'fake',
        return(),
    );

    if (
        fakeplayer~'player_type' == 'fake' || fakeplayer~'player_type' == 'player',
        __page('menu', creativeplayer, fakeplayer), 
        display_title(creativeplayer, 'actionbar', '§4§L等等他不是假人...')
    );
);
__on_tick() -> (
    for(filter(player('all'), _~'player_type' == 'fake'),
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
);