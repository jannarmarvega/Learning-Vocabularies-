#!/usr/bin/env python3
"""Drops any words2.txt entry already defined in words.txt, and any repeated
within words2.txt itself. Keeps section headers and comments intact."""
import os, sys

BASE = os.path.dirname(os.path.abspath(__file__))

def keys(path):
    out = set()
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            out.add(line.split("|")[0].strip().lower())
    return out

existing = keys(os.path.join(BASE, "words.txt"))
src = os.path.join(BASE, "words2.txt")
seen, kept, dropped = set(), [], []

with open(src, encoding="utf-8") as f:
    for line in f:
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            kept.append(line.rstrip("\n"))
            continue
        key = stripped.split("|")[0].strip().lower()
        if key in existing or key in seen:
            dropped.append(key)
            continue
        seen.add(key)
        kept.append(line.rstrip("\n"))

with open(src, "w", encoding="utf-8") as f:
    f.write("\n".join(kept) + "\n")

print(f"dropped {len(dropped)} duplicate(s): {', '.join(dropped) if dropped else '-'}")
print(f"words2.txt now holds {len(seen)} entries")
