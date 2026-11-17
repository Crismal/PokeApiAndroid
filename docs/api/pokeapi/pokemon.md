# Pokemon details used by Home

## Request

```http
GET /pokemon/{id-or-name}/
```

Examples: `/pokemon/1/` and `/pokemon/bulbasaur/`.

See the [reduced captured response](examples/pokemon-detail.json). Fields omitted from that fixture may still exist in the real response; it is deliberately limited to the Home card data.

| JSON path | Meaning and use |
| --- | --- |
| `id` | Integer resource identifier; use as the stable list key |
| `name` | API resource name; presentation formatting belongs in the UI |
| `types` | Array of type assignments |
| `types[].slot` | Position of the type assignment; sort by this when ordering badges |
| `types[].type.name` | Type identifier, such as `grass` or `poison` |
| `types[].type.url` | Reference to the type resource; not needed just to label a badge |
| `sprites.front_default` | Default front sprite URL; may be null |
| `sprites.other.official-artwork.front_default` | Official artwork URL used for the main card image when present; may be null |

## Mapping to the app

- Keep API parsing and response-to-model mapping in the data layer. Compose should receive the data needed to render a card, not navigate a JSON object.
- Use the detail response's ID and name as authoritative for that resource.
- Map type identifiers to the existing `pokemon_*` and `pokemon_back_*` color resources. Use a neutral fallback for an unknown identifier rather than failing the entire list.
- Prefer official artwork, then the default front sprite if available. When both are absent, show an accessible placeholder. An image download failure must not erase the card's text.
- Use the URL supplied by the response rather than constructing artwork paths from the ID.
- Do not assume every resource has exactly two types, that names are space-separated display labels, or that every image field is present.

This file does not yet document species, evolutions, abilities, moves, stats, or type matchups. Add those contracts when a feature needs them, using evidence and recorded provenance rather than inference from a single sample.
