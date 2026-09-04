export default defineEventHandler(async (event) => {
  const { accent } = await readBody<{ accent?: string }>(event)
  if (accent !== ACCENT_BRITISH && accent !== ACCENT_AMERICAN) {
    throw createError({ statusCode: 400, statusMessage: 'accent must be en-GB or en-US' })
  }
  putSetting(KEY_ACCENT, accent)
  return { accent }
})
