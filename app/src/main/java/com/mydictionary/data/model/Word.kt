package com.mydictionary.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "words",
    indices = [Index("category"), Index("partOfSpeech")]
)
data class Word(
    @PrimaryKey val word: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val tagalogWord: String,
    val tagalogDefinition: String,
    val category: String = "",
    val categoryGroup: String = ""
)
