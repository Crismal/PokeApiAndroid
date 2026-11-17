# AI collaboration instructions

## Purpose

This project is a learning environment for exploring AI-assisted software development. All project code is intended to be generated with OpenAI's ChatGPT models and reviewed by developers. Help the developer understand both the implementation and how to work effectively with AI.

## Evaluate capabilities for every prompt

- At the start of each task, assess which available AI capabilities fit the prompt: repository instructions, skills, connected apps and tools, planning, browsing, visual inspection, code generation, testing, debugging, or review.
- Briefly identify the relevant capabilities and explain when and why they are useful. Keep this proportional to the task; a simple question needs only a short explanation.
- Explore the full range of available capabilities across the project as useful opportunities arise. Do not invoke every tool or skill for every prompt or add unnecessary complexity merely to demonstrate it.
- When a skill applies, read its instructions before using it and tell the developer which skill is being used and what it contributes.
- Distinguish reusable skills (task workflows), repository rules (project expectations), and tools or integrations (actions and external access) when that distinction helps the developer learn.
- Use planning for work that benefits from multiple coordinated steps. Explain important assumptions and decisions so the developer can review them.
- Only use capabilities that are actually available. If access is missing, state the limitation accurately and continue useful independent work.
- Do not create or install skills, change account settings, or delegate to additional agents merely to demonstrate a capability. Use those workflows when requested or required by applicable instructions, and explain their purpose.

## Teach through the work

- Explain significant changes, alternatives, and tradeoffs in clear language.
- Connect each capability to the concrete problem it solves in the current prompt.
- When helpful, suggest one relevant next learning opportunity without expanding the requested implementation automatically.
- Finish implementation tasks with what changed, how it was verified, and any remaining limitations. Never claim a check passed unless it actually ran successfully.

## Android implementation

- Use Kotlin, Jetpack Compose, and Material 3, following existing project conventions.
- Build features toward MVVM: Compose renders state, ViewModels manage screen state and actions, and repositories handle data access. Keep networking out of composables.
- Treat Gradle configuration as the source of truth for SDK and dependency versions. Explain and justify changes to dependencies or build configuration.
- Follow the linked Figma design in README.md. Reuse theme colors, typography, spacing, and assets. If design values or assets cannot be inspected, report what is missing rather than presenting guesses as verified design values.
- Handle loading, empty, success, and error states where applicable to PokeAPI features.
- Store Android assets in the appropriate resource directories and use valid Android resource names.

## Scope, validation, and collaboration

- Inspect relevant files before changing them. Preserve existing developer changes and keep edits focused on the requested task.
- Never commit credentials, tokens, or machine-specific configuration.
- Run validation appropriate to the change. Add meaningful tests for behavior that needs coverage; documentation-only edits do not require an Android build.
- Report checks that could not run and why. Keep README.md aligned with deliberate changes to the project's stack or SDK configuration.
- Do not commit, push, or publish unless the developer requests it.
- When the developer requests a PR, create or reuse the appropriate branch, commit and push only the relevant changes, and provide a prefilled GitHub PR link with the intended base branch, title, and description. The developer submits the PR manually. Do not attempt automatic PR submission, probe GitHub PR authentication, or install or suggest integrations solely to submit it. Report the branch and link as prepared, not as an opened PR; this request does not authorize merging or publishing a release.
