package com.example.wordnotes.ui.addeditword

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.wordnotes.R
import com.example.wordnotes.data.MainCoroutineRule
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.onSuccess
import com.example.wordnotes.data.repositories.FakeWordsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddEditWordViewModelTest {
    private lateinit var addEditWordViewModel: AddEditWordViewModel
    private lateinit var wordsRepository: FakeWordsRepository

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordsRepository().apply {
            addWords(Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isLearning = true))
            addWords(Word(id = "2", word = "word2", pos = "pos2", ipa = "ipa2", meaning = "meaning2", isLearning = true))
            addWords(Word(id = "3", word = "word3", pos = "pos3", ipa = "ipa3", meaning = "meaning3"))
        }
        addEditWordViewModel = AddEditWordViewModel(wordsRepository, SavedStateHandle())
    }

    @Test
    fun initializeWord() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        val uiState = addEditWordViewModel.uiState.first()
        assertThat(uiState.word.id).isEqualTo("1")
    }

    @Test
    fun initializeWithNullValue() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        val uiState = addEditWordViewModel.uiState.first()
        assertThat(uiState.word.id).isNotIn(listOf("1", "2", "3"))
    }

    @Test
    fun updateWord() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUserUpdatesWord { word -> word.copy(word = "word2") }

        val uiState = addEditWordViewModel.uiState.first()
        assertThat(uiState.word.id).isEqualTo("1")
        assertThat(uiState.word.word).isEqualTo("word2")
    }

    @Test
    fun updateWord_ThenSaveWord() = runTest {
        addEditWordViewModel.initializeWithWordId("1")
        addEditWordViewModel.onUserUpdatesWord { word -> word.copy(word = "word2") }
        addEditWordViewModel.saveWord()

        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.update_word_successfully)
        val result = wordsRepository.getWord("1")
        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data.word).isEqualTo("word2")
        }
    }

    @Test
    fun initializeWithNullValue_UpdateWord_ThenSaveWord() = runTest {
        addEditWordViewModel.initializeWithWordId(null)
        addEditWordViewModel.onUserUpdatesWord { word -> word.copy(word = "word2", meaning = "meaning2") }
        addEditWordViewModel.saveWord()

        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.add_new_word_successfully)
        val result = wordsRepository.getWord(addEditWordViewModel.uiState.value.word.id)
        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data.word).isEqualTo("word2")
            assertThat(data.meaning).isEqualTo("meaning2")
        }
    }

    @Test
    fun updateWord_WithInvalidInput_ThenSaveWord() = runTest {
        addEditWordViewModel.initializeWithWordId("1")

        addEditWordViewModel.onUserUpdatesWord { word -> word.copy(word = "word2", meaning = "") }
        addEditWordViewModel.saveWord()
        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.word_and_meaning_must_not_be_empty)

        addEditWordViewModel.onUserUpdatesWord { word -> word.copy(word = "word2", meaning = "meaning2") }
        addEditWordViewModel.saveWord()
        assertThat(addEditWordViewModel.uiState.value.snackBarMessage).isEqualTo(R.string.update_word_successfully)
    }
}