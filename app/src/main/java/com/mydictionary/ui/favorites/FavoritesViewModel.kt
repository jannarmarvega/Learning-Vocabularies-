package com.mydictionary.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.data.model.Word
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FavoritesViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository

    val favorites: StateFlow<List<Word>> = repository.getFavoriteWords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
