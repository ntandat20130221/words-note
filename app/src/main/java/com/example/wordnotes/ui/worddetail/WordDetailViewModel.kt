package com.example.wordnotes.ui.worddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.Event
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordDetailViewModel(
    private val wordsRepository: WordsRepository
) : ViewModel() {

    private val _wordId: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _dismissEvent: MutableLiveData<Event<Unit>> = MutableLiveData<Event<Unit>>()
    val dismissEvent: LiveData<Event<Unit>> = _dismissEvent

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<Word> = _wordId.flatMapLatest { wordId ->
        if (wordId != null) {
            wordsRepository.getWordStream(wordId).map { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Error -> Word()
                    is Result.Loading -> Word()
                }
            }
        } else {
            flow { emit(Word()) }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Word()
        )

    /**
     * [wordId] - given by `NavArgs` which persists across configuration change and process death.
     * So no need a `savedStateHandle`.
     */
    fun initializeWithWordId(wordId: String) {
        _wordId.value = wordId
    }

    fun deleteWord() = viewModelScope.launch {
        wordsRepository.deleteWords(listOf(uiState.value.id))
        _dismissEvent.value = Event(Unit)
    }

    fun toggleRemind() = viewModelScope.launch {
        wordsRepository.updateWords(listOf(uiState.value.copy(isRemind = !uiState.value.isRemind)))
        _dismissEvent.value = Event(Unit)
    }
}