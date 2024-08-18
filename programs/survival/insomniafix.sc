// --- insomniafix ---
// Better™ Phantoms by Federico Carboni.
// All bug fixes and changes can be customized using the command /insomniafix
// Phantoms no longer spawn for creative mode players (fixes MC-145177).
// Phantoms no longer spawn when the mobcap is saturated.
// Phantoms no longer spawn on mushroom islands and the void (fixes MC-126778
// and MC-127599).
// Optionally fix MC-154372 reverting phantom damage to 6 hearts instead of 2.
// Optionally enable large phantom spawning which increases the size of phantoms
// the longer you go without sleeping. Similar to VanillaTweaks' large phantoms
// datapack.

// Edit the config file instead of this default configuration or just use the
// command /insomniafix
global_default_config = {
	'respect_mobcap' -> true,
	'spawn_in_creative' -> false,
	'fix_non_spawnable_biomes' -> true,
	'fix_damage' -> false,
	'large_phantoms' -> false,
};
global_config = read_file('config', 'json') || (
	write_file('config', 'json', global_default_config);
	global_default_config
);
global_rule_info = {
	'respect_mobcap' -> 'Phantoms won\'t spawn when the mobcap is saturated (false = vanilla)',
	'spawn_in_creative' -> 'Phantoms spawn for creative mode players (set to false to fix MC-145177)',
	'fix_non_spawnable_biomes' -> 'Phantoms no longer spawn on mushroom islands and the void (fixes MC-126778 and MC-127599)',
	'fix_damage' -> 'Set phantom base damage to 6 instead of 2 (reverts damage to 1.13) (fixes MC-154372)',
	'large_phantoms' -> 'Increase the size of phantoms the longer the player goes without sleeping. If enabled fix_damage will be considered enabled as well regardless of its value.',
};

__config() -> {
	'strict' -> true,
	'scope' -> 'global',
	'command_permission' -> 'ops',
	'commands' -> {
		'' -> 'show_all',
		...map(global_rule_info, [_, ['show_rule', _]]),
		...map(global_rule_info, [_ + ' <value>', ['set_rule', _]]),
	},
	'arguments' -> {
		'value' -> {
			'type' -> 'bool',
		},
	},
};

show_all() -> (
	print(format('b InsomniaFix settings:'));
	for(global_rule_info, (
		print(format('  - [', str('y %s', _), str('^ %s', global_rule_info:_), ' ]', str(' : %b', global_config:_)));
	));
);

show_rule(rule) -> (
	print(format(str('b %s', rule)));
	print(format(str(' %s', global_rule_info:rule)));
	print(str('current value: %b', rule, global_config:rule));
);

set_rule(value, rule) -> (
	global_config:rule = value;
	write_file('config', 'json', global_config);
	print(player(), str('%s: %b', rule, value));
);

global_do_insomnia = true;

// Only override the doInsomnia gamerule when the app is active
__on_start() -> (
	global_do_insomnia = bool(system_info('world_gamerules'):'doInsomnia');
	run('gamerule doInsomnia false');
);

__on_close() -> (
	run(str('gamerule doInsomnia %b', global_do_insomnia));
);

global_rain_gradient = 0;
global_thunder_gradient = 0;
global_rain_gradient_prev = 0;
global_thunder_gradient_prev = 0;
global_cooldown = 0;

__on_tick() -> (
	// Rain and thunder gradients are needed to calculate ambient darkness
	global_rain_gradient_prev = global_rain_gradient;
	global_thunder_gradient_prev = global_thunder_gradient;
	global_rain_gradient = clamp(if(weather('rain') > 0,
		global_rain_gradient + 0.01,
		global_rain_gradient - 0.01
	), 0, 1);
	global_thunder_gradient = clamp(if(weather('thunder') > 0,
		global_thunder_gradient + 0.01,
		global_thunder_gradient - 0.01
	), 0, 1);
	phantom_spawn();
);

// Phantom spawning implementation should perfectly replicate vanilla except for
// our adjustments. This is in its own function just for debugging.
phantom_spawn() -> (
	global_cooldown = global_cooldown - 1;
	if(global_cooldown > 0, exit());
	global_cooldown = (60 + floor(rand(60))) * 20;
	time_of_day = day_time();
	if(get_ambient_darkness(time_of_day) < 5, exit());

	// Mobcap check, not optimal since in 1.18 and above mobcap is checked per
	// player
	[n, cap] = get_mob_counts('monster');
	if(global_config:'respect_mobcap' && n > cap, exit());

	for(player('*'), (
		// if(_~'dimension' != 'overworld', continue());
		gm = _~'gamemode';
		if(gm == 'spectator', continue());
		if(!global_config:'spawn_in_creative' && gm == 'creative', continue());
		position = pos(_);
		local_difficulty = get_local_difficulty(position, time_of_day);
		if(position:1 < 64 || !see_sky(position) || local_difficulty <= rand(3),
			continue());
		time_since_rest =
			clamp(statistic(_, 'custom', 'time_since_rest'), 1, 2147483647);
		r = floor(rand(time_since_rest));
		pos = pos_offset(
			pos_offset(
				pos_offset(position, 'up', 20 + floor(rand(15))),
				'east',
				-10 + floor(rand(21))
			),
			'south',
			-10 + floor(rand(21)),
		);
		// Fix MC-126778 and MC-127599 (phantom spawning in mushroom biomes and
		// in the void).
		if(global_config:'fix_non_spawnable_biomes', (
			biome = biome(pos);
			if(biome(biome, 'tags')~'c:mushroom' != null || biome == 'the_void',
				continue());
		));
		if(r < 72000 || !is_clear_for_spawn(pos), continue());
		nm = 1 + floor(
			rand(get_difficulty_id(system_info('game_difficulty'))) + 1,
		);

		loop(nm, (
			nbt = if(global_config:'large_phantoms', (
				size = min(floor(rand(time_since_rest / 72000)), 10);
				// This is different from VanillaTweaks as it allows all sizes
				{
					'Size' -> size,
					'Attributes' -> [{
						'Name' -> 'generic.attack_damage',
						'Base' -> 6 + size,
					}, {
						'Name' -> 'generic.max_health',
						'Base' -> 20 + 3 * size,
					}],
					'Health' -> 20 + 3 * size,
				}
			), global_config:'fix_damage', {
				// Fix MC-154372 setting damage to 6
				'Attributes' -> [{
					'Name' -> 'generic.attack_damage',
					'Base' -> 6,
				}],
			}, {});
			spawn('phantom', pos, nbt);
		));
	));
);

// Minecraft mechanics
is_clear_for_spawn(pos) -> (
	block = block(pos);
	support = block(pos_offset(pos, 'down', 1));
	tags = block_tags(block);
	(
		transparent(block) && !tags~'prevent_mob_spawning_inside' &&
		!tags~'fire' && !block~'wither_rose' && !block~'sweet_berry_bush' &&
		!block~'cactus' && !block~'powder_snow' &&
		!block_tags(support)~'fire'&& !support~'lava' &&
		!support~'magma_block' && !support~'campfire[lit=true]' &&
		!support~'lava_cauldron'
	)
);

get_ambient_darkness(time_of_day) -> (
	rain_grad = lerp(1, global_rain_gradient_prev, global_rain_gradient);
	thunder_grad = lerp(
		1,
		global_thunder_gradient_prev,
		global_thunder_gradient
	) * rain_grad;
	d = 1 - rain_grad * 5 / 16;
	e = 1 - thunder_grad * 5 / 16;
	f = 0.5 + 2 * clamp(cos(get_sky_angle(time_of_day) * 360), -0.25, 0.25);
	(1 - f * d * e) * 11
);

get_sky_angle(time_of_day) -> (
	d = frac(time_of_day / 24000 - 0.25);
	e = 0.5 - cos(d * 180) / 2;
	(d * 2 + e) / 3
);

get_difficulty_id(difficulty) ->
	['peaceful', 'easy', 'normal', 'hard']~difficulty;

get_local_difficulty(pos, time_of_day) -> (
	difficulty = system_info('game_difficulty');
	if(difficulty == 'peaceful', return(0));
	inhab_time = inhabited_time(pos);
	moon_size = get_moon_size(time_of_day);
	g = clamp((time_of_day - 72000) / 1440000, 0, 1) * 0.25;
	f = 0.75 + g;
	h = clamp(inhab_time / 3600000, 0, 1) * if(difficulty == 'hard', 1.0, 0.75);
	h = h + clamp(moon_size * 0.25, 0.0, g);
	if(difficulty == 'easy', h = h * 0.5);
	get_difficulty_id(difficulty) * (f + h)
);

get_moon_size(time_of_day) -> (
	index = ((time_of_day / 24000 % 8 + 8) % 8);
	[1.0, 0.75, 0.5, 0.25, 0.0, 0.25, 0.5, 0.75]:index
);

lerp(delta, start, end) -> start + delta * (end - start);
frac(num) -> num - floor(num);
clamp(num, min, max) -> if(num < min, min, num > max, max, num);
