package com.example.wordnotes.ui.addeditword

import androidx.lifecycle.SavedStateHandle
import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.R
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.mocks.FakeWordRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddEditWordViewModelTest {
    private lateinit var wordRepository: FakeWordRepository
    private lateinit var addEditWordViewModel: AddEditWordViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private val words = listOf(
        Word(id = "1", word = "word1", pos = "verb", ipa = "ipa1", meaning = "meaning1", isRemind = false),
        Word(id = "2", word = "word2", pos = "noun", ipa = "ipa2", meaning = "meaning2", isRemind = true),
        Word(id = "3", word = "word3", pos = "noun", ipa = "", meaning = "meaning3", isRemind = true),
        Word(id = "4", word = "word4", pos = "adj.", ipa = " /ipa4 /", meaning = "meaning4", isRemind = true),
    )

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        wordRepository = FakeWordRepository(initialWords = words)
        savedStateHandle = SavedStateHandle()
        addEditWordViewModel = AddEditWordViewModel(wordRepository, savedStateHandle)
    }

    @Test
    fun `initialize editing with empty ipa word should successful`() = runTest {
        addEditWordViewModel.initializeWithWordId("3")
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isNull()
        assertThat(uiState.word).isEqualTo(words[2])
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)).isEqualTo(words[2])
        assertThat(savedStateHandle.get<Word>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(1)
    }

    @Test
    fun `initialize editing with bad ipa word should successful`() = runTest {
        addEditWordViewModel.initializeWithWordId("4")
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isNull()
        assertThat(uiState.word).isEqualTo(words[3].copy(ipa = "ipa4"))
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)).isEqualTo(words[3])
        assertThat(savedStateHandle.get<Word>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(2)
    }

    @Test
    fun `initialize editing word with error from repository then ui state should show error`() = runTest {
        wordRepository.setShouldThrowError(true)
        addEditWordViewModel.initializeWithWordId("1")
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isEqualTo(R.string.error_while_loading_word)
        assertThat(uiState.word.id).isNotEqualTo("1")
    }

    @Test
    fun `initialize editing word with error from repository and saved state should successful`() = runTest {
        wordRepository.setShouldThrowError(true)
        savedStateHandle[WORDS_SAVED_STATE_KEY] = words[0]
        addEditWordViewModel.initializeWithWordId("1")
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isNull()
        assertThat(uiState.word).isEqualTo(words[0])
    }

    @Test
    fun `initialize adding word with error from repository should successful`() = runTest {
        wordRepository.setShouldThrowError(true)
        val initialWord = addEditWordViewModel.uiState.value.word
        addEditWordViewModel.initializeWithWordId(null)
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.word).isEqualTo(initialWord.copy(pos = "verb"))
    }

    @Test
    fun `initialize editing word then updates should persist in saved instance state`() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUpdateWord { it.copy(word = "updated word", ipa = "updated ipa", meaning = "updated meaning") }
        addEditWordViewModel.onPosItemClicked(1)
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY))
            .isEqualTo(words[0].copy(word = "updated word", ipa = "updated ipa", pos = "noun", meaning = "updated meaning"))
        assertThat(savedStateHandle.get<Word>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(1)
    }

    @Test
    fun `initialize adding word then updates should persist in saved instance state`() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        val initialWord = addEditWordViewModel.uiState.value.word
        addEditWordViewModel.onUpdateWord { it.copy(word = "new word", ipa = "new ipa", meaning = "new meaning") }
        addEditWordViewModel.onPosItemClicked(1)
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY))
            .isEqualTo(initialWord.copy(word = "new word", ipa = "new ipa", pos = "noun", meaning = "new meaning"))
        assertThat(savedStateHandle.get<Word>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(1)
    }

    @Test
    fun `edit with empty word should show error`() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUpdateWord { it.copy(word = "") }
        addEditWordViewModel.saveWord()
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isEqualTo(R.string.word_must_not_be_empty)
        assertThat((wordRepository.getWords() as Result.Success).data.find { it.id == "1" }).isEqualTo(words[0])
    }

    @Test
    fun `save word with empty word should show error`() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        addEditWordViewModel.saveWord()
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isEqualTo(R.string.word_must_not_be_empty)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(4)
    }

    @Test
    fun `edit with valid word should successful`() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUpdateWord { it.copy(word = "updated word", ipa = " / ipa /  ") }
        addEditWordViewModel.onPosItemClicked(1)
        addEditWordViewModel.saveWord()
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isEqualTo(R.string.update_word_successfully)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(4)
        val updatedWord = (wordRepository.getWords() as Result.Success).data.find { it.id == "1" }
        assertThat(updatedWord).isEqualTo(words[0].copy(word = "updated word", ipa = "/ipa/", pos = "noun"))
    }

    @Test
    fun `save word with valid word should successful`() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        val initialWord = addEditWordViewModel.uiState.value.word
        addEditWordViewModel.onUpdateWord { it.copy(word = "new word", ipa = " / ipa /  ") }
        addEditWordViewModel.onPosItemClicked(1)
        addEditWordViewModel.saveWord()
        val uiState = addEditWordViewModel.uiState.value
        assertThat(uiState.snackBarMessage).isEqualTo(R.string.add_new_word_successfully)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(5)
        val createdWord = (wordRepository.getWords() as Result.Success).data.find { it.id == initialWord.id }
        assertThat(createdWord).isEqualTo(initialWord.copy(word = "new word", ipa = "/ipa/", pos = "noun", timestamp = createdWord!!.timestamp))
    }
}