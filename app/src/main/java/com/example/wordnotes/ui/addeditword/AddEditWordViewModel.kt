package com.example.wordnotes.ui.addeditword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.Event
import com.example.wordnotes.R
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.onError
import com.example.wordnotes.data.onLoading
import com.example.wordnotes.data.onSuccess
import com.example.wordnotes.data.repositories.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditWordUiState(
    val word: Word = Word(),
    val isLoading: Boolean = false,
    val snackBarMessage: Int? = null
)

class AddEditWordViewModel(
    private val wordsRepository: WordsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState: MutableStateFlow<AddEditWordUiState> = MutableStateFlow(AddEditWordUiState())
    val uiState: StateFlow<AddEditWordUiState> = _uiState.asStateFlow()

    private val _wordUpdatedEvent: MutableLiveData<Event<Unit>> = MutableLiveData<Event<Unit>>()
    val wordUpdatedEvent: LiveData<Event<Unit>> = _wordUpdatedEvent

    private var isForAddingWord = false

    fun initializeWithWordId(wordId: String?) {
        if (wordId == null) isForAddingWord = true
        else loadWord(wordId)
    }

    private fun loadWord(wordId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val savedWord: Word? = savedStateHandle[WORDS_SAVED_STATE_KEY]
            if (savedWord != null) {
                _uiState.update {
                    it.copy(word = savedWord, isLoading = false)
                }
            } else {
                wordsRepository.getWord(wordId).let { result ->
                    result.onSuccess { data ->
                        _uiState.update { it.copy(word = data, isLoading = false) }
                    }
                    result.onError {
                        _uiState.update { it.copy(isLoading = false, snackBarMessage = R.string.error_while_loading_word) }
                    }
                    result.onLoading {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun onUserUpdatesWord(onUpdate: (Word) -> Word) {
        _uiState.update { currentWord ->
            currentWord.copy(word = onUpdate(currentWord.word)).also {
                savedStateHandle[WORDS_SAVED_STATE_KEY] = it.word
            }
        }
    }

    fun saveWord() {
        if (_uiState.value.word.isValid)
            onInputValid()
        else
            // TODO("Make stronger invalidation check")
            _uiState.update { it.copy(snackBarMessage = R.string.word_and_meaning_must_not_be_empty) }
    }

    private val Word.isValid get() = word.isNotEmpty() and meaning.isNotEmpty()

    private fun onInputValid() {
        if (isForAddingWord) {
            createWord(_uiState.value.word)
            _uiState.update { it.copy(snackBarMessage = R.string.add_new_word_successfully) }
        } else {
            updateWord(_uiState.value.word)
            _uiState.update { it.copy(snackBarMessage = R.string.update_word_successfully) }
        }
    }

    private fun createWord(newWord: Word) = viewModelScope.launch {
        wordsRepository.saveWord(newWord.copy(timestamp = System.currentTimeMillis()))
        _wordUpdatedEvent.value = Event(Unit)
    }

    private fun updateWord(word: Word) = viewModelScope.launch {
        if (isForAddingWord) throw IllegalStateException("updateWord(word: Word) was called but word is new")

        wordsRepository.updateWord(word.copy(timestamp = System.currentTimeMillis()))
        _wordUpdatedEvent.value = Event(Unit)
    }

    fun snakeBarShown() {
        _uiState.update { it.copy(snackBarMessage = null) }
    }
}

const val WORDS_SAVED_STATE_KEY = "WORDS_SAVED_STATE_KEY"