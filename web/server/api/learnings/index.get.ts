import type { Learning } from '../../utils/db'

export default defineEventHandler((event) => {
  const q = String(getQuery(event).q ?? '').trim()
  const db = getDb()

  if (!q) return db.prepare('SELECT * FROM learnings ORDER BY updatedAt DESC').all() as Learning[]

  return db
    .prepare(
      `SELECT * FROM learnings
       WHERE word LIKE '%' || @q || '%' OR text LIKE '%' || @q || '%'
       ORDER BY updatedAt DESC`
    )
    .all({ q }) as Learning[]
})
