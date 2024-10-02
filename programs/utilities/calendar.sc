__config() -> {
	'scope' -> 'global',
	'stay_loaded' -> true
};

global_gregorian_month_names = ['January', 'February', 'March', 'April', 'May', 'June',
	'July', 'August', 'September', 'October', 'November', 'December'];

// Set the starting date here in the format [year, month, day].
// The default value corresponds to January 1, 1950.
// The special value [1, 1, 1] formats the year as "Year 1" instead of "1950".
global_starting_date = [1950, 1, 1];

__on_start() -> (
	// Convert the starting date from YMD to days.
	global_starting_date = convert_gregorian_ymd_to_days(global_starting_date:0, global_starting_date:1, global_starting_date:2);

	// Initialize the bossbar
	calendar_bossbar = bossbar('calendar:main');
	if(!calendar_bossbar, calendar_bossbar = 'calendar:main');
	bossbar(calendar_bossbar, 'max', 24000);
);

__on_player_connects(player) -> (
	bossbar('calendar:main', 'add_player', player);
);

__on_tick() -> (
	day_ticks = day_time();
	days = floor(day_ticks / 24000);
	time_of_day = day_ticks % 24000;

	hours = floor(time_of_day / 1000);
	minutes = floor((time_of_day - hours * 1000) * 0.06);
	hours = (hours + 6) % 24;
	ampm = if(hours >= 12, 'pm', 'am');
	hours = if(hours % 12 == 0, 12, hours % 12);
	if(time_of_day >= 18000, days += 1);

	[year, month, day] = convert_days_to_gregorian_ymd(days + global_starting_date);

	year_string = if(global_starting_date == 366, str('Year %,d', year), year < 10000, str('%d', year), str('%,d', year));

	bossbar('calendar:main', 'value', time_of_day);
	bossbar('calendar:main', 'name',
		str('%d:%02d%s | %s %d, %s', hours, minutes, ampm, global_gregorian_month_names:(month-1), day, year_string)
	);
);

// Convert days since January 1, 1 BCE into year, month, and day (Gregorian Calendar).
// See: https://github.com/OpenTTD/OpenTTD/blob/3b3412d561e043f8d4859cd973299f94e3810288/src/date.cpp#L94
convert_days_to_gregorian_ymd(days) -> (
	DAYS_IN_YEAR = 365;
	DAYS_IN_LEAP_YEAR = 366;

	// There are 97 leap years in 400 years
	year = 400 * floor(days / (DAYS_IN_YEAR * 400 + 97));
	rem = floor_mod(days, (DAYS_IN_YEAR * 400 + 97));

	if(rem >= DAYS_IN_YEAR * 100 + 25,
		// There are 25 leap years in the first 100 years after
		// every 400th year, as every 400th year is a leap year
		year += 100;
		rem += -(DAYS_IN_YEAR * 100 + 25);

		// There are 24 leap years in the next couple of 100 years
		year += 100 * floor(rem / (DAYS_IN_YEAR * 100 + 24));
		rem = floor_mod(rem, (DAYS_IN_YEAR * 100 + 24));
	);

	// The first 4 years of the century are not always a leap year
	if(!is_leap_year(year) && rem >= DAYS_IN_YEAR * 4,
		year += 4;
		rem += -DAYS_IN_YEAR * 4;
	);

	// There is 1 leap year every 4 years
	year += 4 * floor(rem / (DAYS_IN_YEAR * 4 + 1));
	rem = floor_mod(rem, (DAYS_IN_YEAR * 4 + 1));

	// The last (max 3) years to account for; the first one
	// can be, but is not necessarily a leap year
	while(rem >= if(is_leap_year(year), DAYS_IN_LEAP_YEAR, DAYS_IN_YEAR), 4,
		rem += -if(is_leap_year(year), DAYS_IN_LEAP_YEAR, DAYS_IN_YEAR);
		year += 1;
	);

	// Skip the 29th of February in non-leap years
	if (!is_leap_year(year) && rem >= 59,
		rem += 1;
	);

	days_till_month = [
		0,   // January
		31,  // February
		60,  // March
		91,  // April
		121, // May
		152, // June
		182, // July
		213, // August
		244, // September
		274, // October
		305, // November
		335  // December
	];

	// Get the month from the day
	month = 0;
	for(days_till_month,
		if(rem >= _, month += 1);
	);
	rem += -days_till_month:(month-1);

	// Extract the remaining days
	day = rem;

	// Eliminate the year zero
	if(year <= 0, year += -1);

	// Convert month and day to 1-indexed values
	day += 1;

	// Collect results and return
	return([year, month, day]);
);

// Convert date (year, month, day) in the Gregorian calendar to days since January 1, 1 BCE.
// Month is 1-indexed (1..12).
// See: https://github.com/OpenTTD/OpenTTD/blob/3b3412d561e043f8d4859cd973299f94e3810288/src/date.cpp#L149
convert_gregorian_ymd_to_days(year, month, day) -> (
	// Years before 0 actually refer to the previous year
	// For example: year 0 = 1 BCE (-1), year -1 = 2 BCE (-2), ...
	if(year <= 0, year += 1);

	// Convert date into days
	days_till_month = [
		0,   // January
		31,  // February
		60,  // March
		91,  // April
		121, // May
		152, // June
		182, // July
		213, // August
		244, // September
		274, // October
		305, // November
		335  // December
	];

	days = days_till_month:(month-1) + day - 1;

	// Account for the missing of the 29th of February in non-leap years
	if(!is_leap_year(year) && days >= 60, days += -1);

	return(_DAYS_TILL(year) + days);
);

is_leap_year(year) -> (
	return(year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
);

_DAYS_TILL(year) -> (
	DAYS_IN_YEAR = 365;
	return(DAYS_IN_YEAR * year + _LEAP_YEARS_TILL(year));
);

_LEAP_YEARS_TILL(year) -> (
	if(year == 0, return(0));
	return(floor((year - 1) / 4) - floor((year - 1) / 100) + floor((year - 1) / 400) + 1);
);

// Takes the modulo of two numbers using flooring instead of truncation.
// Required by convert_days_to_gregorian_ymd for negative year support.
floor_mod(dividend, divisor) -> (
	x = floor(dividend / divisor) * divisor;
	return(dividend - x);
);