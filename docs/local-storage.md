# Local Pokemon storage

## What is saved

Room stores the Home data used by the app, rather than every field from the API:

- `pokemon`: ID, API name, ordered type names, nullable artwork URL, and nullable height/weight in API units. Types use a JSON-list converter to preserve their order.
- `type_defenses`: incoming double-damage, half-damage, and immunity type names, keyed by defensive type. Fetched only when a weakness filter needs them, then reused offline.
- `pokemon_pages`: a composite offset/page-size key, the next offset (including null at the end), expected item count, and fetch time in epoch milliseconds.
- `pokemon_page_entries`: each page's Pokemon IDs and their positions. IDs are not assumed to be contiguous or equal to offsets.

Images are files managed by Coil, not database blobs. A single image loader uses the `pokemon-artwork` cache directory with a 100 MiB disk limit and a memory limit of 15% of app memory. The existing API HTTP cache has its own 20 MiB disk limit.

## Reading and downloading

The repository returns a complete saved page without API calls. When a page is missing, it downloads its resource list and any missing detail records, with at most four detail requests in flight. Each successful detail is saved so a later retry can reuse it. A failed detail request does not mark a page complete or advance its saved cursor.

A completed page, its ordering, and its metadata are stored in one Room transaction. The repository returns the committed local snapshot. Reading a page also uses a transaction; missing entry rows cause a cache miss rather than returning a partial page with a cursor that skips items. Empty final pages are stored and distinguishable from cache misses.

## Offline behavior and freshness

Previously downloaded pages remain available after process death and restart. Reaching a page that has never been downloaded still requires a connection; the existing append error and retry UI preserves already displayed cards.

This iteration intentionally reuses saved pages without automatic time-based refreshes or background synchronization to avoid repeated downloads. Fetch times are recorded for a future refresh policy; this PR does not add a refresh control. API changes will not automatically replace records already saved on the device. Clearing app data removes the database and caches; normal app updates retain the database.

Coil caches artwork that has actually been loaded. It does not predownload every image in a saved page. The image cache can be evicted by its size policy, the OS, or the user; after eviction, an offline card retains its text and uses the existing placeholder until the artwork is available again. This is data-saving caching, not a guarantee that all artwork is permanently available offline.

## Schema and dependencies

The current database schema is version 3 and is exported under `app/schemas/`. Non-destructive migrations from version 1 and the separate search feature's version 2 add nullable height/weight fields and type defenses. Existing pages, details, and any search tables are retained. The historical version-2 schema comes from that search branch for migration verification; search UI and logic are not included here. Integrating the separate search feature must retain version-3 filter fields and register its entities together with these entities. Future schema changes must increase the version and supply a tested migration; destructive migration is not enabled.

Room 2.8.5 uses the existing `androidx.room` APIs. KSP 2.3.12 generates the DAO/database implementation without adding a separate Kotlin Android plugin to the existing AGP built-in Kotlin setup. SDK and Kotlin configuration are unchanged.

Reference guidance: [Android Room releases](https://developer.android.com/jetpack/androidx/releases/room), [KSP setup](https://kotlinlang.org/docs/ksp-quickstart.html), and [Coil caching](https://coil-kt.github.io/coil/image_loaders/#caching).

## Verification

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# Requires an emulator or device:
./gradlew :app:connectedDebugAndroidTest
```

Database tests close and reopen a file-backed database, load saved pages using an API stub that rejects all calls, check transaction rollback and incomplete-page handling, and verify that partial-download retries reuse successful detail records. The image test downloads a controlled PNG through an intercepted HTTP response, recreates the image loader, and checks that its next load comes from disk with no second network request. These tests do not consume live PokeAPI data.
