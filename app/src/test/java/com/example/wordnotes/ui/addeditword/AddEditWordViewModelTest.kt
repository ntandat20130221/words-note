package com.example.wordnotes.ui.addeditword

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.R
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.fakes.FakeWordRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddEditWordViewModelTest {
    private lateinit var wordsRepository: FakeWordRepository
    private lateinit var addEditWordViewModel: AddEditWordViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordRepository()
        savedStateHandle = SavedStateHandle()
        addEditWordViewModel = AddEditWordViewModel(wordsRepository, savedStateHandle)
    }

    @Test
    fun initialize_WithErrorFromRepository() = runTest {
        wordsRepository.setShouldThrowError(true)
        addEditWordViewModel.initializeWithWordId("1")
        val uiState = addEditWordViewModel.uiState.first()

        assertThat(uiState.snackBarMessage).isEqualTo(R.string.error_while_loading_word)
        assertThat(uiState.word.id).isNotEqualTo("1")
    }

    @Test
    fun initialize_withWrongWordId() = runTest {
        addEditWordViewModel.initializeWithWordId("123")
        val uiState = addEditWordViewModel.uiState.first()

        assertThat(uiState.snackBarMessage).isEqualTo(R.string.error_while_loading_word)
        assertThat(uiState.word.id).isNotEqualTo("123")
    }

    @Test
    fun initializeWord_SavedStateHandleValueSetCorrectly() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        val uiState = addEditWordViewModel.uiState.first()

        assertThat(uiState.word.id).isEqualTo("1")
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)?.id).isEqualTo("1")
        assertThat(savedStateHandle.get<Int>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(1)
    }

    @Test
    fun initializeWordWithEmptyPos_CurrentPosIndexEqualsNegativeOne() = runTest {
        addEditWordViewModel.initializeWithWordId("3")
        assertThat(savedStateHandle.get<Int>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(-1)
    }

    @Test
    fun initializeWithNullValue_SavedStateHandleValueIsNull() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        val uiState = addEditWordViewModel.uiState.first()

        assertThat(uiState.word.id).isNotIn(listOf("1", "2", "3"))
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)).isNull()
        assertThat(savedStateHandle.get<Int>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isNull()
    }

    @Test
    fun updateWord() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUpdateWord { word -> word.copy(word = "word2", isRemind = false) }

        val uiState = addEditWordViewModel.uiState.first()
        assertThat(uiState.word.id).isEqualTo("1")
        assertThat(uiState.word.word).isEqualTo("word2")
        assertThat(uiState.word.isRemind).isFalse()
    }

    @Test
    fun updateWord_SavedStateHandleValueSetCorrectly() {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUpdateWord { word -> word.copy(word = "word2") }

        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)?.id).isEqualTo("1")
        assertThat(savedStateHandle.get<Word>(WORDS_SAVED_STATE_KEY)?.word).isEqualTo("word2")

        addEditWordViewModel.onPosItemClicked(3)
        assertThat(savedStateHandle.get<Int>(CURRENT_POS_INDEX_SAVED_STATE_KEY)).isEqualTo(3)
    }

    @Test
    fun updateWord_SaveWord_RepositoryUpdateCorrectly() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUpdateWord { word -> word.copy(word = "word2") }
        addEditWordViewModel.saveWord()

        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.update_word_successfully)
        val word = (wordsRepository.getWord("1") as Result.Success).data
        assertThat(word.word).isEqualTo("word2")
    }

    @Test
    fun initializeWithNullValue_UpdateWord_SaveWord_RepositoryUpdateCorrectly() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        addEditWordViewModel.onUpdateWord { word -> word.copy(word = "word2", meaning = "meaning2") }
        addEditWordViewModel.saveWord()

        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.add_new_word_successfully)
        val word = (wordsRepository.getWord(addEditWordViewModel.uiState.value.word.id) as Result.Success).data
        assertThat(word.word).isEqualTo("word2")
        assertThat(word.meaning).isEqualTo("meaning2")
    }

    @Test
    fun updateWordWithInvalidInput_SaveWord() = runTest {
        addEditWordViewModel.initializeWithWordId("1")

        addEditWordViewModel.onUpdateWord { word -> word.copy(word = "") }
        addEditWordViewModel.saveWord()
        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.word_must_not_be_empty)

        addEditWordViewModel.onUpdateWord { word -> word.copy(word = "word2") }
        addEditWordViewModel.saveWord()
        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.update_word_successfully)
    }
}