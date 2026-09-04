<script setup lang="ts">
import type { WordDetail } from '~/types'
import { ACCENT_AMERICAN, ACCENT_BRITISH } from '~/composables/useSpeech'

const route = useRoute()
const key = computed(() => decodeURIComponent(route.params.word as string))

const { data: word, error } = await useFetch<WordDetail>(
  () => `/api/words/${encodeURIComponent(key.value)}`
)

useHead(() => ({ title: word.value ? `${word.value.word} · My Dictionary` : 'My Dictionary' }))

const { speak, accent, setAccent } = useSpeech()

const isFavorite = ref(false)
watchEffect(() => {
  if (word.value) isFavorite.value = word.value.isFavorite
})

async function toggleFavorite() {
  if (!word.value) return
  const result = await $fetch<{ isFavorite: boolean }>('/api/favorites/toggle', {
    method: 'POST',
    body: { word: word.value.word }
  })
  isFavorite.value = result.isFavorite
}
</script>

<template>
  <div class="page">
    <PageHeader :title="word?.word ?? key" back="/search">
      <template #actions>
        <button
          v-if="word"
          class="icon-btn"
          :class="{ accent: isFavorite }"
          :title="isFavorite ? 'Remove from favorites' : 'Add to favorites'"
          :aria-label="isFavorite ? 'Remove from favorites' : 'Add to favorites'"
          :aria-pressed="isFavorite"
          @click="toggleFavorite"
        >
          <AppIcon :name="isFavorite ? 'favorite' : 'favoriteBorder'" :size="22" />
        </button>
      </template>
    </PageHeader>

    <p v-if="error" class="empty body-lg">Word not found</p>

    <div v-else-if="word" class="stack">
      <div class="row-between">
        <h2 class="headline-lg primary-text name">{{ word.word }}</h2>
        <SpeakButton :text="word.word" :size="24" tonal />
      </div>

      <div class="row" style="flex-wrap: wrap">
        <span class="label-md muted">Accent</span>
        <button
          class="chip"
          :class="{ selected: accent === ACCENT_BRITISH }"
          @click="setAccent(ACCENT_BRITISH)"
        >
          British
        </button>
        <button
          class="chip"
          :class="{ selected: accent === ACCENT_AMERICAN }"
          @click="setAccent(ACCENT_AMERICAN)"
        >
          American
        </button>
      </div>

      <div class="row" style="flex-wrap: wrap">
        <span v-if="word.partOfSpeech" class="title-sm" style="color: var(--secondary)">
          {{ word.partOfSpeech }}
        </span>
        <NuxtLink
          v-if="word.category"
          class="chip chip-assist"
          :to="`/words?category=${encodeURIComponent(word.category)}`"
        >
          {{ word.category }}
        </NuxtLink>
      </div>

      <section v-if="word.tagalogWord" class="card card-variant section">
        <div class="label-md primary-text">Salin sa Tagalog</div>
        <p class="title-md">{{ word.tagalogWord }}</p>
      </section>

      <section class="card card-variant section">
        <div class="row-between">
          <span class="label-md primary-text">Definition</span>
          <button
            class="icon-btn accent"
            title="Play Definition aloud"
            aria-label="Play Definition aloud"
            @click="speak(word.definition)"
          >
            <AppIcon name="volumeUp" :size="20" />
          </button>
        </div>
        <p class="body-lg">{{ word.definition }}</p>
      </section>

      <section v-if="word.example" class="card card-variant section">
        <div class="row-between">
          <span class="label-md primary-text">Example</span>
          <button
            class="icon-btn accent"
            title="Play Example aloud"
            aria-label="Play Example aloud"
            @click="speak(word.example)"
          >
            <AppIcon name="volumeUp" :size="20" />
          </button>
        </div>
        <p class="body-md muted">&ldquo;{{ word.example }}&rdquo;</p>
      </section>

      <section v-if="word.tagalogDefinition" class="card card-variant section">
        <div class="label-md primary-text">Kahulugan</div>
        <p class="body-lg">{{ word.tagalogDefinition }}</p>
      </section>

      <NuxtLink class="btn" :to="`/learnings/edit?word=${encodeURIComponent(word.word)}`">
        <AppIcon name="add" :size="20" />
        Add to Learnings
      </NuxtLink>
    </div>
  </div>
</template>

<style scoped>
.name { margin: 0; flex: 1; min-width: 0; font-weight: 400; }
.section { padding: 16px; }
.section p { margin: 8px 0 0; }
</style>
