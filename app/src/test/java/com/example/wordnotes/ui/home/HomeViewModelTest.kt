package com.example.wordnotes.ui.home

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
class HomeViewModelTest {
    private lateinit var wordRepository: FakeWordRepository
    private lateinit var homeViewModel: HomeViewModel

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
        homeViewModel = HomeViewModel(wordRepository)
    }

    @Test
    fun `test initial words ui state`() = runTest {
        var wordsUiState = WordsUiState()
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }
        assertThat(wordsUiState.wordItems).hasSize(3)
        assertThat(wordsUiState.isShowEmptyScreen).isFalse()
        assertThat(wordsUiState.isLoading).isFalse()
    }

    @Test
    fun `update word repository then ui state should update correctly`() = runTest {
        var wordsUiState = WordsUiState()
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        wordRepository.addWords(Word(id = "4", word = "new word", ipa = "ipa"))
        assertThat(wordsUiState.wordItems).hasSize(4)

        wordRepository.deleteWords(listOf("1", "3"))
        assertThat(wordsUiState.wordItems).hasSize(2)

        val updatedWord = Word(id = "2", word = "updated", pos = "prep", ipa = "ipa", meaning = "meaning", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))
        assertThat(wordsUiState.wordItems.find { it.word.id == "2" }!!.word).isEqualTo(updatedWord)

        wordRepository.deleteWords(listOf("2", "4"))
        assertThat(wordsUiState.isShowEmptyScreen).isTrue()
    }

    @Test
    fun `test initial action mode ui state`() = runTest {
        var actionModeUiState = ActionModeUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedCount).isEqualTo(0)
    }

    @Test
    fun `click any item should not start action mode`() = runTest {
        var actionModeUiState = ActionModeUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.itemClicked("1")
        assertThat(actionModeUiState.isActionMode).isFalse()
    }

    @Test
    fun `start action mode then ui state should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedCount).isEqualTo(1)
        assertThat(wordsUiState.wordItems.find { it.word.id == "1" }?.isSelected).isTrue()
    }

    @Test
    fun `start action mode then click items then ui state should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("2")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedCount).isEqualTo(2)
        assertThat(wordsUiState.wordItems.filter { it.isSelected }).hasSize(2)

        homeViewModel.itemLongClicked("2")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedCount).isEqualTo(1)
        assertThat(wordsUiState.wordItems.filter { it.isSelected }).hasSize(1)
    }

    @Test
    fun `start action mode then click items until stop action mode then ui state should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("2")
        homeViewModel.itemClicked("3")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedCount).isEqualTo(3)

        homeViewModel.itemClicked("1")
        homeViewModel.itemClicked("2")
        homeViewModel.itemClicked("3")
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedCount).isEqualTo(0)
        assertThat(wordsUiState.wordItems.any { it.isSelected }).isFalse()
    }

    @Test
    fun `start action mode then click items then stop action mode then ui state should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("2")
        homeViewModel.destroyActionMode()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.any { it.isSelected }).isFalse()
    }

    @Test
    fun `start action mode then update repository then ui should update correctly`() = runTest {
        var wordsUiState = WordsUiState()
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        val updatedWord = Word(id = "2", word = "updated", pos = "prep", ipa = "ipa", meaning = "meaning", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))
        assertThat(wordsUiState.wordItems.find { it.word.id == "2" }?.word).isEqualTo(updatedWord)
    }

    @Test
    fun `start action mode then click edit menu should stop action mode`() = runTest {
        var actionModeUiState = ActionModeUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.itemLongClicked("1")
        homeViewModel.onActionModeMenuEdit()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedCount).isEqualTo(0)
    }

    @Test
    fun `click delete menu in action mode then ui state and repository should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("2")
        homeViewModel.onActionModeMenuDelete()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems).hasSize(1)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(3)
    }

    @Test
    fun `click delete menu in action and dismiss undoing then ui state and repository should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onUndoDismissed()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems).hasSize(1)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(1)
    }

    @Test
    fun `click delete menu in action and undo deletion then ui state and repository should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.undoDeletion()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems).hasSize(3)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(3)
    }

    @Test
    fun `click select all menu in action mode then ui state should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.onActionModeMenuSelectAll()

        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(wordsUiState.wordItems.all { it.isSelected }).isTrue()
    }

    @Test
    fun `click remind menu in action mode then ui state and repository should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("3")
        homeViewModel.onActionModeMenuRemind()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.all { it.word.isRemind }).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data.all { it.isRemind }).isTrue()
    }

    @Test
    fun `start searching then ui state should update correctly`() = runTest {
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEmpty()
        assertThat(searchUiState.searchResult).isEmpty()
    }

    @Test
    fun `search items then ui state should update correctly`() = runTest {
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("wo")
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("wo")
        assertThat(searchUiState.searchResult).hasSize(3)

        homeViewModel.search("word1")
        assertThat(searchUiState.searchQuery).isEqualTo("word1")
        assertThat(searchUiState.searchResult).hasSize(1)

        homeViewModel.search("word4")
        assertThat(searchUiState.searchQuery).isEqualTo("word4")
        assertThat(searchUiState.searchResult).hasSize(0)
    }

    @Test
    fun `search items then stop searching then ui state should update correctly`() = runTest {
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("word1")
        homeViewModel.stopSearching()
        assertThat(searchUiState.isSearching).isFalse()
        assertThat(searchUiState.searchQuery).isEmpty()
        assertThat(searchUiState.searchResult).isEmpty()
    }

    @Test
    fun `search items then update repository then ui state should update correctly`() = runTest {
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        val updatedWord = Word(id = "2", word = "updated", pos = "prep", ipa = "ipa", meaning = "meaning", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))
        assertThat(searchUiState.searchResult).hasSize(2)
    }

    @Test
    fun `search items then long click on an item should start action mode`() = runTest {
        var actionModeUiState = ActionModeUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")
        homeViewModel.itemLongClicked("1")
        assertThat(actionModeUiState.isActionMode).isTrue()
    }

    @Test
    fun `search items then start action mode then stop action mode then ui state should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        homeViewModel.itemLongClicked("1")
        homeViewModel.destroyActionMode()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("word")
        assertThat(searchUiState.searchResult).hasSize(3)
    }

    @Test
    fun `search items then click remind menu in action mode then ui state and repository should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        homeViewModel.itemLongClicked("1")
        homeViewModel.itemClicked("3")
        homeViewModel.onActionModeMenuRemind()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(searchUiState.searchResult).hasSize(3)
        assertThat((wordRepository.getWords() as Result.Success).data.all { it.isRemind }).isTrue()
    }

    @Test
    fun `search items then click delete menu in action mode then ui state and repository should update correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        homeViewModel.itemLongClicked("1")
        homeViewModel.onActionModeMenuDelete()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(searchUiState.searchResult).hasSize(2)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(3)

        homeViewModel.undoDeletion()
        assertThat(searchUiState.searchResult).hasSize(3)

        homeViewModel.itemLongClicked("1")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onUndoDismissed()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(searchUiState.searchResult).hasSize(2)
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(2)
    }

    private fun <T> Flow<T>.collectIn(scope: CoroutineScope, callback: (T) -> Unit) {
        scope.launch(mainCoroutineRule.testDispatcher) {
            this@collectIn.collect {
                callback(it)
            }
        }
    }
}