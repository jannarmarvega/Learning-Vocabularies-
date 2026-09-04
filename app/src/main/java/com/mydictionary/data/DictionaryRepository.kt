package com.mydictionary.data

import com.mydictionary.data.db.FavoriteDao
import com.mydictionary.data.db.LearningDao
import com.mydictionary.data.db.SettingsDao
import com.mydictionary.data.db.WordDao
import com.mydictionary.data.model.CategorySummary
import com.mydictionary.data.model.Favorite
import com.mydictionary.data.model.Learning
import com.mydictionary.data.model.PartOfSpeechSummary
import com.mydictionary.data.model.Settings
import com.mydictionary.data.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DictionaryRepository(
    private val wordDao: WordDao,
    private val favoriteDao: FavoriteDao,
    private val learningDao: LearningDao,
    private val settingsDao: SettingsDao
) {

    fun getWord(word: String): Flow<Word?> = wordDao.getWord(word)

    /** One page of words, narrowed by any combination of text, part of speech and category. */
    suspend fun searchPage(
        query: String = "",
        partOfSpeech: String? = null,
        category: String? = null,
        categoryGroup: String? = null,
        offset: Int,
        limit: Int
    ): List<Word> = wordDao.searchPage(
        query = query.trim(),
        partOfSpeech = partOfSpeech,
        category = category,
        categoryGroup = categoryGroup,
        limit = limit,
        offset = offset
    )

    fun getCategories(): Flow<List<CategorySummary>> = wordDao.getCategories()

    fun getPartsOfSpeech(): Flow<List<PartOfSpeechSummary>> = wordDao.getPartsOfSpeech()

    suspend fun getRandomWord(): Word? = wordDao.getRandomWord()

    suspend fun getWordOfTheDay(): Word? {
        val today = todayKey()
        val storedDate = settingsDao.get(KEY_WOD_DATE)
        if (storedDate == today) {
            val storedWord = settingsDao.get(KEY_WOD_WORD) ?: return null
            return wordDao.getWordOnce(storedWord)
        }
        val random = wordDao.getRandomWord() ?: return null
        settingsDao.put(Settings(KEY_WOD_DATE, today))
        settingsDao.put(Settings(KEY_WOD_WORD, random.word))
        return random
    }

    fun isFavorite(word: String): Flow<Boolean> = favoriteDao.isFavorite(word)

    fun getFavoriteWords(): Flow<List<Word>> = favoriteDao.getFavoriteWords()

    suspend fun toggleFavorite(word: String) {
        val isFav = favoriteDao.isFavorite(word).firstOrNull() ?: false
        if (isFav) {
            favoriteDao.delete(word)
        } else {
            favoriteDao.insert(Favorite(word = word))
        }
    }

    fun getLearnings(): Flow<List<Learning>> = learningDao.getAll()

    fun searchLearnings(query: String): Flow<List<Learning>> =
        if (query.isBlank()) learningDao.getAll() else learningDao.search(query.trim())

    suspend fun getLearning(id: Long): Learning? = learningDao.getById(id)

    suspend fun addLearning(word: String, text: String): Long =
        learningDao.insert(Learning(word = word.trim(), text = text.trim()))

    /** Rewrites an existing note, keeping its original creation time. */
    suspend fun updateLearning(id: Long, word: String, text: String) {
        val existing = learningDao.getById(id) ?: return
        learningDao.update(
            existing.copy(
                word = word.trim(),
                text = text.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteLearning(id: Long) = learningDao.delete(id)

    /** "en-GB" or "en-US"; drives which accent the word is spoken in. */
    suspend fun getSpeechAccent(): String = settingsDao.get(KEY_ACCENT) ?: ACCENT_BRITISH

    suspend fun setSpeechAccent(tag: String) = settingsDao.put(Settings(KEY_ACCENT, tag))

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

    companion object {
        private const val KEY_WOD_DATE = "word_of_day_date"
        private const val KEY_WOD_WORD = "word_of_day_word"
        private const val KEY_ACCENT = "speech_accent"

        const val ACCENT_BRITISH = "en-GB"
        const val ACCENT_AMERICAN = "en-US"
    }
}
