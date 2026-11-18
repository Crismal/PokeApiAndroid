# AI capability usage tracker

Catalogue version: 1. Prompt tracking started: 2026-11-18.

This is an evidence-based learning log for this project. Percentages measure observed use, not AI potential, proficiency, time, token consumption, cost, or developer mastery. The catalogue is a curated inventory, not an exhaustive list of everything AI can do.

- **Observed project coverage: 72.0%** (18/25 categories successfully used in historical evidence or tracked prompts).
- **Coverage within the tracked log: 60.0%** (15/25 categories used in 8 logged prompts).
- **Prompts logged: 8.** Earlier conversations have selected historical evidence, not reconstructed prompt counts.

## How to read the status and coverage

Used in this project is Yes when historical evidence or a logged prompt records successful use; otherwise it is No. No means no recorded successful use, including capabilities only attempted or blocked. Coverage is the percentage of catalogue categories successfully used at least once. Attempt details remain in the evidence and JSON ledger.

Historical evidence establishes that a category was exercised at least once; it does not establish frequency or mastery. Not observed does not mean impossible. Availability notes describe exposed tools and known restrictions, not guaranteed account access. Do not use tools just to increase a percentage.

## Capability inventory and usage

| Capability | What it helps with | Used in this project | Availability / limits |
| --- | --- | --- | --- |
| Repository rules | Apply durable project conventions and scope boundaries | Yes | Available through AGENTS.md |
| Prompt coaching | Assess clarity and suggest useful improved prompts | Yes | Local prompt-coach skill |
| Using skills | Apply a reusable task workflow | Yes | Available skills depend on session and branch |
| Creating skills | Write reusable instructions and supporting resources | Yes | Local files; .agents writes can require approval |
| Task planning and progress | Break work into steps and report findings | Yes | Available in conversation |
| Primary agent execution | Carry out authorized multi-step repository work | Yes | Current assistant and exposed tools |
| Subagent delegation | Assign independent work to additional agents | No | Delegation tools exist; explicit authorization required |
| Model and effort recommendations | Match task complexity to a suitable model | Yes | Recommendations supported; see model guide |
| Automatic model switching | Change the running conversation model | No | No switching tool exposed; developer selects the model |
| Repository inspection | Read files, dependencies, Git history, and diffs | Yes | Shell and filesystem access subject to sandbox |
| Code generation and debugging | Implement features, scripts, fixes, and refactoring | Yes | File-editing and shell tools |
| Automated verification | Build, test, lint, and validate results | Yes | Local toolchains and permissions required |
| Running the app for manual review | Install, launch, inspect, and leave an Android build open | Yes | ADB and an available emulator or designated device |
| Visual inspection | Inspect screenshots and rendered app screens | Yes | Local image viewing and device screenshots |
| Diagrams and visual explanations | Author code-native diagrams to explain decisions | Yes | Mermaid in repository documentation; rendering depends on viewer |
| Local knowledge and references | Reuse project-specific contracts and documentation | Yes | Repository documents and JSON examples |
| Online research | Check current primary documentation when needed | Yes | Web tool exposed; access can fail |
| Connected tools and integration discovery | Inspect available integrations and invoke relevant MCP tools | Yes | Tools exposed do not guarantee service or file access |
| Documentation and synthesis | Write guides, decisions, references, and reports | Yes | Repository editing and conversation |
| Git workflow and review preparation | Create branches, commit, push, and resolve conflicts | Yes | Git access subject to approval; PR submission is manual |
| AI image generation and editing | Create raster visual assets from a prompt or reference | No | Image generation tool exposed; not exercised here |
| Website building and hosting | Create or publish a website through Sites | No | Sites tools exposed; account/hosting readiness unverified |
| Scheduled automation | Arrange recurring work where supported | No | Sites scheduling is exposed; no general automation tool verified |
| General desktop or browser control | Operate desktop/browser interfaces through a control integration | No | No general graphical computer-use tool exposed in this session |
| Structured Figma design access | Retrieve file-level design context, tokens, or assets | No | Figma tools exposed; last attempted file access was denied |

## Evidence before tracking started

This baseline uses visible conversation results and repository artifacts. It is not a complete historical audit. Commit references may belong to another project branch. Access failures are the last observed result and have not been re-tested just for this tracker.

| Capability | Historical observation | Evidence or limitation |
| --- | --- | --- |
| Repository rules | used | AGENTS.md has guided implementations, validation, and manual PR preparation. |
| Prompt coaching | used | .agents/skills/prompt-coach/SKILL.md and the two-alternatives rule in AGENTS.md. |
| Using skills | used | SOLID, GitFlow, local API reference, skill-creator, and Figma guidance were read for relevant tasks. |
| Creating skills | used | Project skills for SOLID, GitFlow, prompt coaching, and local PokeAPI documentation were created. |
| Task planning and progress | used | Home, Room, and search work used staged implementation, validation, and review handoffs. |
| Primary agent execution | used | Home, Room persistence, and local search were implemented by the primary assistant. |
| Subagent delegation | not_observed | No subagents were spawned in the visible project history. |
| Model and effort recommendations | used | docs/ai-model-guide.md and recommendations before implementation and Git tasks. |
| Automatic model switching | not_observed | Recommendations do not establish that a model was switched or which model the developer actually used. |
| Repository inspection | used | Gradle, source, schemas, and Git changes were inspected before edits. |
| Code generation and debugging | used | 4d6e899 Home; a08c02a Room; fc06780 local search, including the keyboard-input fix. |
| Automated verification | used | fc06780 validation recorded 25 unit tests, 23 device tests, and 0 lint errors. |
| Running the app for manual review | used | Pixel_8a_API_34 was left on Home and later on live search results. |
| Visual inspection | used | Saved Figma screenshots and emulator captures were inspected, including the search typing defect. |
| Diagrams and visual explanations | used | docs/ai-model-guide.md contains a model-selection decision diagram; no render validation is claimed. |
| Local knowledge and references | used | docs/api/pokeapi/ is consulted instead of reopening online PokeAPI docs for routine tasks. |
| Online research | used | Official Android, Coil, Kotlin, and OpenAI guidance informed implementation and the model guide. |
| Connected tools and integration discovery | used | Plugin discovery returned installation state; Figma create_session also returned an access denial. |
| Documentation and synthesis | used | README.md, AGENTS.md, docs/local-storage.md, and local API documents were written. |
| Git workflow and review preparation | used | Feature/documentation branches were pushed; c41a9f2 resolved a conflict using an explicit force-with-lease. |
| AI image generation and editing | not_observed | Existing artwork and icons were reused; no AI image-generation call was made. |
| Website building and hosting | not_observed | The Android project has not used Sites or deployed a website. |
| Scheduled automation | not_observed | No persistent scheduled task was created. Local recorder scripts are not scheduled automation. |
| General desktop or browser control | not_observed | Android interaction via ADB is tracked under manual handoff; it does not prove desktop browser control. |
| Structured Figma design access | attempted | On 2026-11-17, create_session for the Pokedex file reported missing edit access. Screenshots were used instead; no successful structured read is claimed. |

## Recent prompts

Only the latest 20 entries are displayed; the JSON ledger retains all entries.

| Prompt ID | Recorded UTC | Summary | Successfully used | Outcome |
| --- | --- | --- | --- | --- |
| 2026-11-18-ai-capability-tracker | 2026-11-18T08:48:13.482064+00:00 | Create an evidence-based AI capability tracker and a per-prompt update rule. | repo-instructions, prompt-coaching, skill-use, task-planning, primary-agent, model-recommendation, repository-inspection, code-implementation, automated-verification, local-knowledge, documentation, git-workflow | Dashboard and recorder implemented; six tests passed; documentation prepared for manual PR review. |
| 2026-11-18-simplify-capability-status | 2026-11-18T09:06:49.797016+00:00 | Replace capability frequency columns with a single project usage status. | repo-instructions, prompt-coaching, skill-use, primary-agent, model-recommendation, repository-inspection, code-implementation, automated-verification, local-knowledge, documentation, git-workflow | Added Yes/No project usage status based on historical and logged success; seven tests passed. |
| 2026-11-18-amend-capability-tracker | 2026-11-18T09:08:03.545190+00:00 | Amend the tracker commit and push the existing review branch. | repo-instructions, prompt-coaching, skill-use, primary-agent, model-recommendation, repository-inspection, automated-verification, local-knowledge, documentation, git-workflow | Tracker checks and seven tests verified before amending and pushing the review branch. |
| 2026-11-18-android-manual-test-skill | 2026-11-18T09:13:51.228813+00:00 | Create a reusable skill to leave Android features ready for manual testing and feedback. | repo-instructions, prompt-coaching, skill-use, skill-authoring, primary-agent, model-recommendation, repository-inspection, automated-verification, local-knowledge, documentation, git-workflow | Created the handoff skill and linked AGENTS.md. Ruby YAML and naming checks passed; bundled validator blocked by missing PyYAML. Documentation only; app not relaunched. |
| 2026-11-18-pr-manual-test-skill | 2026-11-18T09:16:04.047237+00:00 | Prepare the latest manual-testing skill changes for PR review. | repo-instructions, prompt-coaching, skill-use, primary-agent, model-recommendation, repository-inspection, automated-verification, local-knowledge, documentation, git-workflow | Prepared the skill, repository rule, and tracker updates for committing to the existing documentation review branch. |
| 2026-11-18-pokemon-filters | 2026-11-18T09:22:49.905512+00:00 | Implement screenshot-based Pokémon filters and clarify undefined behavior. | repo-instructions, prompt-coaching, skill-use, task-planning, primary-agent, model-recommendation, repository-inspection, visual-inspection, local-knowledge, documentation, git-workflow | Inspected all screenshots, data contracts, and code. Located the complete filter layout; awaiting matching, size boundary, and catalogue coverage decisions before implementation. |
| 2026-11-18-pokemon-filter-selection-rules | 2026-11-18T09:47:39.747354+00:00 | Implement cache-based Filters with up to three selections per category and a 1–1000 or higher cached-ID range. | repo-instructions, prompt-coaching, skill-use, task-planning, primary-agent, model-recommendation, repository-inspection, code-implementation, automated-verification, manual-handoff, visual-inspection, local-knowledge, documentation, git-workflow | Implemented filter sheet, draft Apply/Reset, cached matching, size metadata and type-defense persistence, and non-destructive migrations to schema 3. Build, lint, 30 unit tests and 18 device tests passed. Debug app launched on Pixel_8a_API_34; height/weight silhouettes unavailable, labelled controls used. |
| 2026-11-18-filters-pr-and-default-handoff | 2026-11-18T09:54:32.149797+00:00 | Prepare the Filters PR and authorize automatic PR preparation after completed repository changes. | repo-instructions, prompt-coaching, skill-use, skill-authoring, task-planning, primary-agent, model-recommendation, repository-inspection, automated-verification, local-knowledge, documentation, git-workflow | Added standing authorization in AGENTS.md, updated GitFlow and README, and prepared Filters plus workflow changes for commit and push. Verified prior 30 unit tests, 18 device tests, lint with zero errors, skill YAML, and tracker consistency. PR submission remains manual. |

## Updating this document

The source of truth is [ai-capabilities.json](ai-capabilities.json). This Markdown file is generated by [the recorder](../scripts/ai_capabilities.py); do not edit its numbers manually. The update rule lives in [AGENTS.md](../AGENTS.md).

At the end of each user prompt, record only capabilities actually exercised. Use a short sanitized summary rather than copying prompts, credentials, personal data, or tool output. Assign one stable ID to the prompt and reuse it after retries or recovery: recording the same ID updates that entry instead of increasing the denominator. Tool results, progress messages, and internal recovery messages are not new user prompts.

```sh
# Replace IDs with categories actually used; an empty --used list is valid.
python3 scripts/ai_capabilities.py record --id PROMPT_ID \
  --summary 'Short task summary' --outcome 'Verified result or limitation' \
  --used repo-instructions prompt-coaching documentation

# Rebuild or check the dashboard without recording another prompt:
python3 scripts/ai_capabilities.py render
python3 scripts/ai_capabilities.py check
```

Use --attempted for a capability tried without success. Keep separate outcomes within a capability in the outcome note rather than counting both used and attempted for the same prompt. If a prompt prohibits file changes, queue its entry until writing is authorized and make the delay explicit. Logging does not authorize commits or pushes. Updates depend on the assistant following the rule; there is no background hook or access to unseen conversation history.

When adding or retiring categories, bump catalogue_version and explain the change in the PR. Coverage is calculated against the current catalogue; comparisons between different catalogue versions need that context. Do not delete IDs referenced by prior prompts.
