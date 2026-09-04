import type { Word } from '../../utils/db'

/** Favourited words, most recently added first. */
export default defineEventHandler(() =>
  getDb()
    .prepare(
      `SELECT w.* FROM words w
       INNER JOIN favorites f ON w.word = f.word
       ORDER BY f.addedAt DESC`
    )
    .all() as Word[]
)
