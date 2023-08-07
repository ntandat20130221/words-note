package com.example.wordnotes.ui.addeditword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.Event
import com.example.wordnotes.R
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditWordUiState(
    val word: Word = Word(),
    val currentPosIndex: Int = 0,
    val isLoading: Boolean = false,
    val snackBarMessage: Int? = null
)

class AddEditWordViewModel(
    private val wordsRepository: WordsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val englishPartsOfSpeech: Array<String> = arrayOf("Verb", "Noun", "Adj.", "Adv.", "Pro.", "Prep.", "Conj.", "Interj.", "Det.")

    private val _uiState: MutableStateFlow<AddEditWordUiState> = MutableStateFlow(AddEditWordUiState())
    val uiState: StateFlow<AddEditWordUiState> = _uiState.asStateFlow()

    private val _wordSavedEvent: MutableLiveData<Event<Unit>> = MutableLiveData<Event<Unit>>()
    val wordSavedEvent: LiveData<Event<Unit>> = _wordSavedEvent

    private var isForAddingWord = false

    fun initializeWithWordId(wordId: String?) {
        if (wordId == null) isForAddingWord = true
        else if (savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY) != null) updateWithSavedState()
        else loadWord(wordId)
    }

    private fun updateWithSavedState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)?.let { savedWord ->
                _uiState.update {
                    it.copy(
                        word = savedWord,
                        currentPosIndex = savedStateHandle[CURRENT_POS_INDEX_SAVED_STATE_KEY] ?: 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadWord(wordId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            wordsRepository.getWord(wordId).let { result ->
                when (result) {
                    is Result.Success -> {
                        val currentPartOfSpeechIndex = englishPartsOfSpeech.indexOfFirst { it.equals(result.data.pos, ignoreCase = true) }
                        _uiState.update {
                            it.copy(
                                word = result.data,
                                currentPosIndex = currentPartOfSpeechIndex,
                                isLoading = false
                            )
                        }
                        savedStateHandle[WORDS_SAVED_STATE_KEY] = result.data
                        savedStateHandle[CURRENT_POS_INDEX_SAVED_STATE_KEY] = currentPartOfSpeechIndex
                    }

                    is Result.Error -> _uiState.update { it.copy(isLoading = false, snackBarMessage = R.string.error_while_loading_word) }

                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onUpdateWord(onUpdate: (Word) -> Word) {
        _uiState.update { currentWord ->
            currentWord.copy(word = onUpdate(currentWord.word)).also {
                savedStateHandle[WORDS_SAVED_STATE_KEY] = it.word
            }
        }
    }

    private fun Word.isValid() = word.isNotEmpty()

    fun saveWord() {
        if (_uiState.value.word.isValid()) onInputValid()
        else _uiState.update { it.copy(snackBarMessage = R.string.word_must_not_be_empty) }
    }

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
        _wordSavedEvent.value = Event(Unit)
    }

    private fun updateWord(word: Word) = viewModelScope.launch {
        wordsRepository.updateWord(word)
        _wordSavedEvent.value = Event(Unit)
    }

    fun snakeBarShown() {
        _uiState.update { it.copy(snackBarMessage = null) }
    }

    fun onPosItemClicked(selectedIndex: Int) {
        _uiState.update { it.copy(currentPosIndex = selectedIndex) }
        onUpdateWord { it.copy(pos = englishPartsOfSpeech[selectedIndex].lowercase()) }
        savedStateHandle[CURRENT_POS_INDEX_SAVED_STATE_KEY] = selectedIndex
    }
}

const val WORDS_SAVED_STATE_KEY = "WORDS_SAVED_STATE_KEY"
const val CURRENT_POS_INDEX_SAVED_STATE_KEY = "CURRENT_POS_INDEX_SAVED_STATE_KEY"