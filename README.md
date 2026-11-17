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
| Architecture | MVVM (Model–View–ViewModel), the intended architecture as features are implemented |
| Minimum Android SDK | API 24 (Android 7.0) |
| Target Android SDK | API 37 |
| Compile Android SDK | API 37 |

SDK values reflect the current configuration in [app/build.gradle.kts](app/build.gradle.kts). Dependency versions are defined in [gradle/libs.versions.toml](gradle/libs.versions.toml).

With MVVM, Compose screens display UI state and send user actions to ViewModels. ViewModels manage screen state and coordinate with the data layer. Repositories provide access to PokeAPI data, keeping network calls out of composables.

## Working with AI

Read [AGENTS.md](AGENTS.md) for the project's AI collaboration instructions. For each prompt, the assistant should identify relevant capabilities, explain why they help, and use them when appropriate. We want to learn when to use each capability through practical work on this app.

AI-generated changes still require human review. Review the code, understand the decisions, and check the reported validation before accepting a change.
