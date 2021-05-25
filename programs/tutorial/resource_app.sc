__config() -> {
    'resources' -> [
        {
            'source' -> 'https://raw.githubusercontent.com/gnembon/fabric-carpet/master/src/main/resources/assets/carpet/icon.png',
            'type' -> 'url',
            'target' -> 'foo/photos.zip/foo/cm.png',
        },
        {
            'source' -> 'https://raw.githubusercontent.com/gnembon/fabric-carpet/master/src/main/resources/assets/carpet/icon.png',
            'type' -> 'url',
            'target' -> 'direct.zip/cm.png',
        },
        {
            'source' -> 'survival/rifts/shared/vanilla_worldgen_16.zip',
            'type' -> 'store',
            'target' -> 'vanilla_worldgen_16.zip',
            'shared' -> true,
        },
        {
            'source' -> 'survival/README.md',
            'type' -> 'store',
            'target' -> 'survival_readme.md',
            'shared' -> true,
        },
        {
            'source' -> 'carpets.sc',
            'type' -> 'app',
            'target' -> 'apps/flying_carpets.sc',
            'shared' -> true,
        },
    ]
}