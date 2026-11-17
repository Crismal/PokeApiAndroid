# Local PokeAPI reference

Use this reference before opening online PokeAPI documentation. It describes the subset needed by this project, not the entire PokeAPI surface. Reading these files requires no network connection; the app still needs internet access to fetch live data and artwork.

## Coverage and provenance

| Reference | Coverage |
| --- | --- |
| [Pagination](pagination.md) | Pokemon list, limit/offset, resource URLs, page boundaries |
| [Pokemon details](pokemon.md) | IDs, names, types, and artwork for Home cards |
| [Examples](examples/) | Captured API responses, reduced detail fields, and capture metadata |

API base: `https://pokeapi.co/api/v2/`.

The pagination contract is based on the official PokeAPI v2 documentation already inspected during this project. Examples come from the public API; `examples/metadata.json` records URLs, capture time, and whether fields were reduced. Numeric totals and example URLs are snapshots, not constants to hardcode.

These files are a maintained project reference, not an automatically synchronized mirror. If a field or endpoint is absent, treat it as undocumented here. Do not guess its contract or claim this reference is current without checking its provenance.

## General contract

- Resources are read with HTTP GET. No API key is needed for these endpoints.
- List responses contain `count`, `next`, `previous`, and `results`.
- Referenced resources usually contain a `name` and `url`; they are not full detail objects.
- Check HTTP status before parsing JSON. A missing Pokemon can return HTTP 404; transport errors, timeouts, and other unsuccessful statuses must be handled separately from an empty successful list.
- Do not assume error bodies use the successful response schema or a stable JSON error shape.
- Cache requested resources locally, as requested by PokeAPI's fair-use guidance. A documentation fixture is not an application cache.
- Preserve server ordering and use actual resource identifiers. Do not assume an offset is an ID or that all IDs are contiguous.

## Refreshing examples

Normal development reads local files. To explicitly refresh the samples when requested, run this command from the repository root:

```sh
python3 .agents/skills/pokeapi-local-reference/scripts/refresh_examples.py
```

The command makes two GET requests to the public API, never to the documentation site. It captures the first 20 Pokemon and a reduced Bulbasaur detail response, then records provenance. It fetches and validates both responses before replacing existing snapshots. Review the resulting diff and update the written contracts if the observed data justifies it; samples alone do not establish every possible API behavior.

External documentation is a fallback only when local knowledge is insufficient and the developer explicitly requests an online documentation check. No build or test should depend on an automatic refresh.
