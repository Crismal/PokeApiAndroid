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

Home displays Pokemon IDs, names, types, and API-provided artwork. It loads 20 Pokemon initially and automatically requests another page near the end of the list. Failed page loads offer retry while preserving previously loaded cards. Filters, sorting, and detail navigation are reserved for separate changes.

Search matches partial names or exact IDs (including `#025`) against a complete local Room catalogue. The first search downloads only ID/name references; later queries reuse that index offline. Details and artwork load for composed result rows and reuse existing caches. Clearing search restores the Home list. See [search behavior](docs/search.md) for the migration, retry behavior, and manual test steps.

The repository reads saved pages and Pokemon details from Room before requesting the API. Page ordering, next-page cursors, and fetch timestamps survive app restarts. Missing pages are downloaded, stored transactionally, and then read from the database. Detail requests are limited to four at a time and retain the API's list ordering.

An app-wide Coil image loader uses a 100 MiB disk cache and a memory cache capped at 15% of available app memory. Image files stay outside Room; the database stores their URLs. The 20 MiB HTTP cache remains for server-cacheable API responses. See [local storage behavior](docs/local-storage.md) for cache policy, offline limitations, and database schema guidance.

The screen follows the saved Home screenshot. Dimensions in `ui/home/HomeDimensions` are estimates for manual tuning; the bundled system font is used because the design's SF Pro font is not included. Home currently uses a fixed light appearance to match that reference.

To verify the implementation:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# Requires a running Android emulator or device:
./gradlew :app:connectedDebugAndroidTest
```

## Working with AI

Read [AGENTS.md](AGENTS.md) for the project's AI collaboration instructions. For each prompt, the assistant should identify relevant capabilities, explain why they help, and use them when appropriate. We want to learn when to use each capability through practical work on this app.

See the [model-selection guide and decision diagram](docs/ai-model-guide.md) for task-based recommendations. Before changes, the assistant recommends a suitable model and reasoning effort. After application implementation, it installs and launches the tested app and leaves it ready for manual testing when a device is available.

AI-generated changes still require human review. Review the code, understand the decisions, and check the reported validation before accepting a change.
