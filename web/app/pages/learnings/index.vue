<script setup lang="ts">
import type { Learning } from '~/types'

useHead({ title: 'Learnings · My Dictionary' })

const query = ref('')
const debounced = ref('')

// 200ms debounce, matching LearningsViewModel.
let timer: ReturnType<typeof setTimeout> | undefined
watch(query, (value) => {
  clearTimeout(timer)
  timer = setTimeout(() => (debounced.value = value), 200)
})
onBeforeUnmount(() => clearTimeout(timer))

const { data: learnings, refresh } = await useFetch<Learning[]>('/api/learnings', {
  query: { q: debounced },
  default: () => []
})

const { speak } = useSpeech()

async function remove(learning: Learning) {
  if (!confirm(`Delete this learning?\n\nThe note for "${learning.word}" will be removed.`)) return
  await $fetch(`/api/learnings/${learning.id}`, { method: 'DELETE' })
  refresh()
}

const formatter = new Intl.DateTimeFormat(undefined, {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit'
})

function timestampLabel(learning: Learning): string {
  const stamp = formatter.format(new Date(learning.updatedAt))
  return learning.updatedAt > learning.createdAt ? `Edited ${stamp}` : `Added ${stamp}`
}
</script>

<template>
  <div class="page">
    <PageHeader title="Learnings" />

    <label class="field">
      <AppIcon name="search" :size="20" />
      <input v-model="query" type="search" placeholder="Search your learnings…" aria-label="Search learnings">
    </label>

    <p v-if="!learnings.length" class="empty body-md">
      {{ query ? `No learnings match "${query}".` : 'No learnings yet.\nTap + to add a word you\'re learning.' }}
    </p>

    <div v-else class="list">
      <div v-for="learning in learnings" :key="learning.id" class="card item">
        <NuxtLink class="item-body" :to="`/learnings/edit?id=${learning.id}`">
          <div class="title-md primary-text">{{ learning.word }}</div>
          <div class="body-md text">{{ learning.text }}</div>
          <div class="label-sm muted stamp">{{ timestampLabel(learning) }}</div>
        </NuxtLink>

        <div class="item-actions">
          <SpeakButton :text="learning.word" />
          <NuxtLink
            class="icon-btn accent"
            :to="`/learnings/edit?id=${learning.id}`"
            title="Edit learning"
            aria-label="Edit learning"
          >
            <AppIcon name="edit" :size="20" />
          </NuxtLink>
          <button class="icon-btn" title="Delete learning" aria-label="Delete learning" @click="remove(learning)">
            <AppIcon name="delete" :size="20" />
          </button>
        </div>
      </div>
    </div>

    <NuxtLink class="fab" to="/learnings/edit" title="Add learning" aria-label="Add learning">
      <AppIcon name="add" :size="24" />
    </NuxtLink>
  </div>
</template>

<style scoped>
.list { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }

.item {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  padding: 4px 8px 4px 16px;
}

.item-body { flex: 1; min-width: 0; padding: 12px 0; }
.item-actions { display: flex; align-items: center; flex: none; padding-top: 8px; }
.text { white-space: pre-wrap; }
.stamp { margin-top: 4px; }

.fab {
  position: fixed;
  right: 20px;
  bottom: calc(var(--nav-height) + 20px + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: var(--primary-container);
  color: var(--on-primary-container);
  box-shadow: 0 4px 12px var(--shadow);
  transition: filter 0.15s ease;
}

.fab:hover { filter: brightness(1.05); }

@media (min-width: 900px) {
  .fab { bottom: 32px; right: 32px; }
}
</style>
