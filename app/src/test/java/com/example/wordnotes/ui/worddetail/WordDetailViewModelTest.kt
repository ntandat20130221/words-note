package com.example.wordnotes.ui.worddetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.data.MainCoroutineRule
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.FakeWordsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private fun createEmptyUiStateCollector(scope: CoroutineScope, scheduler: TestCoroutineScheduler) {
        scope.launch(UnconfinedTestDispatcher(scheduler)) { wordDetailViewModel.uiState.collect {} }
    }

    @Test
    fun initialize_CheckUiState() = runTest {
        wordDetailViewModel.initializeWithWordId("1")
        val uiState = wordDetailViewModel.uiState.first()
        assertThat(uiState.id).isEqualTo("1")
    }

    @Test
    fun deleteWord_RepositoryUpdateCorrectly() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordDetailViewModel.initializeWithWordId("1")
        wordDetailViewModel.deleteWord()

        val words = (wordsRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(2)
    }

    @Test
    fun remindWord_RepositoryUpdateCorrectly() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordDetailViewModel.initializeWithWordId("3")
        wordDetailViewModel.remindWord()

        val word = (wordsRepository.getWord("3") as Result.Success).data
        assertThat(word.isRemind).isTrue()
    }
}