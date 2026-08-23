#!/usr/bin/env python3
"""118-completeness and schema checks (ARCHITECTURE_PLAN §8, CONTENT_WRITING_PLAN DoD)."""

from __future__ import annotations

import json
import re
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "shared-data"
errors: list[str] = []


def load(name: str):
    return json.loads((DATA / name).read_text(encoding="utf-8"))


def err(msg: str) -> None:
    errors.append(msg)


def parse_formula(formula: str) -> dict[str, int]:
    body = re.sub(r"[+\-]\d*$", "", formula)
    body = re.sub(r"[+\-]$", "", body)
    merged: dict[str, int] = {}

    def add(sym: str, n: int) -> None:
        merged[sym] = merged.get(sym, 0) + n

    i = 0
    while i < len(body):
        if body[i] == "(":
            j = body.index(")", i)
            inner = parse_formula(body[i + 1 : j])
            k = j + 1
            num = ""
            while k < len(body) and body[k].isdigit():
                num += body[k]
                k += 1
            mul = int(num) if num else 1
            for s, n in inner.items():
                add(s, n * mul)
            i = k
            continue
        m = re.match(r"([A-Z][a-z]?)(\d*)", body[i:])
        if not m:
            raise ValueError(formula)
        add(m.group(1), int(m.group(2) or 1))
        i += m.end()
    return merged


def main() -> int:
    elements = load("elements.json")
    atoms = load("atoms.json")
    molecules = load("molecules.json")
    quiz = load("quiz/all.json")

    if len(elements) != 118:
        err(f"elements.length={len(elements)} != 118")
    zs = [e["z"] for e in elements]
    if zs != list(range(1, 119)):
        err("elements z not contiguous 1..118")
    symbols = [e["symbol"] for e in elements]
    if len(set(symbols)) != 118:
        err("duplicate symbols")
    positions = [(e["displayRow"], e["displayColumn"]) for e in elements]
    if len(positions) != len(set(positions)):
        err("displayRow/Column collision")
    for e in elements:
        for req in ("z", "symbol", "nameKo", "nameEn", "period", "category",
                    "stateAtRoomTemp", "displayRow", "displayColumn", "card", "battle"):
            if req not in e:
                err(f"Z={e.get('z')} missing {req}")
        if not e["nameKo"]:
            err(f"Z={e['z']} empty nameKo")
        if e["z"] == 118 and e.get("hasKnownMolecules") is not False:
            err("Og must have hasKnownMolecules=false")

    if len(atoms) != 118:
        err(f"atoms.length={len(atoms)} != 118")
    for a in atoms:
        if a["defaultProtons"] != a["z"]:
            err(f"atom Z={a['z']} protons != z")
        if a["defaultElectrons"] != a["z"]:
            err(f"atom Z={a['z']} electrons != z")
        if sum(a["shellDistribution"]) != a["z"]:
            err(f"atom Z={a['z']} shell sum {sum(a['shellDistribution'])} != z")

    if len(molecules) < 120:
        err(f"molecules {len(molecules)} < 120 (Tier A+B)")
    ids = [m["id"] for m in molecules]
    if len(ids) != len(set(ids)):
        err("duplicate molecule ids")
    for m in molecules:
        parsed = parse_formula(m["formula"])
        given = {p["symbol"]: p["count"] for p in m["elements"]}
        if parsed != given:
            err(f"{m['id']} formula parse {parsed} != {given}")
        if not m.get("descriptionKo"):
            err(f"{m['id']} missing descriptionKo")

    linked = [e for e in elements if e.get("relatedMoleculeIds")]
    if len(linked) < 15:
        err(f"element-molecule links {len(linked)} < 15")
    for z in (1, 6, 7, 8, 11, 17):
        el = elements[z - 1]
        if not el.get("relatedMoleculeIds"):
            err(f"education element Z={z} missing relatedMoleculeIds")

    if len(quiz) < 150:
        err(f"quiz count {len(quiz)} < 150")
    for q in quiz:
        if not q.get("explanationKo") or len(q["explanationKo"]) < 10:
            err(f"{q['id']} explanation too short")
        if q.get("difficulty") not in (1, 2, 3):
            err(f"{q['id']} bad difficulty")

    if errors:
        print("VALIDATE FAIL")
        for e in errors:
            print(" -", e)
        return 1
    print(f"VALIDATE OK  elements=118 atoms=118 molecules={len(molecules)} quiz={len(quiz)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
