<script setup lang="ts">
import type { CategoryGroup, QuizMode, QuizQuestion, QuizRound } from '~/types'

useHead({ title: 'Quiz · My Dictionary' })

const route = useRoute()

const modes: { value: QuizMode; label: string; hint: string }[] = [
  { value: 'meaning', label: 'Guess the meaning', hint: 'See a word, pick its definition' },
  { value: 'word', label: 'Guess the word', hint: 'See a definition, pick the word' },
  { value: 'tagalog', label: 'Tagalog', hint: 'See a word, pick its Tagalog translation' }
]
const counts = [5, 10, 20]

const { data: groups } = await useFetch<CategoryGroup[]>('/api/categories', { default: () => [] })

// The scope is encoded as "group:<name>" or "category:<name>" so a single
// <select> can offer both levels; blank means the whole dictionary.
const mode = ref<QuizMode>('meaning')
const count = ref(10)
const scope = ref(
  route.query.category
    ? `category:${route.query.category}`
    : route.query.group
      ? `group:${route.query.group}`
      : ''
)

const scopeLabel = computed(() => scope.value.replace(/^(group|category):/, '') || 'All words')

const phase = ref<'setup' | 'playing' | 'done'>('setup')
const isLoading = ref(false)
const questions = ref<QuizQuestion[]>([])
const index = ref(0)
const picked = ref<number | null>(null)
const score = ref(0)
const missed = ref<QuizQuestion[]>([])

const current = computed(() => questions.value[index.value])
const isLast = computed(() => index.value === questions.value.length - 1)

async function start() {
  isLoading.value = true
  try {
    const split = scope.value.indexOf(':')
    const kind = scope.value.slice(0, split)
    const name = scope.value.slice(split + 1)
    const round = await $fetch<QuizRound>('/api/quiz', {
      query: {
        mode: mode.value,
        count: count.value,
        category: kind === 'category' ? name : undefined,
        group: kind === 'group' ? name : undefined
      }
    })
    questions.value = round.questions
    index.value = 0
    picked.value = null
    score.value = 0
    missed.value = []
    phase.value = round.questions.length ? 'playing' : 'setup'
  } finally {
    isLoading.value = false
  }
}

function choose(option: number) {
  if (picked.value !== null || !current.value) return
  picked.value = option
  if (option === current.value.answerIndex) score.value++
  else missed.value.push(current.value)
}

function next() {
  if (isLast.value) {
    phase.value = 'done'
    return
  }
  index.value++
  picked.value = null
}

function optionState(option: number) {
  if (picked.value === null || !current.value) return ''
  if (option === current.value.answerIndex) return 'correct'
  if (option === picked.value) return 'wrong'
  return 'dimmed'
}

const verdict = computed(() => {
  const ratio = score.value / Math.max(1, questions.value.length)
  if (ratio === 1) return 'Perfect score!'
  if (ratio >= 0.8) return 'Great job!'
  if (ratio >= 0.5) return 'Nice effort — keep going.'
  return 'Keep practising, you will get there.'
})

// Number keys 1–4 answer, Enter moves on — handy on a laptop.
function onKey(event: KeyboardEvent) {
  if (phase.value !== 'playing' || event.target instanceof HTMLSelectElement) return
  const n = Number(event.key)
  if (n >= 1 && n <= (current.value?.options.length ?? 0)) choose(n - 1)
  else if (event.key === 'Enter' && picked.value !== null) next()
}

onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>

<template>
  <div class="page">
    <!-- Setup -->
    <div v-if="phase === 'setup'" class="stack">
      <PageHeader title="Quiz" />

      <section class="stack" style="gap: 8px">
        <span class="title-sm primary-text">Question type</span>
        <div class="mode-list">
          <button
            v-for="m in modes"
            :key="m.value"
            class="card card-clickable mode"
            :class="{ selected: mode === m.value }"
            :aria-pressed="mode === m.value"
            @click="mode = m.value"
          >
            <span class="title-md">{{ m.label }}</span>
            <span class="body-sm muted">{{ m.hint }}</span>
          </button>
        </div>
      </section>

      <section class="stack" style="gap: 8px">
        <label class="title-sm primary-text" for="quiz-scope">Category</label>
        <div class="field">
          <AppIcon name="category" :size="20" />
          <select id="quiz-scope" v-model="scope" class="select">
            <option value="">All words</option>
            <optgroup v-for="g in groups" :key="g.group" :label="g.group">
              <option :value="`group:${g.group}`">All of {{ g.group }} ({{ g.wordCount }})</option>
              <option
                v-for="c in g.categories"
                :key="c.category"
                :value="`category:${c.category}`"
              >
                {{ c.category }} ({{ c.wordCount }})
              </option>
            </optgroup>
          </select>
        </div>
      </section>

      <section class="stack" style="gap: 8px">
        <span class="title-sm primary-text">Questions</span>
        <div class="row">
          <button
            v-for="n in counts"
            :key="n"
            class="chip"
            :class="{ selected: count === n }"
            @click="count = n"
          >
            {{ n }}
          </button>
        </div>
      </section>

      <div>
        <button class="btn" :disabled="isLoading" @click="start">
          <AppIcon name="quiz" :size="18" />
          Start quiz
        </button>
      </div>
    </div>

    <!-- Playing -->
    <div v-else-if="phase === 'playing' && current" class="stack">
      <div class="row-between">
        <button class="btn-text btn" @click="phase = 'setup'">
          <AppIcon name="close" :size="18" />
          Quit
        </button>
        <span class="label-md muted">{{ scopeLabel }}</span>
        <span class="label-md">Score {{ score }}</span>
      </div>

      <div class="progress" role="progressbar" :aria-valuenow="index + 1" :aria-valuemax="questions.length">
        <div class="progress-fill" :style="{ width: `${((index + (picked !== null ? 1 : 0)) / questions.length) * 100}%` }" />
      </div>
      <span class="label-sm muted">Question {{ index + 1 }} of {{ questions.length }}</span>

      <div class="card card-primary question">
        <span class="label-md">
          {{ mode === 'word' ? 'Which word means…' : mode === 'tagalog' ? 'In Tagalog, this is…' : 'What does this mean?' }}
        </span>
        <div class="row" style="justify-content: center">
          <span :class="mode === 'word' ? 'title-md' : 'headline-md'">{{ current.prompt }}</span>
          <SpeakButton v-if="mode !== 'word'" :text="current.word.word" />
        </div>
        <span v-if="mode !== 'word'" class="body-sm" style="font-style: italic">{{ current.word.partOfSpeech }}</span>
      </div>

      <div class="options">
        <button
          v-for="(option, i) in current.options"
          :key="i"
          class="card option"
          :class="optionState(i)"
          :disabled="picked !== null"
          @click="choose(i)"
        >
          <span class="option-key label-md">{{ i + 1 }}</span>
          <span class="body-lg spread">{{ option }}</span>
          <AppIcon v-if="optionState(i) === 'correct'" name="check" :size="20" />
          <AppIcon v-else-if="optionState(i) === 'wrong'" name="close" :size="20" />
        </button>
      </div>

      <div v-if="picked !== null" class="card feedback stack" style="gap: 6px">
        <span class="title-md" :class="picked === current.answerIndex ? 'ok-text' : 'bad-text'">
          {{ picked === current.answerIndex ? 'Correct!' : 'Not quite.' }}
        </span>
        <span class="body-md">
          <NuxtLink :to="`/word/${encodeURIComponent(current.word.word)}`" class="primary-text">
            <strong>{{ current.word.word }}</strong>
          </NuxtLink>
          — {{ current.word.definition }}
        </span>
        <span class="body-sm muted">{{ current.word.tagalogWord }} · {{ current.word.tagalogDefinition }}</span>
        <span v-if="current.word.example" class="body-sm muted">“{{ current.word.example }}”</span>
        <div class="row" style="justify-content: flex-end">
          <button class="btn" @click="next">{{ isLast ? 'See results' : 'Next' }}</button>
        </div>
      </div>
    </div>

    <!-- Results -->
    <div v-else-if="phase === 'done'" class="stack">
      <PageHeader title="Results" />

      <div class="card card-primary question">
        <span class="headline-lg">{{ score }} / {{ questions.length }}</span>
        <span class="title-md">{{ verdict }}</span>
        <span class="body-sm">{{ modes.find((m) => m.value === mode)?.label }} · {{ scopeLabel }}</span>
      </div>

      <div class="row">
        <button class="btn" :disabled="isLoading" @click="start">
          <AppIcon name="refresh" :size="18" />
          Play again
        </button>
        <button class="btn-text btn" @click="phase = 'setup'">Change settings</button>
      </div>

      <section v-if="missed.length" class="stack" style="gap: 8px">
        <span class="title-sm primary-text">Words to review</span>
        <WordListItem v-for="q in missed" :key="q.word.word" :word="q.word" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.mode-list { display: grid; gap: 8px; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); }

.mode {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 14px 16px;
  border: 2px solid transparent;
}

.mode.selected { border-color: var(--primary); background: var(--secondary-container); }

.select {
  flex: 1;
  min-width: 0;
  padding: 13px 0;
  border: 0;
  background: none;
  color: inherit;
  font: inherit;
}

.select:focus { outline: none; }
.select option, .select optgroup { background: var(--surface); color: var(--on-surface); }

.progress { height: 6px; border-radius: 3px; background: var(--surface-variant); overflow: hidden; }
.progress-fill { height: 100%; background: var(--primary); transition: width 0.25s ease; }

.question {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 16px;
  text-align: center;
  overflow-wrap: anywhere;
}

.options { display: flex; flex-direction: column; gap: 8px; }

.option {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px 16px;
  text-align: left;
  border: 2px solid transparent;
  transition: border-color 0.15s ease, opacity 0.15s ease, box-shadow 0.15s ease;
}

.option:not(:disabled):hover { box-shadow: 0 4px 10px var(--shadow); border-color: var(--outline-variant); }
.option:disabled { cursor: default; }

.option-key {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--surface-variant);
  color: var(--on-surface-variant);
}

.option.correct { border-color: var(--success); background: var(--success-container); color: var(--on-success-container); }
.option.wrong { border-color: var(--error); background: var(--error-container); color: var(--on-error-container); }
.option.dimmed { opacity: 0.55; }

.feedback { padding: 16px; }
.ok-text { color: var(--success); }
.bad-text { color: var(--error); }
</style>
