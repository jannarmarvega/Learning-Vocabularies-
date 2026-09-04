/**
 * Speaks dictionary entries aloud through the Web Speech API — the browser
 * counterpart of speech/SpeechManager.kt. The chosen accent is persisted
 * server-side so it survives a reload, exactly as the app stores it in settings.
 */
export const ACCENT_BRITISH = 'en-GB'
export const ACCENT_AMERICAN = 'en-US'

const accent = ref<string>(ACCENT_BRITISH)
const isSpeaking = ref(false)
const isSupported = ref(true)
let accentLoaded = false

function pickVoice(lang: string): SpeechSynthesisVoice | null {
  const voices = window.speechSynthesis.getVoices()
  if (!voices.length) return null
  return (
    voices.find((v) => v.lang.replace('_', '-') === lang) ??
    voices.find((v) => v.lang.replace('_', '-').startsWith(lang.split('-')[0]!)) ??
    null
  )
}

export function useSpeech() {
  onMounted(async () => {
    isSupported.value = typeof window !== 'undefined' && 'speechSynthesis' in window
    if (!isSupported.value || accentLoaded) return
    accentLoaded = true
    try {
      const stored = await $fetch<{ accent: string }>('/api/settings/accent')
      accent.value = stored.accent
    } catch {
      // Falls back to British, the app's default.
    }
  })

  function speak(text: string) {
    const trimmed = text?.trim()
    if (!trimmed || !isSupported.value) return

    const synth = window.speechSynthesis
    synth.cancel() // QUEUE_FLUSH: a new request replaces whatever is playing.

    const utterance = new SpeechSynthesisUtterance(trimmed)
    utterance.lang = accent.value
    const voice = pickVoice(accent.value)
    if (voice) utterance.voice = voice
    utterance.onstart = () => (isSpeaking.value = true)
    utterance.onend = () => (isSpeaking.value = false)
    utterance.onerror = () => (isSpeaking.value = false)
    synth.speak(utterance)
  }

  async function setAccent(tag: string) {
    if (accent.value === tag) return
    accent.value = tag
    try {
      await $fetch('/api/settings/accent', { method: 'PUT', body: { accent: tag } })
    } catch {
      // A failed save only costs the preference on the next reload.
    }
  }

  return { accent, isSpeaking, isSupported, speak, setAccent }
}
