#!/usr/bin/env python3
"""Builds the pre-packaged Room database from tools/words.txt.

Line format (fields separated by "|"):
    word | partOfSpeech | English definition | Tagalog word | Tagalog definition | English example

The category comes from the nearest "# ---- Section Name ----" header above the
line, so entries only need to be filed under the right heading. A 7th field may
be supplied to override the section category for a single entry.
"""
import glob
import os
import re
import sqlite3
import sys

BASE = os.path.dirname(os.path.abspath(__file__))
# words.txt holds the original curated set; words2.txt the later expansion.
# Both are read in order, and the first definition of a word wins.
SRC_FILES = sorted(glob.glob(os.path.join(BASE, "words*.txt")))
OUT_DIR = os.path.join(BASE, "..", "app", "src", "main", "assets", "databases")
OUT = os.path.join(OUT_DIR, "dictionary.db")
# The Nuxt app ships the same word list as its read-only seed; keep the two in
# step so a rebuild never leaves the web copy behind.
WEB_SEED = os.path.join(BASE, "..", "web", "server", "db", "dictionary.seed.db")
SCHEMA = os.path.join(
    BASE, "..", "app", "schemas", "com.mydictionary.data.db.AppDatabase", "2.json"
)

# Fallback used when the generated Room schema is not available yet.
IDENTITY_HASH = "0d95f4e4ae4b2f9b8c7d1a5e6f3b2c11"

SECTION_RE = re.compile(r"^#\s*-+\s*(.+?)\s*-+\s*$")

CREATE_SQL = [
    "CREATE TABLE IF NOT EXISTS `words` (`word` TEXT NOT NULL, `partOfSpeech` TEXT NOT NULL, "
    "`definition` TEXT NOT NULL, `example` TEXT NOT NULL, `tagalogWord` TEXT NOT NULL, "
    "`tagalogDefinition` TEXT NOT NULL, `category` TEXT NOT NULL, `categoryGroup` TEXT NOT NULL, "
    "PRIMARY KEY(`word`))",
    "CREATE INDEX IF NOT EXISTS `index_words_category` ON `words` (`category`)",
    "CREATE INDEX IF NOT EXISTS `index_words_partOfSpeech` ON `words` (`partOfSpeech`)",
    "CREATE TABLE IF NOT EXISTS `favorites` (`word` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`word`))",
    "CREATE TABLE IF NOT EXISTS `learnings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
    "`word` TEXT NOT NULL, `text` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)",
    "CREATE TABLE IF NOT EXISTS `settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))",
]

# Every section heading in words.txt maps to a browsable group.
CATEGORY_GROUPS = {
    # Grammar & Language
    "Parts of Speech": "Grammar & Language",
    "Sentence Structure": "Grammar & Language",
    "Word Parts & Spelling": "Grammar & Language",
    "Punctuation": "Grammar & Language",
    "Grammar Rules": "Grammar & Language",
    # Words & Meaning
    "Meaning & Vocabulary": "Words & Meaning",
    "Vocabulary & Diction": "Words & Meaning",
    "Word Relationships": "Words & Meaning",
    "Shades of Meaning": "Words & Meaning",
    "Advanced Vocabulary": "Words & Meaning",
    "Academic Vocabulary": "Words & Meaning",
    "Commonly Confused Words": "Words & Meaning",
    "Useful Verbs": "Words & Meaning",
    "Useful Adjectives": "Words & Meaning",
    "Linking Words": "Words & Meaning",
    # Speaking & Listening
    "Pronunciation & Speech": "Speaking & Listening",
    "Speaking & Conversation": "Speaking & Listening",
    "Listening & Understanding": "Speaking & Listening",
    # Reading & Writing
    "Writing": "Reading & Writing",
    # Idioms & Expressions
    "Common Idioms": "Idioms & Expressions",
    "British Idioms": "Idioms & Expressions",
    "British Slang": "Idioms & Expressions",
    "American Idioms": "Idioms & Expressions",
    "Phrasal Verbs": "Idioms & Expressions",
    "Proverbs & Sayings": "Idioms & Expressions",
    # Communication Skills
    "Communication Qualities": "Communication Skills",
    "Rhetoric & Argumentation": "Communication Skills",
    # People & Character
    "Character & Personality": "People & Character",
    "Emotions & Feelings": "People & Character",
    # Work & Money
    "Work & Career": "Work & Money",
    "Money & Finance": "Work & Money",
    # Society & World
    "Law & Government": "Society & World",
    "Society & Culture": "Society & World",
    "Travel & Places": "Society & World",
    # Science & Technology
    "Science & Nature": "Science & Technology",
    "Health & Medicine": "Science & Technology",
    "Technology & Digital": "Science & Technology",
    "Environment & Climate": "Science & Technology",
    # Grammar & Language (expansion)
    "Verb Tenses": "Grammar & Language",
    "Prefixes & Suffixes": "Grammar & Language",
    "Word Roots": "Grammar & Language",
    "Articles & Determiners": "Grammar & Language",
    "Prepositions": "Grammar & Language",
    "Irregular Verbs": "Grammar & Language",
    # Words & Meaning (expansion)
    "Useful Nouns": "Words & Meaning",
    "Useful Adverbs": "Words & Meaning",
    "Collocations": "Words & Meaning",
    "Formal & Informal": "Words & Meaning",
    # Everyday Life
    "Food & Drink": "Everyday Life",
    "Cooking & Kitchen": "Everyday Life",
    "Family & Relationships": "Everyday Life",
    "Home & Household": "Everyday Life",
    "Clothing & Fashion": "Everyday Life",
    "Body & Appearance": "Everyday Life",
    "Daily Routine": "Everyday Life",
    "Shopping & Services": "Everyday Life",
    "Time & Calendar": "Everyday Life",
    "Numbers & Measurement": "Everyday Life",
    "Colours & Shapes": "Everyday Life",
    "Weather & Seasons": "Everyday Life",
    "Animals & Plants": "Everyday Life",
    "Transport & Vehicles": "Everyday Life",
    "Sports & Leisure": "Everyday Life",
    # Education & Arts
    "School & Education": "Education & Arts",
    "Study Skills": "Education & Arts",
    "Arts & Literature": "Education & Arts",
    "Music & Performance": "Education & Arts",
    # Work & Money (expansion)
    "Business & Marketing": "Work & Money",
    # Society & World (expansion)
    "History & Geography": "Society & World",
    "Religion & Belief": "Society & World",
    # People & Character (expansion)
    "Behaviour & Actions": "People & Character",
    # Speaking & Listening (expansion)
    "Everyday Phrases": "Speaking & Listening",
    "Questions & Responses": "Speaking & Listening",
}

DEFAULT_GROUP = "Other"


def read_schema():
    """Returns (identity_hash, create_statements) from the Room-generated schema.

    Falling back to the constants above lets the script run before a Gradle build
    has produced schemas/2.json, but the generated schema is authoritative: it is
    what the app checks against at startup.
    """
    if not os.path.exists(SCHEMA):
        print("NOTE: schemas/2.json not found, using the built-in fallback schema.")
        return IDENTITY_HASH, list(CREATE_SQL)

    import json

    with open(SCHEMA, encoding="utf-8") as f:
        data = json.load(f)["database"]

    statements = []
    for entity in data["entities"]:
        table = entity["tableName"]
        statements.append(entity["createSql"].replace("${TABLE_NAME}", table))
        for index in entity.get("indices", []):
            statements.append(index["createSql"].replace("${TABLE_NAME}", table))
    return data["identityHash"], statements


def load_words():
    words = []
    seen = {}
    unmapped = set()
    for src in SRC_FILES:
        category = "Other"
        with open(src, encoding="utf-8") as f:
            lines = list(enumerate(f, start=1))
        name = os.path.basename(src)
        for lineno, line in lines:
            line = line.rstrip("\n").rstrip("\r")
            if not line.strip():
                continue
            if line.lstrip().startswith("#"):
                match = SECTION_RE.match(line.strip())
                if match:
                    category = match.group(1)
                    if category not in CATEGORY_GROUPS:
                        unmapped.add(category)
                continue
            parts = line.split("|")
            if len(parts) < 5:
                print(f"SKIP (bad line {name}:{lineno}):", line)
                continue
            word = parts[0].strip()
            pos = parts[1].strip()
            definition = parts[2].strip()
            tagalog_word = parts[3].strip()
            tagalog_definition = parts[4].strip()
            example = parts[5].strip() if len(parts) > 5 else ""
            row_category = parts[6].strip() if len(parts) > 6 and parts[6].strip() else category
            group = CATEGORY_GROUPS.get(row_category, DEFAULT_GROUP)
            key = word.lower()
            if key in seen:
                print(f"SKIP (duplicate {name}:{lineno}): {word} (already in {seen[key]})")
                continue
            seen[key] = row_category
            words.append(
                (
                    word,
                    pos,
                    definition,
                    example,
                    tagalog_word,
                    tagalog_definition,
                    row_category,
                    group,
                )
            )
    if unmapped:
        print("WARNING: sections with no group mapping ->", ", ".join(sorted(unmapped)))
    words.sort(key=lambda w: w[0].lower())
    return words


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    if os.path.exists(OUT):
        os.remove(OUT)

    identity_hash, create_sql = read_schema()

    conn = sqlite3.connect(OUT)
    c = conn.cursor()

    for sql in create_sql:
        c.execute(sql)

    c.execute("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
    c.execute("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, ?)", (identity_hash,))

    words = load_words()
    c.executemany(
        "INSERT OR IGNORE INTO words (word, partOfSpeech, definition, example, tagalogWord, "
        "tagalogDefinition, category, categoryGroup) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
        words,
    )

    c.execute("PRAGMA user_version = 2")
    conn.commit()

    if os.path.isdir(os.path.dirname(WEB_SEED)):
        import shutil

        shutil.copyfile(OUT, WEB_SEED)
        print(f"Copied the word list to {WEB_SEED}")

    print(f"Wrote {len(words)} words to {OUT}")
    print(f"identity_hash = {identity_hash}")
    for group, cat, n in c.execute(
        "SELECT categoryGroup, category, COUNT(*) FROM words GROUP BY categoryGroup, category "
        "ORDER BY categoryGroup, category"
    ):
        print(f"  {group:<24} {cat:<28} {n}")
    for pos, n in c.execute(
        "SELECT partOfSpeech, COUNT(*) FROM words GROUP BY partOfSpeech ORDER BY COUNT(*) DESC"
    ):
        print(f"  POS {pos:<20} {n}")
    conn.close()
    return 0


if __name__ == "__main__":
    sys.exit(main())
