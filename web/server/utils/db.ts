import Database from 'better-sqlite3'
import { createHash } from 'node:crypto'
import { copyFileSync, existsSync, mkdirSync, readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'

export interface Word {
  word: string
  partOfSpeech: string
  definition: string
  example: string
  tagalogWord: string
  tagalogDefinition: string
  category: string
  categoryGroup: string
}

export interface Learning {
  id: number
  word: string
  text: string
  createdAt: number
  updatedAt: number
}

let db: Database.Database | null = null

/**
 * Opens the dictionary, copying the read-only seed into the data directory the
 * first time it runs. This mirrors Room's `createFromAsset`: the word list ships
 * with the build, while favourites, learnings and settings accumulate in the
 * copy that lives on the mounted volume.
 */
export function getDb(): Database.Database {
  if (db) return db

  const config = useRuntimeConfig()
  const dbPath = resolve(config.dataDir, 'dictionary.db')

  if (!existsSync(dbPath)) {
    const seed = resolve(config.seedDb)
    if (!existsSync(seed)) {
      throw new Error(`Dictionary seed not found at ${seed}`)
    }
    mkdirSync(dirname(dbPath), { recursive: true })
    copyFileSync(seed, dbPath)
    console.log(`[db] seeded ${dbPath} from ${seed}`)
  }

  db = new Database(dbPath)
  db.pragma('journal_mode = WAL')
  db.pragma('foreign_keys = ON')
  ensureSchema(db)
  refreshWordsIfSeedChanged(db, resolve(config.seedDb))
  console.log(`[db] opened ${dbPath} (${wordCount(db)} words)`)
  return db
}

/**
 * Replaces the word list when a newer seed ships with the image, leaving
 * favourites, learnings and settings untouched. Without this an existing
 * volume would keep serving the word list it was first created with.
 */
function refreshWordsIfSeedChanged(handle: Database.Database, seed: string) {
  if (!existsSync(seed)) return

  const fingerprint = createHash('sha256').update(readFileSync(seed)).digest('hex').slice(0, 16)
  const row = handle.prepare('SELECT value FROM settings WHERE key = ?').get(KEY_SEED_VERSION) as
    | { value: string }
    | undefined
  if (row?.value === fingerprint) return

  handle.exec(`ATTACH DATABASE '${seed.replace(/'/g, "''")}' AS seed`)
  try {
    handle.transaction(() => {
      handle.exec('DELETE FROM words')
      handle.exec('INSERT INTO words SELECT * FROM seed.words')
    })()
  } finally {
    handle.exec('DETACH DATABASE seed')
  }

  handle
    .prepare('INSERT INTO settings (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value')
    .run(KEY_SEED_VERSION, fingerprint)
  console.log(`[db] word list refreshed from seed (${fingerprint})`)
}

function wordCount(handle: Database.Database): number {
  return (handle.prepare('SELECT COUNT(*) AS n FROM words').get() as { n: number }).n
}

/** The user tables are created by Room on the device; make sure they exist here too. */
function ensureSchema(handle: Database.Database) {
  handle.exec(`
    CREATE TABLE IF NOT EXISTS favorites (
      word TEXT NOT NULL PRIMARY KEY,
      addedAt INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS learnings (
      id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
      word TEXT NOT NULL,
      text TEXT NOT NULL,
      createdAt INTEGER NOT NULL,
      updatedAt INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS settings (
      key TEXT NOT NULL PRIMARY KEY,
      value TEXT NOT NULL
    );
  `)
}

export function getSetting(key: string): string | null {
  const row = getDb().prepare('SELECT value FROM settings WHERE key = ?').get(key) as
    | { value: string }
    | undefined
  return row?.value ?? null
}

export function putSetting(key: string, value: string) {
  getDb()
    .prepare('INSERT INTO settings (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value')
    .run(key, value)
}

export const ACCENT_BRITISH = 'en-GB'
export const ACCENT_AMERICAN = 'en-US'
export const KEY_ACCENT = 'speech_accent'
export const KEY_WOD_DATE = 'word_of_day_date'
export const KEY_WOD_WORD = 'word_of_day_word'
export const KEY_SEED_VERSION = 'seed_version'
