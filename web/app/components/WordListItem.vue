<script setup lang="ts">
import type { Word } from '~/types'

defineProps<{ word: Word; showAddLearning?: boolean }>()
</script>

<template>
  <div class="card item">
    <NuxtLink :to="`/word/${encodeURIComponent(word.word)}`" class="item-body">
      <div class="row" style="gap: 6px">
        <span class="title-md primary-text">{{ word.word }}</span>
        <span v-if="word.partOfSpeech" class="label-sm muted">{{ word.partOfSpeech }}</span>
      </div>
      <div v-if="word.category" class="label-sm category">{{ word.category }}</div>
      <div v-if="word.tagalogWord" class="body-md tagalog">{{ word.tagalogWord }}</div>
      <div class="body-md clamp-2">{{ word.definition }}</div>
      <div v-if="word.example" class="body-sm muted clamp-2">&ldquo;{{ word.example }}&rdquo;</div>
      <div v-if="word.tagalogDefinition" class="body-sm muted clamp-2">
        {{ word.tagalogDefinition }}
      </div>
    </NuxtLink>

    <div class="item-actions">
      <SpeakButton :text="word.word" />
      <NuxtLink
        v-if="showAddLearning"
        class="icon-btn accent"
        :to="`/learnings/edit?word=${encodeURIComponent(word.word)}`"
        title="Add to learnings"
        aria-label="Add to learnings"
      >
        <AppIcon name="add" :size="20" />
      </NuxtLink>
    </div>
  </div>
</template>

<style scoped>
.item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px 4px 16px;
  transition: box-shadow 0.15s ease;
}

.item:hover { box-shadow: 0 4px 10px var(--shadow); }

.item-body {
  flex: 1;
  min-width: 0;
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.item-actions { display: flex; align-items: center; flex: none; }
.category { color: var(--tertiary); }
.tagalog { color: var(--secondary); }
</style>
