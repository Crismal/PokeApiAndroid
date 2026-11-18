from copy import deepcopy
import unittest

from scripts.ai_capabilities import percentage, record, render, statistics, validate


class CapabilityTrackerTest(unittest.TestCase):
    def setUp(self):
        self.data = {
            "schema_version": 1, "catalogue_version": 1, "tracking_started": "2026-11-18",
            "capabilities": [
                {"id": "rules", "name": "Rules", "purpose": "Instructions", "availability": "Available",
                 "historical_status": "used", "evidence": "Existing rule file"},
                {"id": "design", "name": "Design", "purpose": "Design context", "availability": "Blocked",
                 "historical_status": "attempted", "evidence": "Access denied"},
            ],
            "prompts": [],
        }

    def prompt(self, identifier, used=(), attempted=()):
        return {"id": identifier, "recorded_at_utc": "2026-11-18T10:00:00+00:00",
                "summary": "Task summary", "outcome": "Recorded result",
                "used": list(used), "attempted": list(attempted)}

    def test_blocked_attempts_do_not_inflate_successful_coverage(self):
        data = record(self.data, self.prompt("one", attempted=["design"]))
        stats = statistics(data)
        self.assertEqual(1, stats["covered"])
        self.assertEqual(0, stats["tracked_covered"])
        self.assertEqual(0, stats["used_counts"]["design"])
        self.assertEqual(1, stats["attempted_counts"]["design"])

    def test_every_prompt_counts_in_denominator_including_no_tool_replies(self):
        data = record(self.data, self.prompt("one", used=["rules"]))
        data = record(data, self.prompt("two"))
        stats = statistics(data)
        self.assertEqual(2, stats["prompts"])
        self.assertEqual("50.0%", percentage(stats["used_counts"]["rules"], stats["prompts"]))

    def test_project_status_includes_history_and_changes_only_after_success(self):
        data = record(self.data, self.prompt("one", attempted=["design"]))
        dashboard = render(data)
        self.assertIn("| Rules | Instructions | Yes | Available |", dashboard)
        self.assertIn("| Design | Design context | No | Blocked |", dashboard)
        for removed_column in ["Successful prompts / total", "| Usage |", "Blocked/failed prompt attempts"]:
            self.assertNotIn(removed_column, dashboard)
        data = record(data, self.prompt("two", used=["design"]))
        self.assertIn("| Design | Design context | Yes | Blocked |", render(data))

    def test_recovery_updates_same_prompt_without_double_counting(self):
        original = deepcopy(self.data)
        data = record(self.data, self.prompt("one", attempted=["design"]))
        data = record(data, self.prompt("one", used=["design"]))
        self.assertEqual(1, statistics(data)["prompts"])
        self.assertEqual(1, statistics(data)["used_counts"]["design"])
        self.assertEqual(0, statistics(data)["attempted_counts"]["design"])
        self.assertEqual(original, self.data)

    def test_unknown_or_contradictory_categories_are_rejected(self):
        for prompt in [self.prompt("one", used=["invented"]),
                       self.prompt("one", used=["rules"], attempted=["rules"]),
                       self.prompt("one", used=["rules", "rules"])]:
            with self.subTest(prompt=prompt), self.assertRaises(ValueError):
                record(self.data, prompt)

    def test_empty_history_has_no_invented_usage_rate_and_render_is_deterministic(self):
        self.assertEqual("N/A", percentage(0, 0))
        self.assertEqual(0, statistics(self.data)["prompts"])
        self.assertEqual(render(self.data), render(deepcopy(self.data)))

    def test_duplicate_prompt_ids_and_unsupported_schema_are_rejected(self):
        data = deepcopy(self.data)
        data["prompts"] = [self.prompt("one"), self.prompt("one")]
        with self.assertRaises(ValueError):
            validate(data)
        data = deepcopy(self.data)
        data["schema_version"] = 999
        with self.assertRaises(ValueError):
            validate(data)


if __name__ == "__main__":
    unittest.main()
