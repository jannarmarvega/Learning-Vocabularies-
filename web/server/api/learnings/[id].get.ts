import type { Learning } from '../../utils/db'

export default defineEventHandler((event) => {
  const id = Number(getRouterParam(event, 'id'))
  const row = getDb().prepare('SELECT * FROM learnings WHERE id = ?').get(id) as
    | Learning
    | undefined
  if (!row) throw createError({ statusCode: 404, statusMessage: 'Learning not found' })
  return row
})
