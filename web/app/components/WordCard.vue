<script setup lang="ts">
import type { Word } from '~/types'

withDefaults(defineProps<{ word: Word; highlighted?: boolean }>(), { highlighted: true })
</script>

<template>
  <div class="card word-card" :class="highlighted ? 'card-primary' : 'card-variant'">
    <div class="row-between">
      <NuxtLink :to="`/word/${encodeURIComponent(word.word)}`" class="headline-md name">
        {{ word.word }}
      </NuxtLink>
      <SpeakButton :text="word.word" :size="24" tonal />
    </div>

    <div v-if="word.partOfSpeech" class="label-md">{{ word.partOfSpeech }}</div>
    <div v-if="word.tagalogWord" class="title-md">{{ word.tagalogWord }}</div>

    <p class="body-lg">{{ word.definition }}</p>
    <p v-if="word.example" class="body-md">&ldquo;{{ word.example }}&rdquo;</p>
    <p v-if="word.tagalogDefinition" class="body-md">{{ word.tagalogDefinition }}</p>

    <NuxtLink
      v-if="word.category"
      class="chip chip-assist"
      :to="`/words?category=${encodeURIComponent(word.category)}`"
    >
      {{ word.category }}
    </NuxtLink>
  </div>
</template>

<style scoped>
.word-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 20px;
}

.name { flex: 1; min-width: 0; }
.name:hover { text-decoration: underline; }
.word-card p { margin: 8px 0 0; }
.word-card .chip { margin-top: 12px; }
</style>
