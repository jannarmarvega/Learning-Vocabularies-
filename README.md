# My Dictionary

An English–Tagalog learner's dictionary, available as an Android app and as a
self-hosted web app. Both read the same SQLite database: **5,762 words** across
72 categories, each with an English definition, a Tagalog translation, a Tagalog
definition and an example sentence.

## What's here

| Path | What it is |
| --- | --- |
| `app/` | The Android app — Kotlin, Jetpack Compose, Room |
| `web/` | The web app — Nuxt 4, SQLite via better-sqlite3, runs in Docker |
| `tools/` | The word list and the script that builds the database |

## Running the web app

```bash
cd web
docker compose up -d --build
```

Then open <http://localhost:3000>. See [`web/README.md`](web/README.md) for the
API, the dev server and how the database is seeded.

## Running the Android app

Open the project in Android Studio and run the `app` configuration. The word
list ships as a pre-built asset, so no first-run import is needed.

## The word list

`tools/words.txt` and `tools/words2.txt` are the source of truth. Each line is:

```
word | partOfSpeech | English definition | Tagalog word | Tagalog definition | English example
```

Section headers (`# ---- Name ----`) become browsable categories, mapped to
groups in `tools/build_db.py`.

After editing either file, rebuild the database:

```bash
python3 tools/build_db.py     # writes the Android asset and the web seed
python3 tools/check_words.py  # validates field counts and stray characters
python3 tools/dedup_words2.py # drops entries already defined in words.txt
```

`build_db.py` deduplicates by word, sorts alphabetically, and writes both the
Android asset (`app/src/main/assets/databases/dictionary.db`) and the web seed
(`web/server/db/dictionary.seed.db`) so the two never drift.

## Features

- Search across the English word, definition, Tagalog word and Tagalog definition,
  with prefix matches ranked first
- Browse by category group or by part of speech
- Word of the day, stable for the calendar day
- Favourites and personal learning notes
- Pronunciation in British or American English

## Coverage

| Group | Words |
| --- | --- |
| Words & Meaning | 2,237 |
| Idioms & Expressions | 803 |
| Everyday Life | 561 |
| Grammar & Language | 389 |
| Society & World | 327 |
| Science & Technology | 322 |
| Work & Money | 291 |
| People & Character | 274 |
| Speaking & Listening | 253 |
| Communication Skills | 136 |
| Education & Arts | 101 |
| Reading & Writing | 68 |

> The Tagalog translations are a learner's aid, not a scholarly reference. They
> would benefit from review by a native speaker.

## Licence

MIT — see [LICENSE](LICENSE).
