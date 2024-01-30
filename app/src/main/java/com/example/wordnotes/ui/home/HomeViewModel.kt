package com.example.wordnotes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    val selectedIds: Set<String> = emptySet(),
    val undoingItemCount: Int? = null
)

data class SearchUiState(
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResult: List<WordItem> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val wordRepository: WordRepository) : ViewModel() {

    private val _undoingItemCount = MutableStateFlow<Int?>(null)
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

    val actionModeUiState: StateFlow<ActionModeUiState> = combine(
        _isActionMode,
        _selectedIds,
        _undoingItemCount
    ) { isActionMode, selectedIds, undoingItemCount ->
        ActionModeUiState(
            isActionMode = isActionMode,
            selectedIds = selectedIds,
            undoingItemCount = undoingItemCount
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
        _temporalDeletedIds,
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

    fun selectItem(wordId: String) {
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

    fun onItemLongClicked(wordId: String): Boolean {
        if (_isActionMode.value.not()) {
            _isActionMode.update { true }
        }
        selectItem(wordId)
        return true
    }

    fun onActionModeMenuDelete(): Boolean {
        _temporalDeletedIds.update { _selectedIds.value }
        val selectedCount = _selectedIds.value.size
        destroyActionMode()
        _undoingItemCount.update { selectedCount }
        return true
    }

    fun onUndoDismissed() {
        viewModelScope.launch {
            wordRepository.deleteWords(_temporalDeletedIds.value.toList())
            _temporalDeletedIds.update { emptySet() }
            _undoingItemCount.update { null }
        }
    }

    fun undoDeletion() {
        _temporalDeletedIds.update { emptySet() }
        _undoingItemCount.update { null }
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