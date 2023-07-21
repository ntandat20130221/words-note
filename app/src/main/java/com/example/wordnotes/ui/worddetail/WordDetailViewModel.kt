package com.example.wordnotes.ui.worddetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.R
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

data class WordDetailUiState(
    val word: Word? = null,
    val isLoading: Boolean = false,
    val snackBarMessage: Int? = null
)

class WordDetailViewModel(
    private val wordsRepository: WordsRepository
) : ViewModel() {

    private val wordId: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _snackBarMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _wordResult: Flow<Result<Word>> = wordId.flatMapLatest { wordId ->
        if (wordId != null) wordsRepository.observeWord(wordId)
        else flow { emit(Result.Loading) }
    }

    val uiState: StateFlow<WordDetailUiState> = combine(_snackBarMessage, _isLoading, _wordResult) { snackBarMessage, isLoading, wordResult ->
        when (wordResult) {
            is Result.Loading -> WordDetailUiState(
                isLoading = true
            )

            is Result.Error -> WordDetailUiState(
                snackBarMessage = R.string.error_while_loading_word
            )

            is Result.Success -> WordDetailUiState(
                word = wordResult.data,
                isLoading = isLoading,
                snackBarMessage = snackBarMessage
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WordDetailUiState(isLoading = true)
    )

    fun initializeWithWordId(wordId: String) {
        this.wordId.value = wordId
    }
}