<script setup lang="ts">
import type { Word } from '~/types'

useHead({ title: 'My Dictionary' })

const { data: wordOfTheDay } = await useFetch<Word | null>('/api/word-of-the-day')
const { data: surprise, refresh: shuffle, status } = await useFetch<Word | null>('/api/random-word')
</script>

<template>
  <div class="page stack">
    <section class="stack" style="gap: 12px">
      <h2 class="headline-sm primary-text section-title">Word of the Day</h2>
      <WordCard v-if="wordOfTheDay" :word="wordOfTheDay" />
      <p v-else class="body-md muted">Loading your daily word…</p>
    </section>

    <section class="stack" style="gap: 12px">
      <div class="row-between">
        <h2 class="title-md primary-text section-title">Random pick</h2>
        <button class="btn-text btn" :disabled="status === 'pending'" @click="shuffle()">
          <AppIcon name="refresh" :size="18" />
          Shuffle
        </button>
      </div>
      <WordCard v-if="surprise" :word="surprise" :highlighted="false" />
    </section>
  </div>
</template>

<style scoped>
.section-title { margin: 0; font-weight: 400; }
</style>
