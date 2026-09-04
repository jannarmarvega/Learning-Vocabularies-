package com.mydictionary.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.data.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Pages through one fixed slice of the dictionary: a category, a group, or a part of speech. */
class WordListViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository

    private val _items = MutableStateFlow<List<Word>>(emptyList())
    val items: StateFlow<List<Word>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val mutex = Mutex()
    private var filter: Filter? = null

    data class Filter(
        val category: String? = null,
        val categoryGroup: String? = null,
        val partOfSpeech: String? = null
    )

    fun setFilter(newFilter: Filter) {
        if (filter == newFilter) return
        filter = newFilter
        viewModelScope.launch {
            mutex.withLock {
                _items.value = emptyList()
                _hasMore.value = true
                loadPageLocked(newFilter, 0)
            }
        }
    }

    fun loadMore() {
        val current = filter ?: return
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            mutex.withLock {
                if (current != filter) return@withLock
                loadPageLocked(current, _items.value.size)
            }
        }
    }

    private suspend fun loadPageLocked(current: Filter, offset: Int) {
        _isLoading.value = true
        try {
            val page = repository.searchPage(
                partOfSpeech = current.partOfSpeech,
                category = current.category,
                categoryGroup = current.categoryGroup,
                offset = offset,
                limit = PAGE_SIZE
            )
            _items.value = _items.value + page
            _hasMore.value = page.size == PAGE_SIZE
        } finally {
            _isLoading.value = false
        }
    }

    companion object {
        const val PAGE_SIZE = 50
    }
}
