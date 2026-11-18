---
name: gitflow
description: Follow the project's GitFlow workflow when planning implementation branches, preparing commits or pull requests, or handling releases and hotfixes. Apply to all project development, including documentation and configuration changes; a request to explain the workflow does not authorize Git mutations.
---

# Project GitFlow

Use GitFlow throughout this project. Read AGENTS.md before acting and preserve the developer's working tree. Explain the chosen branch type, source, and pull-request target when starting a Git workflow.

## Branch lifecycle

| Branch | Purpose | Start from | Integrate into |
| --- | --- | --- | --- |
| `master` | Stable released code | Existing production branch | Receives reviewed releases and hotfixes |
| `develop` | Integration branch for the next release | `master` at initial setup | Release branches |
| `feature/descriptive-name` | New features | `develop` | `develop` through a PR |
| `bugfix/descriptive-name` | Fixes for unreleased work | `develop` | `develop` through a PR |
| `docs/descriptive-name` or `chore/descriptive-name` | Documentation, skills, configuration, and maintenance | `develop` | `develop` through a PR |
| `release/version` | Stabilization of an agreed release | `develop` | `master`, then back into `develop` |
| `hotfix/descriptive-name` | Urgent fixes to released code | `master` | `master`, then back into `develop` |

Use the existing `master` branch name; do not rename it to `main`. Existing `feat/` branches are legacy feature branches: do not rename, delete, or rewrite them merely to adopt this workflow.

## Before implementation

- Before every implementation task (features, fixes, refactoring, documentation, skills, or configuration), inspect the branch and working tree, then run `git fetch origin` to refresh the integration ref. Do not treat a local `develop` or an old remote-tracking ref as proof of freshness. If the fetch fails, report the blocker and do not begin implementation against an unverified base unless the developer explicitly permits offline work.
- Start new ordinary work from the freshly fetched `origin/develop`, for example `git switch -c feature/descriptive-name origin/develop`. If using local `develop`, verify it matches that ref first; fast-forward it when possible and do not reset or overwrite divergent local commits. Release and production-hotfix branches retain the bases defined in the branch lifecycle table.
- When continuing an existing ordinary work branch, check `git merge-base --is-ancestor origin/develop HEAD`. A successful check means the branch contains the latest fetched integration commit; it need not equal `develop`. If the check fails, preserve uncommitted work, merge `origin/develop` into the work branch, and resolve conflicts before implementation. Synchronizing an authorized work branch is part of this workflow; it does not authorize merging the PR into `develop` or `master`, changing its target, or rewriting published history. Keep work local and ask about material conflict-resolution decisions that cannot be inferred from the task.
- Fetch again before the PR handoff. If `origin/develop` advanced, synchronize the work branch and rerun checks affected by those changes before pushing. Never claim the branch is current without a successful fetch and ancestry check. Do not silently substitute `master` even if it contains changes absent from `develop`; report that integration gap when it affects the requested work.
- This repository did not have a `develop` branch when the skill was introduced. Verify current refs rather than assuming setup is complete. If it is still absent, explain the one-time setup and obtain authorization to establish and publish it from the agreed stable `master` commit before dependent branch work. Do not silently substitute `master` for `develop`.
- Preserve unrelated staged and unstaged changes. If switching would overwrite or carry unrelated changes into the wrong task, use an authorized isolated worktree or ask how to preserve them; do not discard or commit them.
- Do not implement directly on `master` or `develop`.

## Commits and pull requests

- Keep commits focused on the requested scope. Stage explicit paths and inspect the staged diff so unrelated staged files do not enter the commit.
- Run checks appropriate to the change and include actual validation results in the PR description.
- Target ordinary feature, bugfix, documentation, and maintenance PRs at `develop`. Target release and production-hotfix PRs at `master` and describe the required integration back into `develop`.
- Inspect the diff against the intended base so the PR contains only relevant changes. Do not rebase or force-push shared branches just to simplify a diff without explicit authorization.
- A request to implement does not authorize committing, pushing, creating a PR, merging, tagging, or publishing a release. Perform those actions only within the developer's explicit authorization; a request to create a PR includes pushing its required branch.
- Verify external actions actually succeeded. If GitHub access cannot create a PR, report the limitation accurately; a pushed branch or comparison link is not an opened PR.

## Releases and hotfixes

- Start release work only when requested, using a version agreed with the developer. Limit release branches to stabilization, release metadata, and necessary fixes.
- After an approved release merge into `master`, create the agreed version tag only when authorized and integrate the release changes back into `develop`. A tag does not by itself authorize deployment or app-store publication.
- For an authorized production hotfix, branch from `master`, validate the fix, and arrange reviewed integration into both `master` and `develop`. If a release is active, assess whether it needs the fix too and make that follow-up explicit.
- Preserve merge ancestry when possible so integration back into `develop` does not lose release or hotfix changes. Avoid squash-only release flows that make this reconciliation ambiguous.
- Delete completed branches only when requested or covered by an explicitly agreed cleanup policy.
