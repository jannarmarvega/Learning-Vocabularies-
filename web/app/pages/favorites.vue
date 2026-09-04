<script setup lang="ts">
import type { Word } from '~/types'

useHead({ title: 'Favorites · My Dictionary' })

const { data: favorites } = await useFetch<Word[]>('/api/favorites', { default: () => [] })
</script>

<template>
  <div class="page">
    <PageHeader title="Favorites" />

    <p v-if="!favorites.length" class="empty body-md">
      No favorites yet.
      Tap the heart on a word to save it here.
    </p>

    <div v-else class="list">
      <WordListItem v-for="word in favorites" :key="word.word" :word="word" />
    </div>
  </div>
</template>

<style scoped>
.list { display: flex; flex-direction: column; gap: 8px; }
</style>
