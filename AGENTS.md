# AI collaboration instructions

## Purpose

This project is a learning environment for exploring AI-assisted software development. All project code is intended to be generated with OpenAI's ChatGPT models and reviewed by developers. Help the developer understand both the implementation and how to work effectively with AI.

## Evaluate capabilities for every prompt

- Before changing files or running mutating commands, briefly assess the task's scope, uncertainty, risk, and verification needs. Recommend an available model and reasoning effort with a short reason, using [the model-selection guide](docs/ai-model-guide.md). Prefer the least costly suitable option rather than defaulting every task to the strongest model. A short assessment is enough for a routine command.
- Distinguish model selection from agent delegation and skill selection. Do not claim to have switched models without a supported tool and verified result. Keep the developer in control of model changes; explain how to select the recommended option when requested. If the developer asks to choose a model before proceeding, wait for that choice before changes. Otherwise, the recommendation does not add a confirmation gate to an already authorized task. Do not spawn additional agents merely to demonstrate routing.
- At the start of each task, assess which available AI capabilities fit the prompt: repository instructions, skills, connected apps and tools, planning, browsing, visual inspection, code generation, testing, debugging, or review.
- Briefly identify the relevant capabilities and explain when and why they are useful. Keep this proportional to the task; a simple question needs only a short explanation.
- Explore the full range of available capabilities across the project as useful opportunities arise. Do not invoke every tool or skill for every prompt or add unnecessary complexity merely to demonstrate it.
- When a skill applies, read its instructions before using it and tell the developer which skill is being used and what it contributes.
- Distinguish reusable skills (task workflows), repository rules (project expectations), and tools or integrations (actions and external access) when that distinction helps the developer learn.
- Use planning for work that benefits from multiple coordinated steps. Explain important assumptions and decisions so the developer can review them.
- Only use capabilities that are actually available. If access is missing, state the limitation accurately and continue useful independent work.
- Do not create or install skills, change account settings, or delegate to additional agents merely to demonstrate a capability. Use those workflows when requested or required by applicable instructions, and explain their purpose.

## Teach through the work

- Apply the local [prompt-coach skill](.agents/skills/prompt-coach/SKILL.md) to every user prompt in this project, including questions, follow-ups, implementation requests, and Git commands. Begin with brief feedback grounded in the conversation context, suggest a rewrite when useful, then answer or execute the authorized task. If a prompt is already sufficient, explain why without inventing flaws. Routine coaching does not require confirmation; keep longer introductions subject to the developer's learning preferences.
- For complex prompts, also present two distinct improved ways to express the request when meaningful alternatives are possible. Treat a prompt as complex when it combines multiple requirements, constraints, decisions, or deliverables, or contains ambiguity that could materially affect the outcome. Preserve the user's intent, scope, and authorization in both versions; do not introduce new features or permissions. Keep the alternatives concise and explain their benefit when useful. If two meaningful improvements are not possible, say the original is sufficient rather than inventing flaws or redundant rewrites.
- Do not provide alternative phrasings for simple replies such as "yes," "no," acknowledgments, or clear routine commands. Keep any feedback proportional to the message. Prompt coaching must not require confirmation or delay an otherwise clear, authorized task; a rewritten prompt is a suggestion, not a new instruction to execute.
- Explain significant changes, alternatives, and tradeoffs in clear language.
- Connect each capability to the concrete problem it solves in the current prompt.
- When helpful, suggest one relevant next learning opportunity without expanding the requested implementation automatically.
- Finish implementation tasks with what changed, how it was verified, and any remaining limitations. Never claim a check passed unless it actually ran successfully.

## Android implementation

- For every new implementation or application-code change, read and apply the project skill [.agents/skills/solid-clean-code/SKILL.md](.agents/skills/solid-clean-code/SKILL.md). Use SOLID principles and Clean Code with complexity proportional to the task.
- Use Kotlin, Jetpack Compose, and Material 3, following existing project conventions.
- Build features toward MVVM: Compose renders state, ViewModels manage screen state and actions, and repositories handle data access. Keep networking out of composables.
- Treat Gradle configuration as the source of truth for SDK and dependency versions. Explain and justify changes to dependencies or build configuration.
- Follow the linked Figma design in README.md. Reuse theme colors, typography, spacing, and assets. If design values or assets cannot be inspected, report what is missing rather than presenting guesses as verified design values.
- Handle loading, empty, success, and error states where applicable to PokeAPI features.
- For PokeAPI work, apply the [local API reference skill](.agents/skills/pokeapi-local-reference/SKILL.md) and consult [docs/api/pokeapi/README.md](docs/api/pokeapi/README.md) first. Do not reopen the online API documentation during routine implementation; document knowledge gaps and use the skill's explicit refresh workflow when needed.
- Store Android assets in the appropriate resource directories and use valid Android resource names.

## Scope, validation, and collaboration

- Maintain the [AI capability tracker](docs/ai-capabilities.md) after every processed user prompt, including questions, confirmations, and Git commands, before the final response. Record one sanitized summary and the capabilities actually used with `python3 scripts/ai_capabilities.py record`; follow the document's metric definitions and use a stable prompt ID. Reuse that ID after retries or daemon recovery rather than counting the same user prompt twice. Do not log internal recovery/context messages, tool results, or progress updates as user prompts.
- Keep successful use separate from blocked or failed attempts, and do not infer model switches, delegation, account access, token cost, or mastery from a recommendation or exposed tool. Preserve historical evidence without inventing past per-prompt counts. Regenerate the tracker and run `python3 scripts/ai_capabilities.py check` before handoff; do not exercise unrelated capabilities to improve percentages. If the developer prohibits writes for a prompt, defer its log update until writes are allowed and explain the gap. Tracker updates do not authorize commits or pushes; include relevant tracker changes when a PR is requested.
- Follow the project [GitFlow skill](.agents/skills/gitflow/SKILL.md) for all development branches, commits, pull requests, releases, and hotfixes. Use `master` for stable releases and `develop` for integration; ordinary work branches from and targets `develop`. Before every implementation and again before its PR handoff, fetch origin and verify that the work branch includes the latest `origin/develop`; synchronize the work branch and revalidate affected behavior if it is behind. Follow the skill's offline, conflict, release/hotfix, and initial setup guidance when applicable.
- Inspect relevant files before changing them. Preserve existing developer changes and keep edits focused on the requested task.
- Never commit credentials, tokens, or machine-specific configuration.
- Run validation appropriate to the change. Add meaningful tests for behavior that needs coverage; documentation-only edits do not require an Android build.
- After every application-code implementation, apply the [Android manual test handoff skill](.agents/skills/android-manual-test-handoff/SKILL.md): build and install the current debug app, launch it, verify that it stays open, and leave the relevant screen ready for developer testing. Preserve existing app data and device settings. Name the device, screen, and build tested, provide a short manual test path, and invite feedback with steps, expected result, and actual result. If blocked, report the exact limitation and launch steps; do not claim the app is running. Documentation-only changes do not require rebuilding or relaunching unless requested.
- Report checks that could not run and why. Keep README.md aligned with deliberate changes to the project's stack or SDK configuration.
- Do not commit, push, or publish unless the developer requests it.
- When the developer requests a PR, create or reuse the appropriate branch, commit and push only the relevant changes, and provide a prefilled GitHub PR link with the intended base branch, title, and description. The developer submits the PR manually. Do not attempt automatic PR submission, probe GitHub PR authentication, or install or suggest integrations solely to submit it. Report the branch and link as prepared, not as an opened PR; this request does not authorize merging or publishing a release.
