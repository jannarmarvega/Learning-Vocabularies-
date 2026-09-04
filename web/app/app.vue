<template>
  <div class="shell">
    <AppNav />
    <main>
      <NuxtPage />
    </main>
  </div>
</template>

<style scoped>
.shell { min-height: 100%; display: flex; flex-direction: column; }
main { flex: 1; }
</style>
