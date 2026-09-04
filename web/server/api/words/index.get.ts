import type { Word } from '../../utils/db'

const PAGE_SIZE = 50

/**
 * One paged, filtered slice of the dictionary — the web port of WordDao.searchPage.
 * A blank query lists everything; an omitted filter leaves that dimension open.
 */
export default defineEventHandler((event) => {
  const q = getQuery(event)
  const query = String(q.q ?? '').trim()
  const partOfSpeech = q.pos ? String(q.pos) : null
  const category = q.category ? String(q.category) : null
  const categoryGroup = q.group ? String(q.group) : null
  const offset = Math.max(0, Number(q.offset ?? 0) || 0)
  const limit = Math.min(200, Math.max(1, Number(q.limit ?? PAGE_SIZE) || PAGE_SIZE))

  const where = `
    WHERE (@query = '' OR word LIKE '%' || @query || '%' OR definition LIKE '%' || @query || '%'
       OR tagalogWord LIKE '%' || @query || '%' OR tagalogDefinition LIKE '%' || @query || '%')
      AND (@partOfSpeech IS NULL OR partOfSpeech = @partOfSpeech)
      AND (@category IS NULL OR category = @category)
      AND (@categoryGroup IS NULL OR categoryGroup = @categoryGroup)
  `
  const params = { query, partOfSpeech, category, categoryGroup }
  const db = getDb()

  const total = (
    db.prepare(`SELECT COUNT(*) AS n FROM words ${where}`).get(params) as { n: number }
  ).n

  // Exact prefix matches sort first, then alphabetically — same ranking as the app.
  const items = db
    .prepare(
      `SELECT * FROM words ${where}
       ORDER BY CASE WHEN word LIKE @query || '%' THEN 0 ELSE 1 END, word
       LIMIT @limit OFFSET @offset`
    )
    .all({ ...params, limit, offset }) as Word[]

  return { items, total, offset, limit, hasMore: offset + items.length < total }
})
