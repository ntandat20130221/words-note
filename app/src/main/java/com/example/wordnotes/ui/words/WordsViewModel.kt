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
    val selectedWordsCount: Int = 0
)

class WordsViewModel(private val wordRepository: WordRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<WordsUiState> = MutableStateFlow(WordsUiState())
    val uiState: StateFlow<WordsUiState> = _uiState.asStateFlow()

    private val _clickItemEvent: MutableLiveData<Event<String>> = MutableLiveData()
    val clickItemEvent: LiveData<Event<String>> = _clickItemEvent

    private val selectedWordIds = mutableListOf<String>()

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

        }
    }

    fun itemLongClicked(wordId: String): Boolean {
        if (!_uiState.value.isActionMode) {
            val updatedWords = _uiState.value.words.map { wordUiState -> wordUiState.copy(isSelected = wordUiState.id == wordId) }
            _uiState.update { it.copy(words = updatedWords, isActionMode = true, selectedWordsCount = it.selectedWordsCount + 1) }
            selectedWordIds.add(wordId)
        }
        return true
    }

    fun destroyActionMode() {
        _uiState.update {
            it.copy(words = it.words.map { wordUiState -> wordUiState.copy(isSelected = false) }, isActionMode = false, selectedWordsCount = 0)
        }
    }
}