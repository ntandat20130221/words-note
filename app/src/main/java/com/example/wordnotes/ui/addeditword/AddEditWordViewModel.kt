package com.example.wordnotes.ui.addeditword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun initializeWithWordId(wordId: String?) {
        if (wordId != null) {
            viewModelScope.launch {
                wordRepository.getWord(wordId).also { _word.value = it }
            }
        }
    }

    fun updateWord(onUpdate: (Word) -> Word) {
        _word.update { currentWord ->
            onUpdate(currentWord)
        }
    }

    fun saveWord() {
        viewModelScope.launch {
            wordRepository.saveWord(word.value)
        }
    }
}