export default defineNuxtConfig({
  compatibilityDate: '2025-01-01',
  devtools: { enabled: false },
  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    // Directory holding the live dictionary.db. The seed shipped in the image is
    // copied here on first boot, so a mounted volume keeps favourites and
    // learnings across container restarts. Override with NUXT_DATA_DIR.
    dataDir: '.data',
    // Read-only word list shipped with the build; copied into dataDir on first boot.
    seedDb: 'server/db/dictionary.seed.db'
  },

  app: {
    head: {
      title: 'My Dictionary',
      htmlAttrs: { lang: 'en' },
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1, viewport-fit=cover' },
        { name: 'description', content: 'English-Tagalog dictionary with categories, favourites and personal learnings.' }
      ]
    }
  },

  nitro: {
    // better-sqlite3 loads a .node binary, so it must stay outside the bundle.
    externals: { external: ['better-sqlite3'] }
  }
}) 
