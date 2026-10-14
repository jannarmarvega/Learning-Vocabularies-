import type { Word } from '../utils/db'

const OPTION_COUNT = 4
const MODES = ['meaning', 'word', 'tagalog'] as const
type QuizMode = (typeof MODES)[number]

/** What the player is shown, and what each answer option reads as, per mode. */
const prompt: Record<QuizMode, (w: Word) => string> = {
  meaning: (w) => w.word,
  word: (w) => w.definition,
  tagalog: (w) => w.word
}
const answer: Record<QuizMode, (w: Word) => string> = {
  meaning: (w) => w.definition,
  word: (w) => w.word,
  tagalog: (w) => w.tagalogWord
}

function shuffle<T>(items: T[]): T[] {
  for (let i = items.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[items[i], items[j]] = [items[j]!, items[i]!]
  }
  return items
}

/**
 * A round of multiple-choice questions, optionally limited to one category or
 * group. Wrong options come from the same category first so they are plausible,
 * then from the same part of speech anywhere in the dictionary.
 */
export default defineEventHandler(async (event) => {
  const q = getQuery(event)
  const mode: QuizMode = MODES.includes(q.mode as QuizMode) ? (q.mode as QuizMode) : 'meaning'
  const category = q.category ? String(q.category) : null
  const categoryGroup = q.group ? String(q.group) : null
  const count = Math.min(50, Math.max(1, Number(q.count ?? 10) || 10))

  const db = await getDb()
  const words = db
    .prepare(
      `SELECT * FROM words
       WHERE (@category IS NULL OR category = @category)
         AND (@categoryGroup IS NULL OR categoryGroup = @categoryGroup)
       ORDER BY RANDOM() LIMIT @count`
    )
    .all({ category, categoryGroup, count }) as Word[]

  const sameCategory = db.prepare(
    'SELECT * FROM words WHERE category = ? AND word != ? ORDER BY RANDOM() LIMIT 30'
  )
  const samePos = db.prepare(
    'SELECT * FROM words WHERE partOfSpeech = ? AND word != ? ORDER BY RANDOM() LIMIT 30'
  )
  const anyWord = db.prepare('SELECT * FROM words WHERE word != ? ORDER BY RANDOM() LIMIT 30')

  const questions = words.map((w) => {
    const correct = answer[mode](w)
    const seen = new Set([correct.toLowerCase()])
    const distractors: string[] = []
    const pools = [
      () => sameCategory.all(w.category, w.word) as Word[],
      () => samePos.all(w.partOfSpeech, w.word) as Word[],
      () => anyWord.all(w.word) as Word[]
    ]
    for (const pool of pools) {
      for (const other of pool()) {
        const text = answer[mode](other)
        if (!text || seen.has(text.toLowerCase())) continue
        seen.add(text.toLowerCase())
        distractors.push(text)
        if (distractors.length === OPTION_COUNT - 1) break
      }
      if (distractors.length === OPTION_COUNT - 1) break
    }

    const options = shuffle([correct, ...distractors])
    return {
      word: w,
      prompt: prompt[mode](w),
      options,
      answerIndex: options.indexOf(correct)
    }
  })

  return { mode, questions }
})
