export default defineEventHandler(() =>
  getDb()
    .prepare(
      `SELECT partOfSpeech, COUNT(*) AS wordCount FROM words
       WHERE partOfSpeech != '' GROUP BY partOfSpeech ORDER BY COUNT(*) DESC`
    )
    .all()
)
