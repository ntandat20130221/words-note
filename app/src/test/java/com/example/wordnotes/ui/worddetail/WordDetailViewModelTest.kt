package com.example.wordnotes.ui.worddetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.createEmptyCollector
import com.example.wordnotes.data.Result
import com.example.wordnotes.mocks.FakeWordRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class WordDetailViewModelTest {
    private lateinit var wordsRepository: FakeWordRepository
    private lateinit var wordDetailViewModel: WordDetailViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordRepository()
        wordDetailViewModel = WordDetailViewModel(wordsRepository)
    }

    @Test
    fun initialize_WithErrorFromRepository() = runTest {
        wordsRepository.setShouldThrowError(true)
        wordDetailViewModel.initializeWithWordId("1")

        val uiState = wordDetailViewModel.uiState.first()
        assertThat(uiState.id).isNotEqualTo("1")
    }

    @Test
    fun initialize_WithWrongWordId() = runTest {
        wordDetailViewModel.initializeWithWordId("123")

        val uiState = wordDetailViewModel.uiState.first()
        assertThat(uiState.id).isNotEqualTo("123")
    }

    @Test
    fun initialize_CheckUiState() = runTest {
        wordDetailViewModel.initializeWithWordId("1")
        val uiState = wordDetailViewModel.uiState.first()
        assertThat(uiState.id).isEqualTo("1")
    }

    @Test
    fun deleteWord_RepositoryUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordDetailViewModel.uiState)
        wordDetailViewModel.initializeWithWordId("1")
        wordDetailViewModel.deleteWord()

        val words = (wordsRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(2)
    }

    @Test
    fun remindWord_RepositoryUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordDetailViewModel.uiState)
        wordDetailViewModel.initializeWithWordId("3")
        wordDetailViewModel.toggleRemind()

        val word = (wordsRepository.getWord("3") as Result.Success).data
        assertThat(word.isRemind).isTrue()

        wordDetailViewModel.toggleRemind()
        val word2 = (wordsRepository.getWord("3") as Result.Success).data
        assertThat(word2.isRemind).isFalse()
    }
}