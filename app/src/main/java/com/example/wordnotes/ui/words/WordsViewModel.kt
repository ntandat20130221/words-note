package com.example.wordnotes.ui.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.Event
import com.example.wordnotes.data.onLoading
import com.example.wordnotes.data.onSuccess
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.toWordUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class ActionModeState { STARTED, STOPPED }

data class WordUiState(
    val id: String = UUID.randomUUID().toString(),
    val word: String = "",
    val pos: String = "",
    val ipa: String = "",
    val meaning: String = "",
    val isLearning: Boolean = false,
    val isSelected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class WordsUiState(
    val words: List<WordUiState> = emptyList(),
    val isActionMode: Boolean = false,
    val selectedCount: Int = 0
)

class WordsViewModel(private val wordRepository: WordRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<WordsUiState> = MutableStateFlow(WordsUiState())
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()

    private val _clickItemEvent: MutableLiveData<Event<String?>> = MutableLiveData()
    val clickItemEvent: LiveData<Event<String?>> = _clickItemEvent

    private val _actionModeEvent: MutableLiveData<Event<ActionModeState>> = MutableLiveData()
    val actionModeEvent: LiveData<Event<ActionModeState>> = _actionModeEvent

    init {
        viewModelScope.launch {
            wordRepository.observeWords().collect { result ->
                result.onSuccess {
                    _uiState.value = WordsUiState(words = it.map { word -> word.toWordUiState() })
                }
                result.onLoading {
                    // TODO("Notify users using SnackBar, etc.")
                }
            }
        }
    }

    fun itemClicked(wordId: String) {
        if (!_uiState.value.isActionMode) {
            _clickItemEvent.value = Event(wordId)
            return
        }
        selectItem(wordId)
    }

    fun itemLongClicked(wordId: String): Boolean {
        if (!_uiState.value.isActionMode) {
            _actionModeEvent.value = Event(ActionModeState.STARTED)
        }
        selectItem(wordId)
        return true
    }

    private fun selectItem(wordId: String) {
        val updatedWords = _uiState.value.words.map { word -> if (word.id == wordId) word.copy(isSelected = !word.isSelected) else word }
        val selectedCount = updatedWords.count { it.isSelected }

        if (selectedCount == 0) {
            destroyActionMode()
            return
        }

        _uiState.update { it.copy(words = updatedWords, isActionMode = true, selectedCount = selectedCount) }
    }

    fun onActionModeMenuEdit() {
        if (_uiState.value.isActionMode) {
            val selectedWord = _uiState.value.words.find { it.isSelected }
            _clickItemEvent.value = Event(selectedWord?.id)
            destroyActionMode()
        }
    }

    fun onActionModeMenuDelete() {
        if (_uiState.value.isActionMode) {
            val selectedWords = _uiState.value.words.filter { it.isSelected }
            viewModelScope.launch {
                wordRepository.deleteWords(selectedWords.map { it.id })
                destroyActionMode()
            }
        }
    }

    fun onActionModeMenuSelectAll() {
        val updatedWords = _uiState.value.words.map { word -> word.copy(isSelected = true) }
        _uiState.update { it.copy(words = updatedWords, isActionMode = true, selectedCount = updatedWords.size) }
    }

    fun destroyActionMode() {
        _actionModeEvent.value = Event(ActionModeState.STOPPED)
        _uiState.update { it.copy(words = it.words.map { word -> word.copy(isSelected = false) }, isActionMode = false, selectedCount = 0) }
    }
}