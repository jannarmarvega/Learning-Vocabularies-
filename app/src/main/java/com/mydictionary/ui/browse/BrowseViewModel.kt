package com.mydictionary.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.data.model.CategorySummary
import com.mydictionary.data.model.PartOfSpeechSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BrowseViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository

    /** Categories bundled under their group heading, in the order the query returns them. */
    val groupedCategories: StateFlow<List<Pair<String, List<CategorySummary>>>> =
        repository.getCategories()
            .map { categories ->
                categories
                    .groupBy { it.categoryGroup }
                    .map { (group, items) -> group to items }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val partsOfSpeech: StateFlow<List<PartOfSpeechSummary>> = repository.getPartsOfSpeech()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
