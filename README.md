# PokeApi — learning AI through Android development

This is an experimental Android project for testing AI capabilities and learning how to use them throughout software development. All project code will be generated using OpenAI's ChatGPT models, with developers providing prompts, reviewing results, and validating behavior.

The goal is to explore more than code generation: project instructions, skills, connected tools, planning, design implementation, testing, debugging, and code review are part of the learning experience.

## App and design

We are building a Pokédex that consumes [PokeAPI v2](https://pokeapi.co/docs/v2/) and follows this [Figma design](https://www.figma.com/design/THLxZSlOoUYMZrjFg0Kl1M/Pok%C3%A9dex?node-id=18241-2789).

## Technology and Android support

| Area | Project choice |
| --- | --- |
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose with Material 3 |
| Architecture | MVVM (Model–View–ViewModel) |
| Minimum Android SDK | API 24 (Android 7.0) |
| Target Android SDK | API 37 |
| Compile Android SDK | API 37 |

SDK values reflect the current configuration in [app/build.gradle.kts](app/build.gradle.kts). Dependency versions are defined in [gradle/libs.versions.toml](gradle/libs.versions.toml).

With MVVM, Compose screens display UI state and send user actions to ViewModels. ViewModels manage screen state and coordinate with the data layer. Repositories provide access to PokeAPI data, keeping network calls out of composables.

## Home

Home displays Pokemon IDs, names, types, and API-provided artwork. It loads 20 Pokemon initially and automatically requests another page near the end of the list. Failed page loads offer retry while preserving previously loaded cards. Search, sorting, generations, and detail navigation remain separate features.

The Filters button opens a scrollable sheet with types, weaknesses, heights, weights, and an inclusive ID range. Select up to three options per group; any option within a group matches, while all active groups must match. Apply commits the draft; dismiss discards it. Reset clears the draft, and Apply then restores normal pagination. The range starts at 1–1000 and expands to the highest saved ID if larger. An unrestricted upper bound includes all saved IDs.

Filters operate on all Pokemon details saved in Room, including earlier browsing sessions. They do not search the entire online catalogue. Active filters pause network pagination and display the saved-data coverage. First use of a size filter may refresh missing attributes for matching saved Pokemon; a weakness filter may download and persist type-defense metadata. Once those attributes are saved, filtering works offline. See [filter data contracts](docs/api/pokeapi/filters.md) for size thresholds and weakness calculation.

The repository reads saved pages and Pokemon details from Room before requesting the API. Page ordering, next-page cursors, and fetch timestamps survive app restarts. Missing pages are downloaded, stored transactionally, and then read from the database. Detail requests are limited to four at a time and retain the API's list ordering.

An app-wide Coil image loader uses a 100 MiB disk cache and a memory cache capped at 15% of available app memory. Image files stay outside Room; the database stores their URLs. The 20 MiB HTTP cache remains for server-cacheable API responses. See [local storage behavior](docs/local-storage.md) for cache policy, offline limitations, and database schema guidance.

The screen follows the saved Home screenshot. Dimensions in `ui/home/HomeDimensions` and the filter sheet are estimates for manual tuning; the bundled system font is used because the design's SF Pro font is not included. The filter sheet uses a dimmed backdrop and labelled height/weight controls because the exact silhouettes are not in the saved assets. Home currently uses a fixed light appearance to match that reference.

To verify the implementation:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# Requires a running Android emulator or device:
./gradlew :app:connectedDebugAndroidTest
```

## Working with AI

Read [AGENTS.md](AGENTS.md) for the project's AI collaboration instructions. For each prompt, the assistant should identify relevant capabilities, explain why they help, and use them when appropriate. We want to learn when to use each capability through practical work on this app.

See the [model-selection guide and decision diagram](docs/ai-model-guide.md) for task-based recommendations. Before changes, the assistant recommends a suitable model and reasoning effort. After application implementation, it installs and launches the tested app and leaves it ready for manual testing when a device is available.

The [AI capability usage tracker](docs/ai-capabilities.md) records our observed use of skills, agents, tools, and other capabilities. It shows evidence-based percentages and is updated after each user prompt; these numbers measure usage, not mastery or total AI potential.

AI-generated changes still require human review. Review the code, understand the decisions, and check the reported validation before accepting a change.

Completed repository changes automatically end with a focused commit, a pushed GitFlow branch, and a prefilled PR link targeting the appropriate base (normally `develop`). The developer submits the PR manually. Unfinished work or material unanswered questions stay local; an explicit request to keep changes local overrides this default. Tracker-only bookkeeping for conversational questions does not trigger a PR.
