export default defineEventHandler(() => ({
  accent: getSetting(KEY_ACCENT) ?? ACCENT_BRITISH
}))
