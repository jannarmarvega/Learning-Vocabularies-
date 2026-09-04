package com.mydictionary.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mydictionary.data.model.Favorite
import com.mydictionary.data.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE word = :word")
    suspend fun delete(word: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE word = :word)")
    fun isFavorite(word: String): Flow<Boolean>

    @Query("SELECT w.* FROM words w INNER JOIN favorites f ON w.word = f.word ORDER BY f.addedAt DESC")
    fun getFavoriteWords(): Flow<List<Word>>
}
