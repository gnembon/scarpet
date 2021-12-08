__config() -> {
    'resources' -> [
        {
            'source' -> 'https://raw.githubusercontent.com/gnembon/fabric-carpet/master/src/main/resources/assets/carpet/icon.png',
            'target' -> 'foo/photos.zip/foo/cm.png',
        },
        {
            'source' -> 'https://raw.githubusercontent.com/gnembon/fabric-carpet/master/src/main/resources/assets/carpet/icon.png',
            'target' -> 'direct.zip/cm.png',
        },
        {
            'source' -> '/survival/rifts/shared/vanilla_worldgen_16.zip',
            'target' -> 'vanilla_worldgen_16.zip',
            'shared' -> true,
        },
        {
            'source' -> 'https://raw.githubusercontent.com/slicedlime/examples/master/vanilla_worldgen.zip',
            'target' -> 'vanilla_worldgen_17.zip',
            'shared' -> true,
        },
        {
            'source' -> '/survival/README.md',
            'target' -> 'survival_readme.md',
            'shared' -> true,
        }
    ],
    'libraries' -> [
        {
            'source' -> 'carpets.sc'
        }
    ]
}
