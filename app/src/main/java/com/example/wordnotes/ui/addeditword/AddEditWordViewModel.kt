package com.example.wordnotes.ui.addeditword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.R
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

data class AddEditWordUiState(
    val word: Word = Word(),
    val currentPosIndex: Int = 0,
    val isLoading: Boolean = false,
    val snackBarMessage: Int? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddEditWordViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val englishPartsOfSpeech: Array<String> = arrayOf("Verb", "Noun", "Adj.", "Adv.", "Pro.", "Prep.", "Conj.", "Interj.", "Det.")

    private val _uiState: MutableStateFlow<AddEditWordUiState> = MutableStateFlow(AddEditWordUiState(isLoading = true))
    val uiState: StateFlow<AddEditWordUiState> = _uiState.asStateFlow()

    private var isForAddingWord = false

    fun initializeWithWordId(wordId: String?) {
        if (savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY) != null) updateFromSavedState()
        else if (wordId == null) prepareForAddingWord()
        else loadWord(wordId)
    }

    private fun prepareForAddingWord() {
        isForAddingWord = true
        _uiState.update {
            it.copy(
                word = it.word.copy(pos = englishPartsOfSpeech[0].lowercase(), isRemind = true),
                isLoading = false
            )
        }
    }

    private fun updateFromSavedState() {
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

    private fun loadWord(wordId: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        wordRepository.getWord(wordId).let { result ->
            when (result) {
                is Result.Success -> {
                    val currentPartOfSpeechIndex = englishPartsOfSpeech.indexOfFirst { it.equals(result.data.pos, ignoreCase = true) }
                    _uiState.update {
                        it.copy(
                            word = transformWordBeforeLoad(result.data),
                            currentPosIndex = currentPartOfSpeechIndex,
                            isLoading = false
                        )
                    }
                    savedStateHandle[WORDS_SAVED_STATE_KEY] = result.data
                    savedStateHandle[CURRENT_POS_INDEX_SAVED_STATE_KEY] = currentPartOfSpeechIndex
                }

                is Result.Error -> _uiState.update { it.copy(isLoading = false, snackBarMessage = R.string.error_while_loading_word) }
            }
        }
    }

    fun onUpdateWord(onUpdate: (Word) -> Word) = _uiState.update { currentWord ->
        currentWord.copy(word = onUpdate(currentWord.word)).also {
            savedStateHandle[WORDS_SAVED_STATE_KEY] = it.word
        }
    }

    private fun Word.isValid() = word.isNotEmpty() and word.isNotBlank()

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
        wordRepository.saveWords(listOf(transformWordBeforeSave(newWord).copy(timestamp = System.currentTimeMillis())))
        _uiState.update { it.copy(isSaved = true) }
    }

    private fun updateWord(word: Word) = viewModelScope.launch {
        wordRepository.updateWords(listOf(transformWordBeforeSave(word)))
        _uiState.update { it.copy(isSaved = true) }
    }

    private fun transformWordBeforeLoad(word: Word) = word.copy(ipa = if (word.ipa.isBlank()) "" else word.ipa.trim('/', ' '))

    private fun transformWordBeforeSave(word: Word): Word {
        return word.copy(
            ipa = if (word.ipa.isBlank()) "" else word.ipa.trim('/', ' ').let { "/$it/" },
            meaning = word.meaning.ifBlank { "" }
        )
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