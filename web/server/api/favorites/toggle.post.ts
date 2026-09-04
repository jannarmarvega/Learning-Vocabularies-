export default defineEventHandler(async (event) => {
  const { word } = await readBody<{ word?: string }>(event)
  if (!word) throw createError({ statusCode: 400, statusMessage: 'word is required' })

  const db = getDb()
  const exists = db.prepare('SELECT 1 FROM favorites WHERE word = ?').get(word) !== undefined

  if (exists) {
    db.prepare('DELETE FROM favorites WHERE word = ?').run(word)
  } else {
    db.prepare('INSERT OR IGNORE INTO favorites (word, addedAt) VALUES (?, ?)').run(word, Date.now())
  }
  return { word, isFavorite: !exists }
})
