---
name: solid-clean-code
description: Apply SOLID principles and Clean Code when implementing or changing application code in this Kotlin, Jetpack Compose, and MVVM project. Use for new features, bug fixes, and refactoring; documentation-only and asset-only tasks do not require this workflow.
---

# SOLID and Clean Code

For every new implementation, apply the principles below to the code being added or changed. Keep solutions proportional to the feature; SOLID does not require an interface, use case, or module for every class. Preserve the requested behavior and avoid unrelated refactoring.

## Inspect and design

- Read the relevant implementation, tests, dependencies, and repository instructions before editing.
- Identify responsibilities, data flow, and external boundaries. Reuse existing abstractions when they fit.
- Briefly explain the principles that influence the current design and the concrete problem they address. Avoid reciting every principle when it has no bearing on the task.

## Apply SOLID

- **Single responsibility:** Give each class or composable a cohesive responsibility and reason to change. Keep API access and response mapping in the data layer, screen state and actions in ViewModels, and rendering in composables.
- **Open/closed:** Make meaningful variations replaceable through composition or focused abstractions. Extend behavior without repeatedly modifying unrelated consumers. Do not build speculative extension points for imagined requirements.
- **Liskov substitution:** Implementations must honor their contracts, including errors, cancellation, ordering, and state behavior. A test fake must obey the same contract as the production implementation; do not silently add preconditions or unsupported operations.
- **Interface segregation:** Expose only the operations a consumer needs. Prefer small, cohesive contracts over broad service interfaces. Do not create one-method interfaces solely to satisfy a checklist.
- **Dependency inversion:** Inject external dependencies into state and business logic at meaningful boundaries. ViewModels should depend on repository contracts when substitution or testing is useful. Keep concrete construction at an application composition point or factory, without introducing a dependency injection framework just for this rule.

## Write clean Kotlin and Compose

- Use descriptive domain names, small cohesive functions, explicit state models, and immutable values where practical.
- Keep side effects explicit and out of composable rendering. Use lifecycle-aware state collection and intentional effect keys.
- Use structured concurrency, keep blocking I/O off the main thread, and propagate coroutine cancellation rather than displaying it as a failure.
- Distinguish expected failures from programming defects. Handle failures at an appropriate boundary and expose actionable UI state without leaking sensitive implementation details.
- Reuse established theme tokens, resources, and components. Extract duplication when it represents a shared concept; avoid a generic framework for superficially similar code.
- Remove dead code introduced by the change. Use comments to explain intent or a non-obvious constraint rather than narrating syntax.
- Keep public APIs and visibility as narrow as practical, and avoid unnecessary dependencies or changes to project architecture.

## Verify and teach

- Review the changed code for mixed responsibilities, hidden dependencies, broken contracts, duplication, and unnecessary abstractions.
- Test observable behavior and important boundaries with suitable fakes or controlled responses. For pagination, consider duplicate requests, ordering, retries, exhausted pages, and preservation of existing items on failure.
- Run checks appropriate to the change and report actual results and limitations. Do not claim adherence is guaranteed by a skill or a passing build.
- In the completion message, explain a significant SOLID or Clean Code decision when it helps the developer understand the implementation.
