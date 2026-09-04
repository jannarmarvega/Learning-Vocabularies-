<script setup lang="ts">
import type { Word, WordPage } from '~/types'

const PAGE_SIZE = 50
const route = useRoute()

const category = computed(() => (route.query.category as string) || '')
const group = computed(() => (route.query.group as string) || '')
const pos = computed(() => (route.query.pos as string) || '')

const title = computed(() => {
  if (category.value) return category.value
  if (group.value) return group.value
  if (pos.value) return pos.value.charAt(0).toUpperCase() + pos.value.slice(1)
  return 'All words'
})

useHead(() => ({ title: `${title.value} · My Dictionary` }))

const items = ref<Word[]>([])
const total = ref(0)
const hasMore = ref(true)
const isLoading = ref(false)

async function loadPage(offset: number) {
  if (isLoading.value) return
  isLoading.value = true
  try {
    const page = await $fetch<WordPage>('/api/words', {
      query: {
        category: category.value || undefined,
        group: group.value || undefined,
        pos: pos.value || undefined,
        offset,
        limit: PAGE_SIZE
      }
    })
    items.value = offset === 0 ? page.items : [...items.value, ...page.items]
    total.value = page.total
    hasMore.value = page.hasMore
  } finally {
    isLoading.value = false
  }
}

function reset() {
  items.value = []
  hasMore.value = true
  loadPage(0)
}

watch([category, group, pos], reset)

const sentinel = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | undefined

onMounted(() => {
  reset()
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0]?.isIntersecting && hasMore.value && !isLoading.value) {
        loadPage(items.value.length)
      }
    },
    { rootMargin: '400px' }
  )
  if (sentinel.value) observer.observe(sentinel.value)
})

onBeforeUnmount(() => observer?.disconnect())
</script>

<template>
  <div class="page">
    <PageHeader :title="title" back="/browse" />

    <div v-if="!items.length && isLoading" class="center-row"><span class="spinner" /></div>

    <p v-else-if="!items.length" class="empty body-md">No words match this filter.</p>

    <div v-else class="list">
      <WordListItem v-for="word in items" :key="word.word" :word="word" show-add-learning />
    </div>

    <div ref="sentinel" class="center-row">
      <span v-if="isLoading && items.length" class="spinner" />
      <span v-else-if="!hasMore && items.length" class="body-sm muted">{{ total }} words</span>
    </div>
  </div>
</template>

<style scoped>
.list { display: flex; flex-direction: column; gap: 8px; }
</style>
