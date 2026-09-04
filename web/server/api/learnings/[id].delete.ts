export default defineEventHandler((event) => {
  const id = Number(getRouterParam(event, 'id'))
  getDb().prepare('DELETE FROM learnings WHERE id = ?').run(id)
  return { id, deleted: true }
})
