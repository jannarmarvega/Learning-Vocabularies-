package com.mydictionary.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mydictionary.MyDictionaryApplication
import com.mydictionary.data.model.PartOfSpeechSummary
import com.mydictionary.data.model.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as MyDictionaryApplication).repository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** null means "every part of speech". */
    private val _partOfSpeech = MutableStateFlow<String?>(null)
    val partOfSpeech: StateFlow<String?> = _partOfSpeech.asStateFlow()

    private val _items = MutableStateFlow<List<Word>>(emptyList())
    val items: StateFlow<List<Word>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    val partsOfSpeech: StateFlow<List<PartOfSpeechSummary>> = repository.getPartsOfSpeech()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val mutex = Mutex()

    init {
        viewModelScope.launch {
            combine(_query.debounce(250), _partOfSpeech) { q, pos -> q to pos }
                .collectLatest { (q, pos) ->
                    mutex.withLock {
                        _items.value = emptyList()
                        _hasMore.value = true
                        loadPageLocked(q, pos, 0)
                    }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun onPartOfSpeechChange(newPartOfSpeech: String?) {
        _partOfSpeech.value = newPartOfSpeech
    }

    fun loadMore() {
        val q = _query.value
        val pos = _partOfSpeech.value
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            mutex.withLock {
                if (q != _query.value || pos != _partOfSpeech.value) return@withLock
                loadPageLocked(q, pos, _items.value.size)
            }
        }
    }

    private suspend fun loadPageLocked(query: String, partOfSpeech: String?, offset: Int) {
        _isLoading.value = true
        try {
            val page = repository.searchPage(
                query = query,
                partOfSpeech = partOfSpeech,
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
