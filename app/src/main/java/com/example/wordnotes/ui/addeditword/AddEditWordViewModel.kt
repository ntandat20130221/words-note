package com.example.wordnotes.ui.addeditword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.R
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditWordViewModel(private val wordRepository: WordRepository) : ViewModel() {
    private val _word: MutableStateFlow<Word> = MutableStateFlow(Word())
    val word: StateFlow<Word> = _word.asStateFlow()

    private val _snackBarMessage: MutableStateFlow<Int> = MutableStateFlow(0)
    val snackBarMessage: StateFlow<Int> = _snackBarMessage.asStateFlow()

    private var isForAddingWord = false

    fun initializeWithWordId(wordId: String?) {
        if (wordId == null) {
            isForAddingWord = true
            return
        }

        viewModelScope.launch {
            wordRepository.getWord(wordId).also { _word.value = it }
        }
    }

    fun onUpdateWord(onUpdate: (Word) -> Word) {
        _word.update { currentWord ->
            onUpdate(currentWord)
        }
    }

    fun saveWord() {
        if (word.value.isValid) {
            if (isForAddingWord) {
                createWord(word.value)
                _snackBarMessage.value = R.string.add_new_word_successfully
            } else {
                updateWord(word.value)
                _snackBarMessage.value = R.string.update_word_successfully
            }
        }
    }

    private val Word.isValid get() = word.isNotEmpty() and meaning.isNotEmpty()

    private fun createWord(newWord: Word) = viewModelScope.launch {
        wordRepository.saveWord(newWord)
    }

    private fun updateWord(word: Word) {
        if (isForAddingWord) throw IllegalStateException("updateWord(word: Word) was called but word is new")

        viewModelScope.launch {
            wordRepository.updateWord(word)
        }
    }
}