import type { Word } from '../utils/db'

/** Picks one word per calendar day and remembers it, matching the app's behaviour. */
export default defineEventHandler(() => {
  const db = getDb()
  const today = new Date().toLocaleDateString('en-CA') // yyyy-mm-dd, local time

  if (getSetting(KEY_WOD_DATE) === today) {
    const stored = getSetting(KEY_WOD_WORD)
    if (stored) {
      const row = db.prepare('SELECT * FROM words WHERE word = ?').get(stored) as Word | undefined
      if (row) return row
    }
  }

  const random = db.prepare('SELECT * FROM words ORDER BY RANDOM() LIMIT 1').get() as
    | Word
    | undefined
  if (!random) return null

  putSetting(KEY_WOD_DATE, today)
  putSetting(KEY_WOD_WORD, random.word)
  return random
})
