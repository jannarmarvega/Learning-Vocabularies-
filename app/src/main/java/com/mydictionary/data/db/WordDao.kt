package com.mydictionary.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mydictionary.data.model.CategorySummary
import com.mydictionary.data.model.PartOfSpeechSummary
import com.mydictionary.data.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<Word>)

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Query("SELECT * FROM words WHERE word = :word")
    fun getWord(word: String): Flow<Word?>

    @Query("SELECT * FROM words WHERE word = :word")
    suspend fun getWordOnce(word: String): Word?

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWord(): Word?

    /**
     * One paged, filtered slice of the dictionary. A blank [query] lists everything;
     * a null [partOfSpeech] or [category] leaves that filter off.
     */
    @Query(
        "SELECT * FROM words WHERE " +
            "(:query = '' OR word LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' " +
            "OR tagalogWord LIKE '%' || :query || '%' OR tagalogDefinition LIKE '%' || :query || '%') " +
            "AND (:partOfSpeech IS NULL OR partOfSpeech = :partOfSpeech) " +
            "AND (:category IS NULL OR category = :category) " +
            "AND (:categoryGroup IS NULL OR categoryGroup = :categoryGroup) " +
            "ORDER BY CASE WHEN word LIKE :query || '%' THEN 0 ELSE 1 END, word " +
            "LIMIT :limit OFFSET :offset"
    )
    suspend fun searchPage(
        query: String,
        partOfSpeech: String?,
        category: String?,
        categoryGroup: String?,
        limit: Int,
        offset: Int
    ): List<Word>

    @Query(
        "SELECT category, categoryGroup, COUNT(*) AS wordCount FROM words " +
            "WHERE category != '' GROUP BY categoryGroup, category ORDER BY categoryGroup, category"
    )
    fun getCategories(): Flow<List<CategorySummary>>

    @Query(
        "SELECT partOfSpeech, COUNT(*) AS wordCount FROM words " +
            "WHERE partOfSpeech != '' GROUP BY partOfSpeech ORDER BY COUNT(*) DESC"
    )
    fun getPartsOfSpeech(): Flow<List<PartOfSpeechSummary>>
}
