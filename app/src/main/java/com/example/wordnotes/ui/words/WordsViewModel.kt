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
    val isLoading: Boolean = false,
    val isActionMode: Boolean = false,
    val selectedCount: Int = 0
)

class WordsViewModel(
    private val wordsRepository: WordsRepository
) : ViewModel() {

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _isActionMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _selectedWordIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    private val _wordItemsResult: Flow<Result<List<WordItem>>> = combine(
        wordsRepository.observeWords(), _selectedWordIds
    ) { wordsResult, selectedWordId ->
        when (wordsResult) {
            is Result.Success -> Result.Success(resolveSelected(wordsResult.data, selectedWordId))
            is Result.Error -> wordsResult
            is Result.Loading -> wordsResult
        }
    }

    val uiState: StateFlow<WordsUiState> = combine(
        _isLoading, _isActionMode, _wordItemsResult
    ) { isLoading, isActionMode, wordsResult ->
        when (wordsResult) {
            is Result.Success -> WordsUiState(
                items = wordsResult.data,
                isLoading = isLoading,
                isActionMode = isActionMode,
                selectedCount = _selectedWordIds.value.size
            )

            is Result.Loading -> WordsUiState(isLoading = true)

            is Result.Error -> WordsUiState()
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WordsUiState(isLoading = true)
        )

    private val _clickItemEvent: MutableLiveData<Event<String>> = MutableLiveData()
    val clickItemEvent: LiveData<Event<String>> = _clickItemEvent

    private val _clickEditItemEvent: MutableLiveData<Event<String?>> = MutableLiveData()
    val clickEditItemEvent: LiveData<Event<String?>> = _clickEditItemEvent

    private fun resolveSelected(words: List<Word>, selectedWordId: Set<String>): List<WordItem> {
        return words.map { WordItem(word = it, isSelected = it.id in selectedWordId) }
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
            viewModelScope.launch {
                wordsRepository.deleteWords(selectedWords.map { it.word.id })
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
}