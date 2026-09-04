package com.mydictionary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val word: String,
    val addedAt: Long = System.currentTimeMillis()
)
