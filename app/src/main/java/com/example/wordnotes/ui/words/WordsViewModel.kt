package com.example.wordnotes.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.Event
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordItem(
    val word: Word,
    val isSelected: Boolean = false
)

data class WordsUiState(
    val items: List<WordItem> = emptyList(),
    val firstEmit: Boolean = false,
    val isLoading: Boolean = false,
    val isActionMode: Boolean = false,
    val selectedCount: Int = 0,
    val isSearching: Boolean = false,
    val searchResult: List<WordItem> = emptyList()
)

class WordsViewModel(private val wordsRepository: WordsRepository) : ViewModel() {
    private val _clickItemEvent: MutableLiveData<Event<String>> = MutableLiveData()
    val clickItemEvent: LiveData<Event<String>> = _clickItemEvent

    private val _clickEditItemEvent: MutableLiveData<Event<String?>> = MutableLiveData()
    val clickEditItemEvent: LiveData<Event<String?>> = _clickEditItemEvent

    private val _showUndoEvent: MutableLiveData<Event<Int>> = MutableLiveData()
    val showUndoEvent: LiveData<Event<Int>> = _showUndoEvent

    private val _isLoading = MutableStateFlow(false)
    private val _isActionMode = MutableStateFlow(false)
    private val _selectedWordIds = MutableStateFlow(emptySet<String>())
    private val _isSearching = MutableStateFlow(false)

    private val _tempDeleted = MutableStateFlow(emptySet<String>())
    private val _temporalItems: Flow<Result<List<Word>>> = combine(
        wordsRepository.getWordsStream(), _tempDeleted
    ) { wordsResult, tempDeleted ->
        when (wordsResult) {
            is Result.Success -> Result.Success(notInSelected(wordsResult.data, tempDeleted))
            is Result.Error -> wordsResult
            is Result.Loading -> wordsResult
        }
    }

    private val _wordItemsResult: Flow<Result<List<WordItem>>> = combine(
        _temporalItems, _selectedWordIds
    ) { temporalItems, selectedWordId ->
        when (temporalItems) {
            is Result.Success -> Result.Success(resolveSelected(temporalItems.data, selectedWordId))
            is Result.Error -> temporalItems
            is Result.Loading -> temporalItems
        }
    }
    private val _searchQuery = MutableStateFlow("")
    private val _searchResult: Flow<List<WordItem>> = combine(
        wordsRepository.getWordsStream(), _searchQuery, _selectedWordIds
    ) { wordsResult, searchQuery, selectedWordIds ->
        when (wordsResult) {
            is Result.Success -> resolveSelected(filterSearch(wordsResult.data, searchQuery), selectedWordIds)
            is Result.Error -> emptyList()
            is Result.Loading -> emptyList()
        }
    }

    val uiState: StateFlow<WordsUiState> = combine(
        _isLoading, _isActionMode, _wordItemsResult, _isSearching, _searchResult
    ) { isLoading, isActionMode, wordsResult, isSearching, searchResult ->
        when (wordsResult) {
            is Result.Success -> WordsUiState(
                items = wordsResult.data,
                isLoading = isLoading,
                isActionMode = isActionMode,
                selectedCount = _selectedWordIds.value.size,
                isSearching = isSearching,
                searchResult = searchResult
            )

            is Result.Loading -> WordsUiState(isLoading = true)

            is Result.Error -> WordsUiState()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WordsUiState(firstEmit = true, isLoading = true)
        )

    val selectedCount: Int get() = _selectedWordIds.value.count()

    init {
        viewModelScope.launch {
            wordsRepository.refreshWords()
        }
    }

    private fun notInSelected(words: List<Word>, selectedWordId: Set<String>): List<Word> {
        return words.filterNot { selectedWordId.contains(it.id) }
    }

    private fun resolveSelected(words: List<Word>, selectedWordId: Set<String>): List<WordItem> {
        return words.map { WordItem(word = it, isSelected = it.id in selectedWordId) }
    }

    private fun filterSearch(data: List<Word>, searchQuery: String): List<Word> {
        return if (searchQuery.isNotEmpty() && searchQuery.isNotBlank()) data.filter { it.word.contains(searchQuery) } else emptyList()
    }

    fun refresh() {
        _isLoading.value = true
        viewModelScope.launch {
            wordsRepository.refreshWords()
            _isLoading.value = false
        }
    }

    fun itemClicked(wordId: String) {
        if (!uiState.value.isActionMode) {
            _clickItemEvent.value = Event(wordId)
            return
        }
        selectItem(wordId)
    }

    fun itemLongClicked(wordId: String): Boolean {
        if (!uiState.value.isActionMode) {
            _isActionMode.value = true
        }
        selectItem(wordId)
        return true
    }

    private fun selectItem(wordId: String) {
        val updatedWordIds = _selectedWordIds.value.toMutableSet()
        if (!updatedWordIds.add(wordId)) updatedWordIds.remove(wordId)

        if (updatedWordIds.isEmpty()) {
            destroyActionMode()
            return
        }

        _selectedWordIds.update { updatedWordIds }
    }

    fun onActionModeMenuEdit(): Boolean {
        if (uiState.value.isActionMode && uiState.value.selectedCount == 1) {
            val selectedWord = uiState.value.items.find { it.isSelected }
            _clickEditItemEvent.value = Event(selectedWord?.word?.id)
            destroyActionMode()
        }
        return true
    }

    fun onActionModeMenuDelete(): Boolean {
        if (uiState.value.isActionMode) {
            val selectedWords = uiState.value.items.filter { it.isSelected }
            _showUndoEvent.value = Event(selectedWords.size)
            _tempDeleted.update { _selectedWordIds.value }
            destroyActionMode()
        }
        return true
    }

    fun undoDismissed() {
        viewModelScope.launch {
            wordsRepository.deleteWords(_tempDeleted.value.toList())
            _tempDeleted.update { emptySet() }
        }
    }

    fun undoDeletion() {
        _tempDeleted.update { emptySet() }
    }

    fun onActionModeMenuRemind(): Boolean {
        if (uiState.value.isActionMode) {
            val selectedWords = uiState.value.items.filter { it.isSelected }
            viewModelScope.launch {
                wordsRepository.remindWords(selectedWords.map { it.word.id })
                destroyActionMode()
            }
        }
        return true
    }

    fun onActionModeMenuSelectAll(): Boolean {
        if (uiState.value.isActionMode) {
            _selectedWordIds.update { uiState.value.items.map { it.word.id }.toSet() }
        }
        return true
    }

    fun destroyActionMode() {
        _isActionMode.update { false }
        _selectedWordIds.update { emptySet() }
    }

    fun startSearching() {
        if (!_isSearching.value) {
            _isSearching.value = true
        }
    }

    fun stopSearching() {
        if (_isSearching.value) {
            _isSearching.value = false
            search("")
        }
    }

    fun search(text: String) {
        _searchQuery.value = text
    }
}