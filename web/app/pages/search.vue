<script setup lang="ts">
import type { PartOfSpeechSummary, Word, WordPage } from '~/types'

useHead({ title: 'Search · My Dictionary' })

const PAGE_SIZE = 50

const query = ref('')
const partOfSpeech = ref<string | null>(null)
const items = ref<Word[]>([])
const total = ref(0)
const hasMore = ref(true)
const isLoading = ref(false)

const { data: partsOfSpeech } = await useFetch<PartOfSpeechSummary[]>('/api/parts-of-speech', {
  default: () => []
})

async function loadPage(offset: number) {
  if (isLoading.value) return
  isLoading.value = true
  const snapshot = { q: query.value, pos: partOfSpeech.value }
  try {
    const page = await $fetch<WordPage>('/api/words', {
      query: { q: snapshot.q, pos: snapshot.pos ?? undefined, offset, limit: PAGE_SIZE }
    })
    // A newer keystroke may have landed while this page was in flight.
    if (snapshot.q !== query.value || snapshot.pos !== partOfSpeech.value) return
    items.value = offset === 0 ? page.items : [...items.value, ...page.items]
    total.value = page.total
    hasMore.value = page.hasMore
  } finally {
    isLoading.value = false
  }
}

function loadMore() {
  if (isLoading.value || !hasMore.value) return
  loadPage(items.value.length)
}

// 250ms debounce, matching SearchViewModel.
let timer: ReturnType<typeof setTimeout> | undefined
watch([query, partOfSpeech], () => {
  clearTimeout(timer)
  timer = setTimeout(() => {
    items.value = []
    hasMore.value = true
    loadPage(0)
  }, 250)
})

function togglePos(value: string) {
  partOfSpeech.value = partOfSpeech.value === value ? null : value
}

const sentinel = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | undefined

onMounted(() => {
  loadPage(0)
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0]?.isIntersecting) loadMore()
    },
    { rootMargin: '400px' }
  )
  if (sentinel.value) observer.observe(sentinel.value)
})

onBeforeUnmount(() => {
  clearTimeout(timer)
  observer?.disconnect()
})
</script>

<template>
  <div class="page">
    <label class="field">
      <AppIcon name="search" :size="20" />
      <input
        v-model="query"
        type="search"
        placeholder="Search word, meaning or Tagalog…"
        aria-label="Search the dictionary"
      >
      <button
        v-if="query"
        class="icon-btn"
        style="width: 32px; height: 32px"
        title="Clear search"
        aria-label="Clear search"
        @click="query = ''"
      >
        <AppIcon name="close" :size="18" />
      </button>
    </label>

    <div class="filters">
      <button class="chip" :class="{ selected: partOfSpeech === null }" @click="partOfSpeech = null">
        All
      </button>
      <button
        v-for="summary in partsOfSpeech"
        :key="summary.partOfSpeech"
        class="chip"
        :class="{ selected: partOfSpeech === summary.partOfSpeech }"
        @click="togglePos(summary.partOfSpeech)"
      >
        {{ summary.partOfSpeech }} ({{ summary.wordCount }})
      </button>
    </div>

    <p v-if="items.length" class="label-md muted count">{{ total }} words</p>

    <div v-if="!items.length && isLoading" class="center-row"><span class="spinner" /></div>

    <p v-else-if="!items.length" class="empty body-md">
      {{ query ? `No words found for "${query}"` : 'No words match this filter.' }}
    </p>

    <div v-else class="list">
      <WordListItem v-for="word in items" :key="word.word" :word="word" show-add-learning />
    </div>

    <div ref="sentinel" class="center-row">
      <span v-if="isLoading && items.length" class="spinner" />
      <span v-else-if="!hasMore && items.length" class="body-sm muted">End of list</span>
    </div>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 8px;
  margin: 12px 0;
  padding-bottom: 4px;
  overflow-x: auto;
  scrollbar-width: thin;
}

.count { margin: 0 0 8px; }
.list { display: flex; flex-direction: column; gap: 8px; }
</style>
