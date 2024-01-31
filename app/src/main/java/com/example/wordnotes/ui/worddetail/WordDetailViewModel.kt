package com.example.wordnotes.ui.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordDetailUiState(
    val word: Word = Word(),
    val isDismissed: Boolean = false
)

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<WordDetailUiState> = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    fun initializeWithWordId(wordId: String) = viewModelScope.launch {
        wordRepository.getWordFlow(wordId).collect {
            _uiState.update { uiState -> uiState.copy(word = it) }
        }
    }

    fun deleteWord() = viewModelScope.launch {
        wordRepository.deleteWords(listOf(uiState.value.word.id))
        _uiState.update { it.copy(isDismissed = true) }
    }

    fun toggleRemind() = viewModelScope.launch {
        wordRepository.updateWords(listOf(uiState.value.word.copy(isRemind = !uiState.value.word.isRemind)))
        _uiState.update { it.copy(isDismissed = true) }
    }
}