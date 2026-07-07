# Config Search

Search every mod's config options from one search bar in Mod Menu.

## What it does

Ever known a mod has a setting for something but not which screen it's buried in? Config Search adds a small search button next to the search field on Mod Menu's Mods screen. Type what you're looking for ("light", "render distance", "hud") and every matching config option from every installed mod shows up in one list, with the option name and where it lives (mod > category > group). Clicking a result opens that mod's config screen, jumping straight to the right category where the config library supports it.

The same screen is also reachable through Config Search's own config button in Mod Menu, and results match against option names, tooltips, categories and mod names in your selected language.

## Supported config libraries

- YetAnotherConfigLib (YACL) 3.x
- Cloth Config
- MidnightLib
- Forge Config API Port (1.21.1+ only)

Mods using other config libraries or hand-written config screens still show up as a single result that opens their config screen.

## Versions

Fabric, Minecraft 1.20.1, 1.21.1, 1.21.11 and 26.2. Requires Mod Menu and Fabric API.

## For developers

Config Search can index your mod's options directly if you register a `configsearch` entrypoint. This also works for config libraries that want to provide options for every mod using them.

```json
"entrypoints": {
    "configsearch": ["com.example.mymod.ConfigSearchImpl"]
}
```

```java
public class ConfigSearchImpl implements ConfigSearchEntrypoint {
    @Override
    public void register(Registrar registrar) {
        registrar.addEntries("mymod", List.of(new ConfigOptionEntry(
            "mymod",
            Component.literal("My Mod"),
            List.of(Component.translatable("mymod.config.category.general")),
            Component.translatable("mymod.config.option.enableThing"),
            Component.translatable("mymod.config.option.enableThing.tooltip"),
            parent -> MyModConfigScreen.create(parent)
        )));
    }
}
```

Registered entries replace the built-in extraction for that mod id. You can also register a `ScreenOptionExtractor` to handle every screen of a config library.

## Building

```
./gradlew build
```

Jars for every supported Minecraft version land in `versions/<version>/build/libs`.

## License

LGPL-3.0
