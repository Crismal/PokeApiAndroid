#!/usr/bin/env python3
"""Record one prompt and render an evidence-based capability dashboard (stdlib only)."""
import argparse
from copy import deepcopy
from datetime import datetime, timezone
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def validate(data):
    if data.get("schema_version") != 1:
        raise ValueError("Unsupported tracker schema")
    capabilities = data["capabilities"]
    ids = [capability["id"] for capability in capabilities]
    if not ids or len(ids) != len(set(ids)):
        raise ValueError("Capability IDs must be nonempty and unique")
    for capability in capabilities:
        if capability["historical_status"] not in {"used", "attempted", "not_observed"}:
            raise ValueError("Invalid historical status")
        if capability["historical_status"] != "not_observed" and not capability["evidence"]:
            raise ValueError("Historical use or attempts need evidence")
    prompts = data["prompts"]
    prompt_ids = [prompt["id"] for prompt in prompts]
    if len(prompt_ids) != len(set(prompt_ids)):
        raise ValueError("Prompt IDs must be unique")
    for prompt in prompts:
        if not prompt["id"].strip() or not prompt["summary"].strip():
            raise ValueError("Each prompt needs an ID and a short summary")
        instant = datetime.fromisoformat(prompt["recorded_at_utc"].replace("Z", "+00:00"))
        if instant.utcoffset() is None:
            raise ValueError("Prompt timestamps need a timezone")
        used, attempted = prompt["used"], prompt["attempted"]
        if len(used) != len(set(used)) or len(attempted) != len(set(attempted)):
            raise ValueError("Count a capability at most once per prompt")
        if set(used) & set(attempted):
            raise ValueError("Use attempted only for capabilities not successfully used in this prompt")
        if (set(used) | set(attempted)) - set(ids):
            raise ValueError("Unknown capability ID")


def statistics(data):
    validate(data)
    used_counts = {capability["id"]: 0 for capability in data["capabilities"]}
    attempted_counts = dict.fromkeys(used_counts, 0)
    covered = {c["id"] for c in data["capabilities"] if c["historical_status"] == "used"}
    tracked_covered = set()
    for prompt in data["prompts"]:
        for capability in prompt["used"]:
            used_counts[capability] += 1
            tracked_covered.add(capability)
            covered.add(capability)
        for capability in prompt["attempted"]:
            attempted_counts[capability] += 1
    return {
        "total": len(used_counts), "covered": len(covered),
        "tracked_covered": len(tracked_covered), "prompts": len(data["prompts"]),
        "used_counts": used_counts, "attempted_counts": attempted_counts,
    }


def percentage(count, total):
    return f"{100 * count / total:.1f}%" if total else "N/A"


def cell(value):
    return str(value).replace("|", "\\|").replace("\n", " ")


def render(data):
    stats = statistics(data)
    total, prompts = stats["total"], stats["prompts"]
    lines = [
        "# AI capability usage tracker", "",
        f"Catalogue version: {data['catalogue_version']}. Prompt tracking started: {data['tracking_started']}.", "",
        "This is an evidence-based learning log for this project. Percentages measure observed use, not AI potential, proficiency, time, token consumption, cost, or developer mastery. The catalogue is a curated inventory, not an exhaustive list of everything AI can do.", "",
        f"- **Observed project coverage: {percentage(stats['covered'], total)}** ({stats['covered']}/{total} categories successfully used in historical evidence or tracked prompts).",
        f"- **Coverage within the tracked log: {percentage(stats['tracked_covered'], total)}** ({stats['tracked_covered']}/{total} categories used in {prompts} logged prompts).",
        f"- **Prompts logged: {prompts}.** Earlier conversations have selected historical evidence, not reconstructed prompt counts.", "",
        "## How to read the status and coverage", "",
        "Used in this project is Yes when historical evidence or a logged prompt records successful use; otherwise it is No. No means no recorded successful use, including capabilities only attempted or blocked. Coverage is the percentage of catalogue categories successfully used at least once. Attempt details remain in the evidence and JSON ledger.", "",
        "Historical evidence establishes that a category was exercised at least once; it does not establish frequency or mastery. Not observed does not mean impossible. Availability notes describe exposed tools and known restrictions, not guaranteed account access. Do not use tools just to increase a percentage.", "",
        "## Capability inventory and usage", "",
        "| Capability | What it helps with | Used in this project | Availability / limits |",
        "| --- | --- | --- | --- |",
    ]
    for capability in data["capabilities"]:
        identifier = capability["id"]
        used = capability["historical_status"] == "used" or stats["used_counts"][identifier] > 0
        values = [capability["name"], capability["purpose"], "Yes" if used else "No", capability["availability"]]
        lines.append("| " + " | ".join(cell(value) for value in values) + " |")
    lines += ["", "## Evidence before tracking started", "",
              "This baseline uses visible conversation results and repository artifacts. It is not a complete historical audit. Commit references may belong to another project branch. Access failures are the last observed result and have not been re-tested just for this tracker.", "",
              "| Capability | Historical observation | Evidence or limitation |", "| --- | --- | --- |"]
    for capability in data["capabilities"]:
        lines.append("| " + " | ".join(cell(capability[key]) for key in ("name", "historical_status", "evidence")) + " |")
    lines += ["", "## Recent prompts", "", "Only the latest 20 entries are displayed; the JSON ledger retains all entries.", "",
              "| Prompt ID | Recorded UTC | Summary | Successfully used | Outcome |", "| --- | --- | --- | --- | --- |"]
    for prompt in data["prompts"][-20:]:
        values = [prompt["id"], prompt["recorded_at_utc"], prompt["summary"], ", ".join(prompt["used"]), prompt["outcome"]]
        lines.append("| " + " | ".join(cell(value) for value in values) + " |")
    lines += ["", "## Updating this document", "",
              "The source of truth is [ai-capabilities.json](ai-capabilities.json). This Markdown file is generated by [the recorder](../scripts/ai_capabilities.py); do not edit its numbers manually. The update rule lives in [AGENTS.md](../AGENTS.md).", "",
              "At the end of each user prompt, record only capabilities actually exercised. Use a short sanitized summary rather than copying prompts, credentials, personal data, or tool output. Assign one stable ID to the prompt and reuse it after retries or recovery: recording the same ID updates that entry instead of increasing the denominator. Tool results, progress messages, and internal recovery messages are not new user prompts.", "",
              "```sh", "# Replace IDs with categories actually used; an empty --used list is valid.",
              "python3 scripts/ai_capabilities.py record --id PROMPT_ID \\",
              "  --summary 'Short task summary' --outcome 'Verified result or limitation' \\",
              "  --used repo-instructions prompt-coaching documentation", "",
              "# Rebuild or check the dashboard without recording another prompt:",
              "python3 scripts/ai_capabilities.py render", "python3 scripts/ai_capabilities.py check", "```", "",
              "Use --attempted for a capability tried without success. Keep separate outcomes within a capability in the outcome note rather than counting both used and attempted for the same prompt. If a prompt prohibits file changes, queue its entry until writing is authorized and make the delay explicit. Logging does not authorize commits or pushes. Updates depend on the assistant following the rule; there is no background hook or access to unseen conversation history.", "",
              "When adding or retiring categories, bump catalogue_version and explain the change in the PR. Coverage is calculated against the current catalogue; comparisons between different catalogue versions need that context. Do not delete IDs referenced by prior prompts.", ""]
    return "\n".join(lines)


def record(data, prompt):
    updated = deepcopy(data)
    for index, existing in enumerate(updated["prompts"]):
        if existing["id"] == prompt["id"]:
            updated["prompts"][index] = prompt
            break
    else:
        updated["prompts"].append(prompt)
    validate(updated)
    return updated


def write_atomic(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_text(content, encoding="utf-8")
    temporary.replace(path)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--data", type=Path, default=ROOT / "docs/ai-capabilities.json")
    parser.add_argument("--output", type=Path, default=ROOT / "docs/ai-capabilities.md")
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("render")
    commands.add_parser("check")
    add = commands.add_parser("record")
    add.add_argument("--id", required=True)
    add.add_argument("--summary", required=True)
    add.add_argument("--outcome", required=True)
    add.add_argument("--used", nargs="*", default=[])
    add.add_argument("--attempted", nargs="*", default=[])
    args = parser.parse_args()
    data = json.loads(args.data.read_text(encoding="utf-8"))
    if args.command == "record":
        data = record(data, {
            "id": args.id, "recorded_at_utc": datetime.now(timezone.utc).isoformat(),
            "summary": args.summary, "outcome": args.outcome,
            "used": args.used, "attempted": args.attempted,
        })
    content = render(data)
    if args.command == "check":
        if not args.output.exists() or args.output.read_text(encoding="utf-8") != content:
            raise SystemExit("Dashboard is stale; run the render command")
        print("Tracker is valid and dashboard is current.")
    else:
        if args.command == "record":
            write_atomic(args.data, json.dumps(data, indent=2, ensure_ascii=False) + "\n")
        write_atomic(args.output, content)
        print(f"Rendered {len(data['prompts'])} prompt entries to {args.output}")


if __name__ == "__main__":
    main()
