<script setup lang="ts">
const props = withDefaults(defineProps<{ text: string; size?: number; tonal?: boolean }>(), {
  size: 20,
  tonal: false
})

const { speak } = useSpeech()

function onClick(event: MouseEvent) {
  // Speaker buttons sit inside clickable cards; don't navigate as well.
  event.stopPropagation()
  event.preventDefault()
  speak(props.text)
}
</script>

<template>
  <button
    class="icon-btn"
    :class="tonal ? 'tonal' : 'accent'"
    :title="`Play pronunciation of ${text}`"
    :aria-label="`Play pronunciation of ${text}`"
    @click="onClick"
  >
    <AppIcon name="volumeUp" :size="size" />
  </button>
</template>
