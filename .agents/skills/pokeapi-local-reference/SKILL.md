---
name: pokeapi-local-reference
description: Consult and maintain the project's local PokeAPI reference for endpoint contracts, pagination, response mapping, and API fixtures. Use whenever implementing, debugging, reviewing, or documenting PokeAPI integration; use local documentation first instead of repeatedly browsing the online docs.
---

# Local PokeAPI reference

## Read local knowledge first

Read the project reference at `docs/api/pokeapi/README.md`, then only the endpoint documents and examples needed for the task. Paths are relative to the repository root. This skill's `scripts/refresh_examples.py` locates that root from its own location.

- Use `docs/api/pokeapi/pagination.md` for Pokemon list requests, resource IDs, cursors, retries, and page boundaries.
- Use `docs/api/pokeapi/pokemon.md` for Home card detail fields, types, and artwork.
- Use `docs/api/pokeapi/examples/metadata.json` to understand sample provenance and age. JSON examples show actual captured values, not exhaustive schemas.
- Keep API behavior distinct from application requirements. The local reference labels project-specific pagination expectations separately.

Do not open `https://pokeapi.co/docs/v2/` or its fragments during routine implementation. Reading this skill does not trigger network access or refreshes. Normal application API calls and explicitly requested integration checks remain allowed within the task's scope; they are separate from documentation browsing.

## When local knowledge is insufficient

State the specific undocumented endpoint, field, or behavior. Do not invent a contract or present stale samples as fresh verification. Use relevant existing code and local artifacts as evidence, with their limitations. Do not fetch the entire API or copy the online manual merely to fill an unused gap.

When the developer requests a documentation update or verification, obtain focused evidence from the public API where sufficient. Browse official online documentation only when the developer explicitly requests an online documentation check. If an unresolved gap blocks correct implementation, ask for the needed information rather than guessing.

## Maintain examples and contracts

For an explicitly requested sample refresh, run from the repository root:

```sh
python3 .agents/skills/pokeapi-local-reference/scripts/refresh_examples.py
```

This makes two GET requests to fixed public API endpoints and updates local JSON examples plus capture metadata. It does not access the documentation site or change app behavior. If the network fails, preserve prior samples, report the failure, and do not replace them with fabricated data.

After a refresh, inspect the diff, update affected endpoint notes when supported by evidence, and distinguish observed sample behavior from general guarantees. Validate JSON and references. Do not run the refresh from the app, builds, or ordinary tests. Commit or publish documentation only when requested.

Keep the reference focused on resources needed by implemented or requested features. Record sources and capture times for additions. The local reference is project documentation, not an SDK, a complete API mirror, or an application response cache.
