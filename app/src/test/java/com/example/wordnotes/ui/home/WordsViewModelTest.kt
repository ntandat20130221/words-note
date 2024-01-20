package com.example.wordnotes.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.createEmptyCollector
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
class WordsViewModelTest {
    private lateinit var wordsRepository: FakeWordRepository
    private lateinit var wordsViewModel: WordsViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordRepository()
        wordsViewModel = WordsViewModel(wordsRepository)
    }

    @Test
    fun checkSizeAtInitialState() = runTest {
        assertThat(wordsViewModel.uiState.first().items).hasSize(3)
    }

    @Test
    fun addWordsFromRepository_UiStateUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsRepository.saveWord(Word(id = "4", word = "word4", isRemind = true))
        assertThat(wordsViewModel.uiState.value.items).hasSize(4)
        assertThat(wordsViewModel.uiState.value.items[3].word.id).isEqualTo("4")
    }

    @Test
    fun clickItem_ClickItemEventUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isNull()
    }

    @Test
    fun startActionMode_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.selectedCount).isEqualTo(1)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(1)
        assertThat(uiState.items[0].isSelected).isTrue()
    }

    @Test
    fun clickItemInActionMode_UntilDestroy_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
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
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.destroyActionMode()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_DestroyActionMode_ClickItem_ClickItemEventUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.destroyActionMode()
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
    }

    @Test
    fun startActionMode_ClickAnotherItem_ClickEditMenu_UiStateDoesNotUpdate() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
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
    fun startActionMode_SelectAnyItem_ClickEditMenu_ClickEditMenuEventUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
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
    fun startActionMode_SelectSomeItems_ClickDeleteMenu_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
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
    fun startActionMode_DeleteItems_Undo() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.onActionModeMenuDelete()

        wordsViewModel.undoDeletion()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(3)
    }

    @Test
    fun startActionMode_DeleteItems_DismissUndo() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.onActionModeMenuDelete()

        wordsViewModel.undoDismissed()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(1)
    }

    @Test
    fun startActionMode_SelectSomeItems_ClickRemindMenu_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.itemClicked(wordId = "3")

        wordsViewModel.onActionModeMenuRemind()
        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(3)
        assertThat(uiState.items[0].word.isRemind).isTrue()
        assertThat(uiState.items[1].word.isRemind).isTrue()
        assertThat(uiState.items[2].word.isRemind).isTrue()
    }

    @Test
    fun startActionMode_ClickSelectAllMenu_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)
        wordsViewModel.itemLongClicked(wordId = "1")

        wordsViewModel.onActionModeMenuSelectAll()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.items.filter { it.isSelected }).hasSize(3)
    }

    @Test
    fun startSearching_EmptyResult() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)

        wordsViewModel.startSearching()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isSearching).isTrue()
        assertThat(uiState.searchResult).isEmpty()
    }

    @Test
    fun search_WithQuery_CheckSearchResult() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)

        wordsViewModel.startSearching()
        wordsViewModel.search("w")
        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.searchResult).hasSize(3)

        wordsViewModel.search("word2")
        val uiState2 = wordsViewModel.uiState.value
        assertThat(uiState2.searchResult).hasSize(1)
    }

    @Test
    fun stopSearching_EmptyResult() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, wordsViewModel.uiState)

        wordsViewModel.startSearching()
        wordsViewModel.search("w")
        wordsViewModel.stopSearching()

        val uiState = wordsViewModel.uiState.value
        assertThat(uiState.isSearching).isFalse()
        assertThat(uiState.searchResult).isEmpty()
    }
}