package com.example.wordnotes.ui.worddetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.data.MainCoroutineRule
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.createEmptyCollector
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.FakeWordsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class WordDetailViewModelTest {
    private lateinit var wordsRepository: FakeWordsRepository
    private lateinit var wordDetailViewModel: WordDetailViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordsRepository().apply {
            addWords(Word(id = "1", word = "word", pos = "noun", ipa = "ipa", meaning = "meaning", isRemind = true))
            addWords(Word(id = "2", word = "word2", pos = "prep.", ipa = "ipa2", meaning = "meaning2", isRemind = true))
            addWords(Word(id = "3", word = "word3", pos = "", ipa = "ipa3", meaning = "meaning3"))
        }
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