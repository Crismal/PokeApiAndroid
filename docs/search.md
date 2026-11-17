# Local Pokemon search

## Catalogue and matching

PokeAPI REST v2 does not provide a partial-name search endpoint. The app builds a lightweight index from the existing Pokemon list resource, following its next-page offsets in batches of 200. It stores every returned ID and name, including alternate forms supplied by that resource. Resource IDs are not necessarily National Pokedex numbers.

The first nonblank query starts index preparation after a 300 ms typing debounce. The download continues if the query changes or is cleared, avoiding repeated catalogue downloads on every edit. Cancellation of the ViewModel cancels its work. The complete index and its completion metadata are published transactionally only after the final page succeeds. If setup fails, search offers retry instead of claiming there are no matches. There is no hardcoded total Pokemon count.

Queries run against Room only. Matching is case-insensitive for the stored API names; hyphens and spaces are normalized for names such as `mr-mime`. Numeric queries and `#`-prefixed numbers use exact resource IDs, including leading zeros. SQL wildcard characters are treated literally. Results have stable ID ordering and load in batches of 20.

## Details and offline behavior

The index is separate from fully downloaded Pokemon records. Building or searching it does not download details for the entire catalogue. Composed result rows, including limited lazy-list prefetch, request details through the shared cache-first repository, with a maximum of four detail requests in flight across Home and search. Coil handles artwork caching as before.

After catalogue setup, names and IDs remain searchable offline even for Pokemon not previously viewed. Uncached details require a connection; a failed detail request retains the named card with a placeholder and an explicit retry action. It does not fail the entire search or silently mark the incomplete record as downloaded. Already cached detail records and images are reused.

Changing the query cancels the old lookup and its outstanding detail jobs, and generation checks prevent stale results from replacing the new query's state. Clearing search restores the independent Home list and scroll position. Home pagination and search-result pagination use separate states and cursors.

The search field owns its state-based Compose editing state and forwards text changes to the ViewModel. Asynchronous catalogue/result updates do not write back into in-progress keyboard edits; a UI regression test covers a lagging external search state.

The index currently has no automatic freshness refresh. Its completion record includes a sync timestamp for a future policy. This matches the project's data-saving approach; newly added API resources require a future index-refresh feature or app-data reset to enter an already completed index. Artwork remains an evictable cache, not guaranteed permanent offline storage.

## Database migration

Schema version 2 adds `pokemon_search_index` and `pokemon_search_state`. The explicit `MIGRATION_1_2` creates these tables without deleting or rewriting existing Pokemon, page metadata, or page ordering. Both exported schemas are committed under `app/schemas/`; destructive migration is not enabled.

## Verification and manual test

```sh
./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:lintDebug :app:assembleDebug
```

Tests cover catalogue completion/retry, metadata-only synchronization, local result pagination, typing debounce, query cancellation, offline detail failures, literal wildcard handling, persisted offline name lookup, and a real version-1 database opened through the production version-2 Room migration path. Existing Home, persistence, and image-cache tests remain in the suite.

For a manual test:

1. Open Home, enter `pika`, and allow the first catalogue setup to finish. Pikachu should appear even if Home has not loaded Pokemon 25.
2. Try `#025`, `saur`, and `mr mime`. Compare exact-ID lookup with partial-name matches.
3. Clear search and check that Home retains its rows and position.
4. Relaunch the app and search again: the completed catalogue is reused.
5. After setup, disconnect the emulator's network and search for an unviewed Pokemon. Its ID/name should remain visible; uncached details show retry. Reconnect and retry that row. Restore network settings after testing.

The local API contract is documented in [pagination.md](api/pokeapi/pagination.md); this feature does not query an unsupported server-side search parameter.
