package com.mydictionary.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mydictionary.data.model.Settings

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(settings: Settings)

    @Query("SELECT value FROM settings WHERE `key` = :key")
    suspend fun get(key: String): String?
}
