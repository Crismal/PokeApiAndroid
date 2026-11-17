# Pokemon list and pagination

## Request

```http
GET /pokemon?limit=20&offset=0
```

`limit` selects the maximum number of resources in a page. `offset` selects how many resources to skip, starting at zero. PokeAPI's documented default page size is 20; send both parameters explicitly when predictable page sizes matter.

| Field | Meaning |
| --- | --- |
| `count` | Total matching resources at response time; do not hardcode it |
| `next` | URL of the next page, or null at the end |
| `previous` | URL of the previous page, or null on the first page |
| `results` | Ordered array of resource references |
| `results[].name` | API resource name |
| `results[].url` | Detail-resource URL, including the resource ID |

See the captured [first page](examples/pokemon-page.json). It is a fixture, not an offline replacement for the app's live list.

## IDs and details

The list endpoint does **not** return a separate `id`, type information, or artwork. For example, the resource URL `https://pokeapi.co/api/v2/pokemon/1/` identifies Pokemon 1. If deriving an ID from that URL, remove the trailing slash and validate the final path segment as an integer. Do not calculate IDs using the row index or offset.

Fetch the [Pokemon detail endpoint](pokemon.md) to obtain types and the API-provided artwork URL for cards. This means one list request plus detail requests for that page; control concurrency and cache successful detail responses. Preserve list order even if detail requests complete out of order.

## Application behavior for pagination

The following are implementation expectations for this project, not additional server guarantees:

- Keep one page load in flight at a time. Recomposition or repeated scroll events must not enqueue duplicate loads.
- Advance the page cursor only after the required page data has loaded successfully. Retry the failed page rather than skipping it.
- Prefer `next` as the end-of-list signal. If using its URL, validate the HTTPS scheme and expected PokeAPI host/path before following it. If converting it to an offset, parse the query rather than guessing the next ID.
- Keep already displayed items if appending a page fails. Show an append error and retry action separately from the initial load error.
- Merge items by stable resource ID, preserving order, if a repeated page overlaps previous data.
- Stop when `next` is null. Treat an empty page that still reports `next` as an unexpected condition requiring bounded handling, not an invitation to loop forever.
- Keep cancellation distinct from a user-visible failure.

Exact name/ID lookup uses the detail resource; the list endpoint does not support a free-text search query. This project implements partial-name search using a complete local ID/name index populated through these list pages; see [local search](../../search.md).
