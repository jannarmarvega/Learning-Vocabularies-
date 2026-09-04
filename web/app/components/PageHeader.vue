<script setup lang="ts">
defineProps<{ title: string; back?: string }>()
</script>

<template>
  <div class="page-header">
    <NuxtLink v-if="back" :to="back" class="icon-btn" title="Back" aria-label="Back">
      <AppIcon name="arrowBack" :size="22" />
    </NuxtLink>
    <h1 class="headline-sm">{{ title }}</h1>
    <slot name="actions" />
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
}

.page-header h1 {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-weight: 400;
}
</style>
