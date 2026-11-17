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

Home displays Pokemon IDs, names, types, and API-provided artwork. It loads 20 Pokemon initially and automatically requests another page near the end of the list. Failed page loads offer retry while preserving previously loaded cards. Search, filters, sorting, and detail navigation are reserved for separate changes.

The repository caches successful pages and Pokemon details in memory, with an HTTP disk cache for server-cacheable API responses. Detail requests are limited to four at a time and retain the API's list ordering. Coil loads and caches artwork and renders the existing SVG assets.

The screen follows the saved Home screenshot. Dimensions in `ui/home/HomeDimensions` are estimates for manual tuning; the bundled system font is used because the design's SF Pro font is not included. Home currently uses a fixed light appearance to match that reference.

To verify the implementation:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
# Requires a running Android emulator or device:
./gradlew :app:connectedDebugAndroidTest
```

## Working with AI

Read [AGENTS.md](AGENTS.md) for the project's AI collaboration instructions. For each prompt, the assistant should identify relevant capabilities, explain why they help, and use them when appropriate. We want to learn when to use each capability through practical work on this app.

AI-generated changes still require human review. Review the code, understand the decisions, and check the reported validation before accepting a change.
