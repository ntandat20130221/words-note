package com.example.wordnotes.ui.worddetail

import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.mocks.FakeWordRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class WordDetailViewModelTest {
    private lateinit var wordRepository: FakeWordRepository
    private lateinit var wordDetailViewModel: WordDetailViewModel

    private val words = listOf(
        Word(id = "1", word = "word1", pos = "verb", ipa = "ipa1", meaning = "meaning1", isRemind = false),
        Word(id = "2", word = "word2", pos = "noun", ipa = "ipa2", meaning = "meaning2", isRemind = true),
        Word(id = "3", word = "word3", pos = "adj", ipa = "ipa3", meaning = "meaning3", isRemind = true),
    )

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        wordRepository = FakeWordRepository(initialWords = words)
        wordDetailViewModel = WordDetailViewModel(wordRepository)
    }

    @Test
    fun `initialize with invalid word id should not update ui state`() = runTest {
        var uiState = WordDetailUiState()
        wordDetailViewModel.uiState.collectIn(backgroundScope) { uiState = it }
        wordDetailViewModel.initializeWithWordId("4")
        assertThat(uiState.word.word).isEmpty()
    }

    @Test
    fun `initialize with valid word id should update ui state correctly`() = runTest {
        var uiState = WordDetailUiState()
        wordDetailViewModel.uiState.collectIn(backgroundScope) { uiState = it }
        wordDetailViewModel.initializeWithWordId("1")
        assertThat(uiState.word).isEqualTo(words[0])
    }

    @Test
    fun `delete word should update repository`() = runTest {
        wordDetailViewModel.initializeWithWordId("1")
        wordDetailViewModel.deleteWord()
        assertThat(wordDetailViewModel.uiState.value.isDismissed).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(2)
    }

    @Test
    fun `toggle remind should update repository`() = runTest {
        wordDetailViewModel.initializeWithWordId("1")
        wordDetailViewModel.toggleRemind()
        assertThat(wordDetailViewModel.uiState.value.isDismissed).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data.find { it.id == "1" }!!.isRemind).isTrue()
    }

    private fun <T> Flow<T>.collectIn(scope: CoroutineScope, callback: (T) -> Unit) {
        scope.launch(mainCoroutineRule.testDispatcher) {
            this@collectIn.collect {
                callback(it)
            }
        }
    }
}