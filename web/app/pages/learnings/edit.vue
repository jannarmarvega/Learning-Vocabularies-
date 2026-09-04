<script setup lang="ts">
import type { Learning } from '~/types'

const route = useRoute()
const router = useRouter()

const id = computed(() => Number(route.query.id ?? 0))
const isEditing = computed(() => id.value > 0)

useHead(() => ({ title: `${isEditing.value ? 'Edit' : 'Add'} Learning · My Dictionary` }))

const word = ref((route.query.word as string) ?? '')
const text = ref('')
const isLoaded = ref(!isEditing.value)
const isSaving = ref(false)

if (isEditing.value) {
  const { data } = await useFetch<Learning>(`/api/learnings/${id.value}`)
  if (data.value) {
    word.value = data.value.word
    text.value = data.value.text
  }
  isLoaded.value = true
}

const canSave = computed(
  () => isLoaded.value && !isSaving.value && word.value.trim() !== '' && text.value.trim() !== ''
)

async function save() {
  if (!canSave.value) return
  isSaving.value = true
  try {
    const body = { word: word.value, text: text.value }
    if (isEditing.value) {
      await $fetch(`/api/learnings/${id.value}`, { method: 'PUT', body })
    } else {
      await $fetch('/api/learnings', { method: 'POST', body })
    }
    router.push('/learnings')
  } finally {
    isSaving.value = false
  }
}

async function remove() {
  if (!isEditing.value) return
  if (!confirm(`Delete this learning?\n\nThe note for "${word.value}" will be removed.`)) return
  await $fetch(`/api/learnings/${id.value}`, { method: 'DELETE' })
  router.push('/learnings')
}
</script>

<template>
  <div class="page">
    <PageHeader :title="isEditing ? 'Edit Learning' : 'Add Learning'" back="/learnings">
      <template #actions>
        <button
          v-if="isEditing"
          class="icon-btn"
          title="Delete learning"
          aria-label="Delete learning"
          @click="remove"
        >
          <AppIcon name="delete" :size="22" />
        </button>
      </template>
    </PageHeader>

    <form class="stack" @submit.prevent="save">
      <div>
        <span class="label-md field-label">Word</span>
        <label class="field">
          <input v-model="word" type="text" :disabled="!isLoaded" aria-label="Word">
        </label>
      </div>

      <div>
        <span class="label-md field-label">Meaning / notes</span>
        <label class="field">
          <textarea v-model="text" :disabled="!isLoaded" aria-label="Meaning or notes" />
        </label>
      </div>

      <div class="row">
        <button type="submit" class="btn spread" :disabled="!canSave">
          {{ isEditing ? 'Save changes' : 'Save' }}
        </button>
        <NuxtLink class="btn btn-text" to="/learnings">Cancel</NuxtLink>
      </div>
    </form>
  </div>
</template>
