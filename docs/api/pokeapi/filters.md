# Cached Pokemon filters

## API evidence

Focused live response checks on 2026-11-18 used `GET https://pokeapi.co/api/v2/pokemon/1/` and `GET https://pokeapi.co/api/v2/type/grass/`. These are response checks, not an online documentation refresh or an exhaustive API audit. The implemented Pokemon list contract has limit/offset pagination and no documented arbitrary filter parameters; the app filters its saved detail records instead.

Pokemon detail fields `height` and `weight` are mapped as decimetres and hectograms. Bulbasaur's observed values were `7` and `69` (0.7 m, 6.9 kg). Missing or non-positive fields remain unknown; an active size filter requires known values. No missing attribute is treated as zero.

`GET /type/{name}/` supplies `name` and `damage_relations`. The incoming arrays `double_damage_from`, `half_damage_from`, and `no_damage_from` contain resource objects with a `name`. They describe 2×, 0.5×, and 0× incoming damage respectively. An unlisted attacking type has 1× damage. For the observed grass response, fire was in `double_damage_from` and water in `half_damage_from`.

To calculate a Pokemon's weakness, multiply incoming damage across its defensive types. A net multiplier above 1× is a weakness. Resistance can cancel a weakness, and immunity produces 0×. Abilities, historical generation rules, and battle-specific effects are outside this filter. Missing type-defense data causes a recoverable preparation error, not a false empty result.

## Project behavior

- At most three selections per group; OR within a group, AND between active groups. An empty group imposes no restriction.
- Height: short <1 m; medium 1–<2 m; tall ≥2 m.
- Weight: light <10 kg; medium 10–<100 kg; heavy ≥100 kg.
- ID bounds are inclusive. The slider spans 1 to the greater of 1000 or the highest saved Pokemon ID. This is a cached-data bound, not a claim about the online catalogue's maximum. The default upper bound is unrestricted.
- Filters inspect all saved detail rows in ID order, not just the cards currently loaded on screen. They do not fetch missing Pokemon IDs or traverse the complete catalogue. Browse more pages with filters cleared to expand coverage.
- Draft changes and Reset take effect only after Apply. Dismissing preserves previously applied filters. Clearing filters restores the unfiltered list and its pagination cursor.
- Type and ID filters work directly from existing saved fields. When size attributes are missing, only saved candidate IDs are refreshed, using at most four concurrent detail requests. Type-defense resources are fetched only when a weakness filter needs them and then stored in Room. Failed preparation preserves the current results and draft for retry.
- Unknown or evicted artwork does not affect text filtering. An empty cache is an empty result, not a reason to download the catalogue.
