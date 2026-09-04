#!/usr/bin/env python3
"""Validates words2.txt: field counts, empty fields, stray characters."""
import os, re, sys

BASE = os.path.dirname(os.path.abspath(__file__))
src = os.path.join(BASE, "words2.txt")
ALLOWED_EXTRA = set("áéíóúñÑàèìòùâêîôûäëïöüçÁÉÍÓÚ‘’“”–—æÆœŒ")
problems = 0

with open(src, encoding="utf-8") as f:
    for n, line in enumerate(f, 1):
        s = line.rstrip("\n")
        if not s.strip() or s.lstrip().startswith("#"):
            continue
        parts = s.split("|")
        if len(parts) != 6:
            print(f"{n}: expected 6 fields, got {len(parts)}: {s[:70]}")
            problems += 1
            continue
        for i, p in enumerate(parts):
            if not p.strip():
                print(f"{n}: empty field {i+1}: {s[:70]}")
                problems += 1
        bad = [c for c in s if ord(c) > 127 and c not in ALLOWED_EXTRA]
        if bad:
            print(f"{n}: stray character(s) {bad}: {s[:70]}")
            problems += 1

print(f"\n{problems} problem(s) found" if problems else "\nNo problems found")
sys.exit(1 if problems else 0)
