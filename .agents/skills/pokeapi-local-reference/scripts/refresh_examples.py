#!/usr/bin/env python3
"""Explicitly refresh the project's small PokeAPI sample set (no docs browsing)."""
import json
from datetime import datetime, timezone
from pathlib import Path
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[4]
OUTPUT = ROOT / "docs/api/pokeapi/examples"
PAGE_URL = "https://pokeapi.co/api/v2/pokemon?limit=20&offset=0"
DETAIL_URL = "https://pokeapi.co/api/v2/pokemon/1/"


def fetch_json(url):
    request = Request(url, headers={"Accept": "application/json", "User-Agent": "PokeApiAndroid-local-reference"})
    with urlopen(request, timeout=30) as response:
        if response.status != 200:
            raise ValueError(f"Unexpected HTTP status: {response.status}")
        return json.load(response)


def refresh():
    # Fetch and validate both documents before changing any existing samples.
    page = fetch_json(PAGE_URL)
    detail = fetch_json(DETAIL_URL)
    if not isinstance(page.get("results"), list) or not isinstance(page.get("count"), int):
        raise ValueError("Unexpected Pokemon page structure")
    if not all(isinstance(item.get("name"), str) and isinstance(item.get("url"), str) for item in page["results"]):
        raise ValueError("Unexpected Pokemon resource structure")
    if "next" not in page or "previous" not in page:
        raise ValueError("Missing page cursors")
    if not isinstance(detail.get("id"), int) or not isinstance(detail.get("name"), str):
        raise ValueError("Unexpected Pokemon identity")
    if not isinstance(detail.get("types"), list) or not isinstance(detail.get("sprites"), dict):
        raise ValueError("Unexpected Pokemon detail structure")
    sprites = detail["sprites"]
    artwork = (sprites.get("other") or {}).get("official-artwork") or {}
    reduced = {
        "id": detail["id"],
        "name": detail["name"],
        "types": detail["types"],
        "sprites": {
            "front_default": sprites.get("front_default"),
            "other": {"official-artwork": {"front_default": artwork.get("front_default")}},
        },
    }
    metadata = {
        "captured_at_utc": datetime.now(timezone.utc).isoformat(),
        "purpose": "Local project reference; examples are snapshots, not exhaustive schemas.",
        "sources": [
            {"url": PAGE_URL, "file": "pokemon-page.json", "reduced": False},
            {"url": DETAIL_URL, "file": "pokemon-detail.json", "reduced": True,
             "retained_fields": ["id", "name", "types", "sprites.front_default", "sprites.other.official-artwork.front_default"]},
        ],
    }
    documents = {"pokemon-page.json": page, "pokemon-detail.json": reduced, "metadata.json": metadata}
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name, value in documents.items():
        target = OUTPUT / name
        temporary = target.with_suffix(".json.tmp")
        temporary.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        temporary.replace(target)
    print(f"Refreshed {len(page['results'])} Pokemon references and one reduced detail example in {OUTPUT}")


if __name__ == "__main__":
    refresh()
