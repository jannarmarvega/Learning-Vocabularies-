package com.mydictionary.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mydictionary.data.model.Learning
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    @Insert
    suspend fun insert(learning: Learning): Long

    @Update
    suspend fun update(learning: Learning)

    @Query("DELETE FROM learnings WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM learnings WHERE id = :id")
    suspend fun getById(id: Long): Learning?

    @Query("SELECT * FROM learnings ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Learning>>

    @Query(
        "SELECT * FROM learnings WHERE word LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' " +
            "ORDER BY updatedAt DESC"
    )
    fun search(query: String): Flow<List<Learning>>
}
