/** Rewrites an existing note, keeping its original creation time. */
export default defineEventHandler(async (event) => {
  const id = Number(getRouterParam(event, 'id'))
  const body = await readBody<{ word?: string; text?: string }>(event)
  const word = (body.word ?? '').trim()
  const text = (body.text ?? '').trim()
  if (!word || !text) {
    throw createError({ statusCode: 400, statusMessage: 'word and text are required' })
  }

  const now = Date.now()
  const info = getDb()
    .prepare('UPDATE learnings SET word = ?, text = ?, updatedAt = ? WHERE id = ?')
    .run(word, text, now, id)
  if (info.changes === 0) {
    throw createError({ statusCode: 404, statusMessage: 'Learning not found' })
  }
  return { id, word, text, updatedAt: now }
})
