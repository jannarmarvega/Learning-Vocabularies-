<script setup lang="ts">
const { toggle } = useTheme()

const items = [
  { to: '/', label: 'Home', icon: 'home' },
  { to: '/search', label: 'Search', icon: 'search' },
  { to: '/browse', label: 'Browse', icon: 'category' },
  { to: '/learnings', label: 'Learnings', icon: 'school' },
  { to: '/favorites', label: 'Favorites', icon: 'favorite' }
]
</script>

<template>
  <header class="top-bar">
    <div class="top-inner">
      <NuxtLink to="/" class="brand">
        <AppIcon name="school" :size="22" />
        <span>My Dictionary</span>
      </NuxtLink>

      <nav class="desktop-nav" aria-label="Main">
        <NuxtLink
          v-for="item in items"
          :key="item.to"
          :to="item.to"
          class="desktop-link"
          active-class="active"
        >
          {{ item.label }}
        </NuxtLink>
      </nav>

      <button class="icon-btn" title="Toggle dark mode" aria-label="Toggle dark mode" @click="toggle">
        <AppIcon name="darkMode" :size="20" />
      </button>
    </div>
  </header>

  <nav class="bottom-nav" aria-label="Main">
    <NuxtLink
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="bottom-link"
      active-class="active"
    >
      <span class="pill"><AppIcon :name="item.icon" :size="22" /></span>
      <span class="label-sm">{{ item.label }}</span>
    </NuxtLink>
  </nav>
</template>

<style scoped>
.top-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--scrim);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--outline-variant);
}

.top-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  max-width: var(--content-max);
  margin: 0 auto;
  padding: 10px 16px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: var(--primary);
  margin-right: auto;
}

.desktop-nav { display: none; gap: 4px; }

.desktop-link {
  padding: 8px 14px;
  border-radius: 20px;
  font-size: 14px;
  color: var(--on-surface-variant);
  transition: background 0.15s ease, color 0.15s ease;
}

.desktop-link:hover { background: var(--surface-variant); }

.desktop-link.active {
  background: var(--secondary-container);
  color: var(--on-secondary-container);
  font-weight: 500;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10;
  display: flex;
  height: var(--nav-height);
  padding-bottom: env(safe-area-inset-bottom);
  background: var(--scrim);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--outline-variant);
}

.bottom-link {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: var(--on-surface-variant);
  transition: color 0.15s ease;
}

.pill {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 30px;
  border-radius: 16px;
  transition: background 0.15s ease;
}

.bottom-link.active { color: var(--on-secondary-container); }
.bottom-link.active .pill { background: var(--secondary-container); }

@media (min-width: 900px) {
  .desktop-nav { display: flex; }
  .bottom-nav { display: none; }
}
</style>
