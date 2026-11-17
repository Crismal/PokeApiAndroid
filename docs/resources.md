# Pokémon design resources

Recovered from the companion iOS project, `PokeAPI/Assets.xcassets`.

- `app/src/main/res/raw/pokemon_*.svg`: 26 original SVGs, including all 18 Pokémon type icons, Back, Front, Filter, Sort, Generation, Pattern, Pokeball, and PokeballMenu. Preserved without modification. Convert these to Android VectorDrawable XML before using them with Compose `painterResource`; raw SVG files are not drawable resources.
- `app/src/main/res/drawable/pokemon_logo.png`: original Pokémon logo.
- `app/src/main/res/drawable/pokemon_loading.png`: original loading Pokéball image.
- `app/src/main/res/values/pokemon_colors.xml`: 39 named sRGB colors, including foreground and background colors for all 18 Pokémon types. Names use the `pokemon_` prefix and replace hyphens with underscores. The empty iOS AccentColor entry is omitted.

No screens or existing theme values are changed by this import. Colors are available through `R.color.pokemon_*` and PNGs through `R.drawable.pokemon_*`.

## Remaining resources

The iOS app icon catalog is empty. No custom font files are bundled; the SwiftUI views use system fonts. Pokémon artwork and shiny sprites are loaded from API-provided URLs rather than bundled locally. Person, lock, and search icons use iOS system symbols and will need Android equivalents. SwiftUI source provides layout measurements and text sizes for the login, list, detail, and settings screens. Any differences from the intended Figma design still need a visual reference.
