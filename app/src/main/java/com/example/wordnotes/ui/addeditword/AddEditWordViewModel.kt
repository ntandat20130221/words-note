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

class AddEditWordViewModel(
    private val wordsRepository: WordsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _word = MutableStateFlow(Word())
    val word: StateFlow<Word> = _word.asStateFlow()

    private val _snackBarMessage = MutableStateFlow(0)
    val snackBarMessage: StateFlow<Int> = _snackBarMessage.asStateFlow()

    private val _taskUpdatedEvent = MutableLiveData<Event<Unit>>()
    val taskUpdatedEvent: LiveData<Event<Unit>> = _taskUpdatedEvent

    private var isForAddingWord = false

    fun initializeWithWordId(wordId: String?) {
        if (wordId == null)
            prepareForAddingNewWord()
        else
            prepareForLoadingWord(wordId)
    }

    private fun prepareForAddingNewWord() {
        isForAddingWord = true
    }

    private fun prepareForLoadingWord(wordId: String) {
        viewModelScope.launch {
            val savedWord: Word? = savedStateHandle[WORDS_SAVED_STATE_KEY]
            if (savedWord != null) {
                _word.value = savedWord
            } else {
                val result = wordsRepository.getWord(wordId)
                result.onSuccess { data ->
                    _word.value = data
                }
                result.onError {
                    // TODO("Show error message")
                }
                result.onLoading {
                    // TODO("Show ProgressBar")
                }
            }
        }
    }

    fun onUserUpdatesWord(onUpdate: (Word) -> Word) {
        _word.update { currentWord ->
            onUpdate(currentWord).also {
                savedStateHandle[WORDS_SAVED_STATE_KEY] = it
            }
        }
    }

    fun saveWord() {
        if (word.value.isValid)
            onInputValid()
        else
            _snackBarMessage.value = R.string.word_and_meaning_must_not_be_empty
    }

    private val Word.isValid get() = word.isNotEmpty() and meaning.isNotEmpty()

    private fun onInputValid() {
        if (isForAddingWord) {
            createWord(word.value)
            _snackBarMessage.value = R.string.add_new_word_successfully
        } else {
            updateWord(word.value)
            _snackBarMessage.value = R.string.update_word_successfully
        }
    }

    private fun createWord(newWord: Word) = viewModelScope.launch {
        wordsRepository.saveWord(newWord.copy(timestamp = System.currentTimeMillis()))
        _taskUpdatedEvent.value = Event(Unit)
    }

    private fun updateWord(word: Word) = viewModelScope.launch {
        if (isForAddingWord) throw IllegalStateException("updateWord(word: Word) was called but word is new")

        wordsRepository.updateWord(word.copy(timestamp = System.currentTimeMillis()))
        _taskUpdatedEvent.value = Event(Unit)
    }
}

const val WORDS_SAVED_STATE_KEY = "WORDS_SAVED_STATE_KEY"