<script setup lang="ts">
import type { CategoryGroup, PartOfSpeechSummary } from '~/types'

useHead({ title: 'Browse · My Dictionary' })

const tab = ref<'categories' | 'pos'>('categories')

const { data: groups } = await useFetch<CategoryGroup[]>('/api/categories', { default: () => [] })
const { data: partsOfSpeech } = await useFetch<PartOfSpeechSummary[]>('/api/parts-of-speech', {
  default: () => []
})
</script>

<template>
  <div class="page">
    <div class="tabs" role="tablist">
      <button
        class="tab"
        :class="{ active: tab === 'categories' }"
        role="tab"
        :aria-selected="tab === 'categories'"
        @click="tab = 'categories'"
      >
        Categories
      </button>
      <button
        class="tab"
        :class="{ active: tab === 'pos' }"
        role="tab"
        :aria-selected="tab === 'pos'"
        @click="tab = 'pos'"
      >
        Parts of speech
      </button>
    </div>

    <div v-if="tab === 'categories'">
      <section v-for="group in groups" :key="group.group">
        <NuxtLink class="group-header" :to="`/words?group=${encodeURIComponent(group.group)}`">
          <span class="title-sm primary-text">{{ group.group }}</span>
          <span class="label-sm muted">{{ group.wordCount }} words</span>
        </NuxtLink>
        <div class="list">
          <NuxtLink
            v-for="summary in group.categories"
            :key="summary.category"
            class="card card-variant card-clickable browse-row"
            :to="`/words?category=${encodeURIComponent(summary.category)}`"
          >
            <span class="body-lg">{{ summary.category }}</span>
            <span class="label-md muted">{{ summary.wordCount }}</span>
          </NuxtLink>
        </div>
      </section>
    </div>

    <div v-else class="list">
      <NuxtLink
        v-for="summary in partsOfSpeech"
        :key="summary.partOfSpeech"
        class="card card-variant card-clickable browse-row"
        :to="`/words?pos=${encodeURIComponent(summary.partOfSpeech)}`"
      >
        <span class="body-lg">{{ summary.partOfSpeech }}</span>
        <span class="label-md muted">{{ summary.wordCount }}</span>
      </NuxtLink>
    </div>
  </div>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--outline-variant);
}

.tab {
  flex: 1;
  padding: 14px 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--on-surface-variant);
  border-bottom: 3px solid transparent;
  transition: color 0.15s ease, border-color 0.15s ease;
}

.tab.active { color: var(--primary); border-bottom-color: var(--primary); }

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 4px 6px;
}

.group-header:hover .title-sm { text-decoration: underline; }

.list { display: flex; flex-direction: column; gap: 8px; }

.browse-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
}
</style>
