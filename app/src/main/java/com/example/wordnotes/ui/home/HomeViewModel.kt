package com.example.wordnotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordItem(
    val word: Word,
    val isSelected: Boolean = false
)

data class WordsUiState(
    val wordItems: List<WordItem> = emptyList(),
    val isShowEmptyScreen: Boolean = false,
    val isLoading: Boolean = false,
)

data class ActionModeUiState(
    val isActionMode: Boolean = false,
    val selectedCount: Int = 0
)

data class SearchUiState(
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResult: List<WordItem> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {
    private val _clickItemEvent: MutableStateFlow<String?> = MutableStateFlow(null)
    val clickItemEvent: StateFlow<String?> = _clickItemEvent.asStateFlow()

    private val _clickEditItemEvent: MutableStateFlow<String?> = MutableStateFlow(null)
    val clickEditItemEvent: StateFlow<String?> = _clickEditItemEvent.asStateFlow()

    private val _undoEvent: MutableStateFlow<Int?> = MutableStateFlow(null)
    val undoEvent: StateFlow<Int?> = _undoEvent.asStateFlow()

    private val _selectedIds = MutableStateFlow(emptySet<String>())
    private val _temporalDeletedIds = MutableStateFlow(emptySet<String>())
    private val _isLoading = MutableStateFlow(false)
    private val _isActionMode = MutableStateFlow(false)
    private val _isSearching = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val searchUiState: StateFlow<SearchUiState> = combine(
        wordRepository.getWordsFlow(),
        _isSearching,
        _selectedIds,
        _temporalDeletedIds,
        _searchQuery
    ) { words, isSearching, selectedIds, temporalDeletedIds, searchQuery ->
        SearchUiState(
            isSearching = isSearching,
            searchQuery = searchQuery,
            searchResult = if (searchQuery.isBlank())
                emptyList() else
                words
                    .filterNot { temporalDeletedIds.contains(it.id) }
                    .filter { it.word.contains(searchQuery) }
                    .map { WordItem(word = it, isSelected = selectedIds.contains(it.id)) },
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState()
        )

    val actionModeUiState: StateFlow<ActionModeUiState> = combine(_isActionMode, _selectedIds) { isActionMode, selectedId ->
        ActionModeUiState(
            isActionMode = isActionMode,
            selectedCount = selectedId.size
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActionModeUiState()
        )

    val wordsUiState: StateFlow<WordsUiState> = combine(
        wordRepository.getWordsFlow(),
        _selectedIds,
        _isLoading,
        _temporalDeletedIds
    ) { words, selectedIds, isLoading, temporalDeletedIds ->
        WordsUiState(
            wordItems = words
                .filterNot { temporalDeletedIds.contains(it.id) }
                .map { WordItem(word = it, isSelected = selectedIds.contains(it.id)) },
            isShowEmptyScreen = words.isEmpty() and isLoading.not(),
            isLoading = isLoading,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WordsUiState()
        )

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _isLoading.update { true }
        wordRepository.refresh()
        _isLoading.update { false }
    }

    fun itemClicked(wordId: String) {
        if (_isActionMode.value.not()) {
            _clickItemEvent.update { wordId }
            _clickItemEvent.update { null }
            return
        }
        selectItem(wordId)
    }

    fun itemLongClicked(wordId: String): Boolean {
        if (_isActionMode.value.not()) {
            _isActionMode.update { true }
        }
        selectItem(wordId)
        return true
    }

    private fun selectItem(wordId: String) {
        val updatedWordIds = _selectedIds.value.toMutableSet()
        if (!updatedWordIds.add(wordId)) {
            updatedWordIds.remove(wordId)
        }
        if (updatedWordIds.isEmpty()) {
            destroyActionMode()
            return
        }
        _selectedIds.update { updatedWordIds }
    }

    fun onActionModeMenuEdit(): Boolean {
        _clickEditItemEvent.update { _selectedIds.value.firstOrNull() }
        _clickEditItemEvent.update { null }
        destroyActionMode()
        return true
    }

    fun onActionModeMenuDelete(): Boolean {
        _temporalDeletedIds.update { _selectedIds.value }
        val selectedCount = _selectedIds.value.size
        destroyActionMode()
        _undoEvent.update { selectedCount }
        _undoEvent.update { null }
        return true
    }

    fun onUndoDismissed() {
        viewModelScope.launch {
            wordRepository.deleteWords(_temporalDeletedIds.value.toList())
            _temporalDeletedIds.update { emptySet() }
        }
    }

    fun undoDeletion() {
        _temporalDeletedIds.update { emptySet() }
    }

    fun onActionModeMenuRemind(): Boolean {
        viewModelScope.launch {
            val wordItems = if (_isSearching.value)
                searchUiState.value.searchResult else
                wordsUiState.value.wordItems
            val updatedWords = wordItems
                .filter { _selectedIds.value.contains(it.word.id) }
                .map { it.word.copy(isRemind = true) }
            wordRepository.updateWords(updatedWords)
            destroyActionMode()
        }
        return true
    }

    fun onActionModeMenuSelectAll(): Boolean {
        val wordItems = if (_isSearching.value)
            searchUiState.value.searchResult else
            wordsUiState.value.wordItems
        _selectedIds.update { wordItems.map { it.word.id }.toSet() }
        return true
    }

    fun destroyActionMode() {
        _isActionMode.update { false }
        _selectedIds.update { emptySet() }
    }

    fun startSearching() {
        if (!_isSearching.value) {
            _isSearching.update { true }
        }
    }

    fun stopSearching() {
        if (_isSearching.value) {
            _isSearching.update { false }
            _searchQuery.update { "" }
        }
    }

    fun search(text: String) {
        _searchQuery.update { text }
    }
}