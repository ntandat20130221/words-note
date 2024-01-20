package com.example.wordnotes.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.createEmptyCollector
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.mocks.FakeWordRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {
    private lateinit var wordsRepository: FakeWordRepository
    private lateinit var homeViewModel: HomeViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordsRepository = FakeWordRepository()
        homeViewModel = HomeViewModel(wordsRepository)
    }

    @Test
    fun checkSizeAtInitialState() = runTest {
        assertThat(homeViewModel.uiState.first().items).hasSize(3)
    }

    @Test
    fun addWordsFromRepository_UiStateUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        wordsRepository.saveWord(Word(id = "4", word = "word4", isRemind = true))
        assertThat(homeViewModel.uiState.value.items).hasSize(4)
        assertThat(homeViewModel.uiState.value.items[3].word.id).isEqualTo("4")
    }

    @Test
    fun clickItem_ClickItemEventUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemClicked(wordId = "2")

        assertThat(homeViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
        assertThat(homeViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isNull()
    }

    @Test
    fun startActionMode_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.selectedCount).isEqualTo(1)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(1)
        assertThat(uiState.items[0].isSelected).isTrue()
    }

    @Test
    fun clickItemInActionMode_UntilDestroy_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")

        assertThat(homeViewModel.uiState.value.isActionMode).isTrue()
        assertThat(homeViewModel.uiState.value.selectedCount).isEqualTo(2)

        homeViewModel.itemClicked(wordId = "1")

        assertThat(homeViewModel.uiState.value.selectedCount).isEqualTo(1)

        homeViewModel.itemClicked(wordId = "2")

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_DestroyActionMode_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.destroyActionMode()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_DestroyActionMode_ClickItem_ClickItemEventUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.destroyActionMode()
        homeViewModel.itemClicked(wordId = "2")

        assertThat(homeViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
    }

    @Test
    fun startActionMode_ClickAnotherItem_ClickEditMenu_UiStateDoesNotUpdate() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")

        homeViewModel.onActionModeMenuEdit()
        assertThat(homeViewModel.clickEditItemEvent.value?.getContent()).isNull()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.selectedCount).isEqualTo(2)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(2)
        assertThat(uiState.items[2].isSelected).isFalse()
    }

    @Test
    fun startActionMode_SelectAnyItem_ClickEditMenu_ClickEditMenuEventUpdateCorrectly() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")
        homeViewModel.itemClicked(wordId = "1")

        homeViewModel.onActionModeMenuEdit()
        assertThat(homeViewModel.clickEditItemEvent.value?.getContent()).isEqualTo("2")

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.selectedCount).isEqualTo(0)
        assertThat(uiState.items.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_SelectSomeItems_ClickDeleteMenu_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")
        homeViewModel.itemClicked(wordId = "3")
        homeViewModel.itemClicked(wordId = "1")

        homeViewModel.onActionModeMenuDelete()
        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(1)
        assertThat(uiState.items[0].word.id).isEqualTo("1")
    }

    @Test
    fun startActionMode_DeleteItems_Undo() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")
        homeViewModel.onActionModeMenuDelete()

        homeViewModel.undoDeletion()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(3)
    }

    @Test
    fun startActionMode_DeleteItems_DismissUndo() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")
        homeViewModel.onActionModeMenuDelete()

        homeViewModel.undoDismissed()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(1)
    }

    @Test
    fun startActionMode_SelectSomeItems_ClickRemindMenu_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")
        homeViewModel.itemClicked(wordId = "2")
        homeViewModel.itemClicked(wordId = "3")

        homeViewModel.onActionModeMenuRemind()
        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isFalse()
        assertThat(uiState.items).hasSize(3)
        assertThat(uiState.items[0].word.isRemind).isTrue()
        assertThat(uiState.items[1].word.isRemind).isTrue()
        assertThat(uiState.items[2].word.isRemind).isTrue()
    }

    @Test
    fun startActionMode_ClickSelectAllMenu_CheckUiState() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)
        homeViewModel.itemLongClicked(wordId = "1")

        homeViewModel.onActionModeMenuSelectAll()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isActionMode).isTrue()
        assertThat(uiState.items.filter { it.isSelected }).hasSize(3)
    }

    @Test
    fun startSearching_EmptyResult() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)

        homeViewModel.startSearching()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isSearching).isTrue()
        assertThat(uiState.searchResult).isEmpty()
    }

    @Test
    fun search_WithQuery_CheckSearchResult() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)

        homeViewModel.startSearching()
        homeViewModel.search("w")
        val uiState = homeViewModel.uiState.value
        assertThat(uiState.searchResult).hasSize(3)

        homeViewModel.search("word2")
        val uiState2 = homeViewModel.uiState.value
        assertThat(uiState2.searchResult).hasSize(1)
    }

    @Test
    fun stopSearching_EmptyResult() = runTest {
        createEmptyCollector(backgroundScope, testScheduler, homeViewModel.uiState)

        homeViewModel.startSearching()
        homeViewModel.search("w")
        homeViewModel.stopSearching()

        val uiState = homeViewModel.uiState.value
        assertThat(uiState.isSearching).isFalse()
        assertThat(uiState.searchResult).isEmpty()
    }
}