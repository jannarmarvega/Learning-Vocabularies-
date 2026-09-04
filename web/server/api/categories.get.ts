interface CategorySummary {
  category: string
  categoryGroup: string
  wordCount: number
}

/** Categories bundled under their group heading, in query order. */
export default defineEventHandler(() => {
  const rows = getDb()
    .prepare(
      `SELECT category, categoryGroup, COUNT(*) AS wordCount FROM words
       WHERE category != '' GROUP BY categoryGroup, category ORDER BY categoryGroup, category`
    )
    .all() as CategorySummary[]

  const groups: { group: string; wordCount: number; categories: CategorySummary[] }[] = []
  for (const row of rows) {
    let bucket = groups.find((g) => g.group === row.categoryGroup)
    if (!bucket) {
      bucket = { group: row.categoryGroup, wordCount: 0, categories: [] }
      groups.push(bucket)
    }
    bucket.categories.push(row)
    bucket.wordCount += row.wordCount
  }
  return groups
})
