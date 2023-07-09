package com.example.wordnotes.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.onLoading
import com.example.wordnotes.data.onSuccess
import com.example.wordnotes.data.repositories.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WordsViewModel(private val wordRepository: WordRepository) : ViewModel() {
    private val _words: MutableStateFlow<List<Word>> = MutableStateFlow(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    init {
        viewModelScope.launch {
            wordRepository.observeWords().collect { result ->
                result.onSuccess {
                    _words.value = it
                }
                result.onLoading {
                    // TODO("Notify users using SnackBar, etc.")
                }
            }
        }
    }
}