// Scarpet app to download the Scarpet Edit (https://github.com/Ghoulboy78/Scarpet-edit) and its translations.
// This app is not intended to be downloaded manually, and will only work if downloaded using the "/script download building/scarpet_edit_installer.sc" command
// to download it from the app store.

printf(text) -> (
    print(format(text));
);

checkText = 'Check the Scarpet Edit license at https://github.com/Ghoulboy78/Scarpet-edit/blob/master/licence.txt';
global_langs = ['es_la', 'it_it', 'zh_cn'];

if (length(list_files('', 'text')) == 0,
    firstTime = true;
);

genResources() -> (
    resources = [];
    for (global_langs,
        resources += {
            //'source' -> 'https://github.com/Ghoulboy78/Scarpet-edit/releases/latest/download/' + _ + '.json' TODO Change on SE release
            'source' -> 'https://raw.githubusercontent.com/Ghoulboy78/Scarpet-edit/master/se.data/langs/' + _ + '.json'
        };
    );
    resources += {
        'source' -> 'https://raw.githubusercontent.com/Ghoulboy78/Scarpet-edit/master/licence.txt'
    };
    resources
);

__config() ->
{
    'scope' -> 'global',
    'stay_loaded' -> false, // Don't keep it running if autoloaded
    'requires' -> {
        'carpet' -> '>=1.4.42'
    },
    'libraries' -> [
        {
            'source' -> 'https://github.com/Ghoulboy78/Scarpet-edit/releases/latest/download/se.sc'
        }
    ],
    'resources' -> genResources()
};

if (length(list_files('', 'text')) != 0 && firstTime,
    printf('l Scarpet Edit installed successfully (unless otherwise noted)');
, read_file('licence', 'text'):0 != checkText,
    printf('l Scarpet Edit updated successfully (unless otherwise noted)');
, //else
    schedule(0, _() -> ( // So it is printed after the loaded message and not printed on autoload
        printf('r Error when downloading Scarpet Edit!');
        print(format('r The Scarpet Edit installer is supposed to be used by running the "/script download building/scarpet_edit_installer.sc" command (click), not by loading it manually!',
            '?/script download building/scarpet_edit_installer.sc',
            '^g Click to get the download command'
        ));
        printf('r If you wish to update Scarpet Edit, just run the download command again.');
        printf('r You can safely remove the installer (and its .data folder) once Scarpet Edit has been successfully installed');
    ));
    exit();
);

// Give ourselves a hint for next executions
delete_file('licence', 'text');
write_file('licence', 'text', checkText);

// Install languages
for (global_langs,
    signal_event('se_install_lang', null, [_, read_file(_, 'json')]);
    delete_file(_, 'json');
);
