import type { Word } from '../../utils/db'

export default defineEventHandler((event) => {
  const word = decodeURIComponent(getRouterParam(event, 'word') ?? '')
  const row = getDb().prepare('SELECT * FROM words WHERE word = ?').get(word) as Word | undefined
  if (!row) throw createError({ statusCode: 404, statusMessage: 'Word not found' })

  const isFavorite =
    (getDb().prepare('SELECT 1 FROM favorites WHERE word = ?').get(word) as unknown) !== undefined

  return { ...row, isFavorite }
})
