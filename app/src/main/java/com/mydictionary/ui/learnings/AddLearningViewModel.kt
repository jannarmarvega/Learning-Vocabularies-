package com.mydictionary.ui.learnings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Backs both "add a learning" and "edit an existing learning". */
class AddLearningViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository

    private val _word = MutableStateFlow("")
    val word: StateFlow<String> = _word.asStateFlow()

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private var learningId: Long = NEW_LEARNING

    /** Called once per screen entry; [id] of 0 starts a blank note for [initialWord]. */
    fun start(id: Long, initialWord: String) {
        if (_isLoaded.value && learningId == id) return
        learningId = id
        if (id == NEW_LEARNING) {
            _word.value = initialWord
            _text.value = ""
            _isEditing.value = false
            _isLoaded.value = true
        } else {
            _isEditing.value = true
            viewModelScope.launch {
                val existing = repository.getLearning(id)
                if (existing != null) {
                    _word.value = existing.word
                    _text.value = existing.text
                } else {
                    // The note was deleted from elsewhere; fall back to a new one.
                    learningId = NEW_LEARNING
                    _isEditing.value = false
                    _word.value = initialWord
                }
                _isLoaded.value = true
            }
        }
    }

    fun onWordChange(value: String) {
        _word.value = value
    }

    fun onTextChange(value: String) {
        _text.value = value
    }

    fun save(onDone: () -> Unit) {
        val word = _word.value
        val text = _text.value
        if (word.isBlank() || text.isBlank()) return
        viewModelScope.launch {
            if (learningId == NEW_LEARNING) {
                repository.addLearning(word, text)
            } else {
                repository.updateLearning(learningId, word, text)
            }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = learningId
        if (id == NEW_LEARNING) {
            onDone()
            return
        }
        viewModelScope.launch {
            repository.deleteLearning(id)
            onDone()
        }
    }

    companion object {
        const val NEW_LEARNING = 0L
    }
}
