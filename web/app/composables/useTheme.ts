type Theme = 'system' | 'light' | 'dark'

const theme = ref<Theme>('system')

/** Remembers a light/dark override per browser; "system" defers to the OS. */
export function useTheme() {
  onMounted(() => {
    const stored = localStorage.getItem('theme') as Theme | null
    if (stored === 'light' || stored === 'dark') {
      theme.value = stored
      apply(stored)
    }
  })

  function apply(next: Theme) {
    const root = document.documentElement
    if (next === 'system') root.removeAttribute('data-theme')
    else root.setAttribute('data-theme', next)
  }

  function isDark(): boolean {
    if (theme.value !== 'system') return theme.value === 'dark'
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  }

  function toggle() {
    const next: Theme = isDark() ? 'light' : 'dark'
    theme.value = next
    localStorage.setItem('theme', next)
    apply(next)
  }

  return { theme, toggle }
}
