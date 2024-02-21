package com.example.wordnotes.ui.learning

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_WORDS = "KEY_WORDS"
private const val KEY_POSITION = "KEY_POSITION"
private const val KEY_OK_POSITIONS = "KEY_OK_POSITIONS"

data class FlashCardUiState(
    val words: List<Word> = emptyList(),
    val cardPosition: Int = 0,
    val okPositions: Set<Int> = emptySet()
)

@HiltViewModel
class FlashCardViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState: MutableStateFlow<FlashCardUiState> = MutableStateFlow(FlashCardUiState())
    val uiState: StateFlow<FlashCardUiState> = _uiState.asStateFlow()

    init {
        if (savedStateHandle.get<List<Word>>(KEY_WORDS) != null) {
            loadFromSavedStateHandle()
        } else {
            loadWordsFromRepository()
        }
    }

    private fun loadFromSavedStateHandle() {
        val words = savedStateHandle.get<List<Word>>(KEY_WORDS) ?: emptyList()
        val cardPosition = savedStateHandle.get<Int>(KEY_POSITION) ?: 0
        val okPositions = savedStateHandle.get<List<Int>>(KEY_OK_POSITIONS) ?: emptyList()
        _uiState.update {
            it.copy(
                words = words,
                cardPosition = cardPosition,
                okPositions = okPositions.toSet()
            )
        }
    }

    private fun loadWordsFromRepository() = viewModelScope.launch {
        val result = wordRepository.getRemindingWords()
        if (result is Result.Success) {
            _uiState.update {
                val data = result.data.shuffled()
                it.copy(words = data).also {
                    savedStateHandle[KEY_WORDS] = data
                }
            }
        }
    }

    fun setCurrentPage(position: Int) {
        _uiState.update { it.copy(cardPosition = position) }
        savedStateHandle[KEY_POSITION] = position
    }

    fun moveToNext() {
        setCurrentPage((uiState.value.cardPosition + 1) % uiState.value.words.size)
    }

    fun moveToPrevious() {
        setCurrentPage((uiState.value.words.size + uiState.value.cardPosition - 1) % uiState.value.words.size)
    }

    fun onOkButtonClicked() {
        _uiState.update { currentState ->
            val set = currentState.okPositions.toMutableSet()
            if (set.add(currentState.cardPosition).not()) {
                set.remove(currentState.cardPosition)
            }
            currentState.copy(okPositions = set.toSet()).also {
                savedStateHandle[KEY_OK_POSITIONS] = set.toList()
            }
        }
    }
}