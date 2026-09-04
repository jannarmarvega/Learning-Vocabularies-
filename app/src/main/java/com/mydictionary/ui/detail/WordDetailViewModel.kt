package com.mydictionary.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.data.DictionaryRepository
import com.mydictionary.data.model.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WordDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository
    private val speech = (app as MyDictionaryApplication).speech

    private val _wordKey = MutableStateFlow("")

    val word: StateFlow<Word?> = _wordKey
        .flatMapLatest { repository.getWord(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isFavorite: StateFlow<Boolean> = _wordKey
        .flatMapLatest { repository.isFavorite(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _accent = MutableStateFlow(DictionaryRepository.ACCENT_BRITISH)
    val accent: StateFlow<String> = _accent.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = repository.getSpeechAccent()
            _accent.value = stored
            speech.setAccent(stored)
        }
    }

    fun setWord(key: String) {
        if (_wordKey.value != key) _wordKey.value = key
    }

    fun toggleFavorite() {
        val key = _wordKey.value
        if (key.isNotEmpty()) {
            viewModelScope.launch { repository.toggleFavorite(key) }
        }
    }

    fun setAccent(languageTag: String) {
        if (_accent.value == languageTag) return
        _accent.value = languageTag
        speech.setAccent(languageTag)
        viewModelScope.launch { repository.setSpeechAccent(languageTag) }
    }

    fun speak(text: String) = speech.speak(text)
}
