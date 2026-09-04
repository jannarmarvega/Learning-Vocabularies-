package com.mydictionary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learnings")
data class Learning(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
) {
    val isEdited: Boolean get() = updatedAt > createdAt
}
