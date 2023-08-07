package com.example.wordnotes.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.data.MainCoroutineRule
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.FakeWordsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class WordsViewModelTest {
    private lateinit var wordsRepository: FakeWordsRepository
    private lateinit var wordsViewModel: WordsViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordsRepository().apply {
            addWords(Word(id = "1", word = "word", pos = "adj.", ipa = "ipa", meaning = "meaning", isRemind = true))
            addWords(Word(id = "2", word = "word2", pos = "noun", ipa = "ipa2", meaning = "meaning2", isRemind = true))
            addWords(Word(id = "3", word = "word3", pos = "verb", ipa = "ipa3", meaning = "meaning3"))
        }
        wordsViewModel = WordsViewModel(wordsRepository)
    }

    private fun createEmptyUiStateCollector(scope: CoroutineScope, scheduler: TestCoroutineScheduler) {
        scope.launch(UnconfinedTestDispatcher(scheduler)) { wordsViewModel.uiState.collect {} }
    }

    @Test
    fun checkSizeAtInitialState() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        assertThat(wordsViewModel.uiState.value.items).hasSize(3)
    }

    @Test
    fun addWordsFromRepository_UiStateUpdateCorrectly() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsRepository.saveWord(Word(id = "4", word = "word4", isRemind = true))
        assertThat(wordsViewModel.uiState.value.items).hasSize(4)
        assertThat(wordsViewModel.uiState.value.items[3].word.id).isEqualTo("4")
    }

    @Test
    fun clickItem_ClickItemEventUpdateCorrectly() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isNull()
    }

    @Test
    fun startActionMode_CheckUiState() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.selectedCount).isEqualTo(1)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(1)
        assertThat(uiState.items[0].isSelected).isTrue()
    }

    @Test
    fun clickItemInActionMode_UntilDestroy_CheckUiState() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.uiState.value.isActionMode).isTrue()
        assertThat(wordsViewModel.uiState.value.selectedCount).isEqualTo(2)

        wordsViewModel.itemClicked(wordId = "1")

        assertThat(wordsViewModel.uiState.value.selectedCount).isEqualTo(1)

        wordsViewModel.itemClicked(wordId = "2")

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_DestroyActionMode_CheckUiState() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.destroyActionMode()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_DestroyActionMode_ClickItem_ClickItemEventUpdateCorrectly() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.destroyActionMode()
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
    }

    @Test
    fun startActionMode_ClickAnotherItem_ClickEditMenu_UiStateDoesNotUpdate() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")

        wordsViewModel.onActionModeMenuEdit()
        assertThat(wordsViewModel.clickEditItemEvent.value?.getContent()).isNull()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.selectedCount).isEqualTo(2)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(2)
        assertThat(uiState.items[2].isSelected).isFalse()
    }

    @Test
    fun startActionMode_ClickEditMenu_ClickEditMenuEventUpdateCorrectly() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.itemClicked(wordId = "1")

        wordsViewModel.onActionModeMenuEdit()
        assertThat(wordsViewModel.clickEditItemEvent.value?.getContent()).isEqualTo("2")

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_ClickDeleteMenu_CheckUiState() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.itemClicked(wordId = "3")
        wordsViewModel.itemClicked(wordId = "1")

        wordsViewModel.onActionModeMenuDelete()
        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(1)
        assertThat(uiState.items[0].word.id).isEqualTo("1")
    }

    @Test
    fun startActionMode_ClickSelectAllMenu_CheckUiState() = runTest {
        createEmptyUiStateCollector(backgroundScope, testScheduler)
        wordsViewModel.itemLongClicked(wordId = "1")

        wordsViewModel.onActionModeMenuSelectAll()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.items.filter { it.isSelected }).hasSize(3)
    }
}