import type { Word } from '../utils/db'

export default defineEventHandler(() => {
  const row = getDb().prepare('SELECT * FROM words ORDER BY RANDOM() LIMIT 1').get() as
    | Word
    | undefined
  return row ?? null
})
