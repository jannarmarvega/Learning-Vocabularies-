export default defineEventHandler(async (event) => {
  const body = await readBody<{ word?: string; text?: string }>(event)
  const word = (body.word ?? '').trim()
  const text = (body.text ?? '').trim()
  if (!word || !text) {
    throw createError({ statusCode: 400, statusMessage: 'word and text are required' })
  }

  const now = Date.now()
  const info = getDb()
    .prepare('INSERT INTO learnings (word, text, createdAt, updatedAt) VALUES (?, ?, ?, ?)')
    .run(word, text, now, now)

  return { id: Number(info.lastInsertRowid), word, text, createdAt: now, updatedAt: now }
})
