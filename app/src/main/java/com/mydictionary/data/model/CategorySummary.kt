package com.mydictionary.data.model

/** One browsable category with the number of words filed under it. */
data class CategorySummary(
    val category: String,
    val categoryGroup: String,
    val wordCount: Int
)

/** One part of speech with the number of words that use it. */
data class PartOfSpeechSummary(
    val partOfSpeech: String,
    val wordCount: Int
)
