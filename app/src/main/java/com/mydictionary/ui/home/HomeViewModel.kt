package com.mydictionary.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.data.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository

    private val _wordOfTheDay = MutableStateFlow<Word?>(null)
    val wordOfTheDay: StateFlow<Word?> = _wordOfTheDay.asStateFlow()

    private val _surpriseWord = MutableStateFlow<Word?>(null)
    val surpriseWord: StateFlow<Word?> = _surpriseWord.asStateFlow()

    init {
        viewModelScope.launch {
            _wordOfTheDay.value = repository.getWordOfTheDay()
        }
        shuffle()
    }

    fun shuffle() {
        viewModelScope.launch {
            _surpriseWord.value = repository.getRandomWord()
        }
    }
}
