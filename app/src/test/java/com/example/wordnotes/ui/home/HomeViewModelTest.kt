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
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactlyElementsIn(words)
        assertThat(wordsUiState.isShowEmptyScreen).isFalse()
        assertThat(wordsUiState.isLoading).isFalse()
    }

    @Test
    fun `update repository should update ui state correctly`() = runTest {
        var wordsUiState = WordsUiState()
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        val newWord = Word(id = "4", word = "new word", ipa = "ipa")
        wordRepository.addWords(newWord)
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactlyElementsIn(arrayOf(newWord, *words.toTypedArray()))

        wordRepository.deleteWords(listOf("1", "3"))
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[1], newWord)

        val updatedWord = Word(id = "2", word = "updated", pos = "prep", ipa = "ipa", meaning = "meaning", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(updatedWord, newWord)

        wordRepository.deleteWords(listOf("2", "4"))
        assertThat(wordsUiState.wordItems).isEmpty()
        assertThat(wordsUiState.isShowEmptyScreen).isTrue()
    }

    @Test
    fun `test initial action mode ui state`() = runTest {
        var actionModeUiState = ActionModeUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
    }

    @Test
    fun `select same items should remove duplicates`() = runTest {
        var actionModeUiState = ActionModeUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.selectItem("1")
        homeViewModel.selectItem("2")
        homeViewModel.selectItem("1")
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).containsExactly("2")
    }

    @Test
    fun `start action mode should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1")
        assertThat(wordsUiState.wordItems.single { it.isSelected }.word).isEqualTo(words[0])
    }

    @Test
    fun `select items using long click should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.onItemLongClicked("2")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1", "2")
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactlyElementsIn(words)
        assertThat(wordsUiState.wordItems.filter { it.isSelected }.map { it.word }).containsExactly(words[0], words[1])
    }

    @Test
    fun `click last item should stop action mode`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("1")
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactlyElementsIn(words)
        assertThat(wordsUiState.wordItems.any { it.isSelected }).isFalse()
    }

    @Test
    fun `select items in action mode should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1", "2")
        assertThat(wordsUiState.wordItems.filter { it.isSelected }.map { it.word }).containsExactly(words[0], words[1])

        homeViewModel.onItemLongClicked("2")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1")
        assertThat(wordsUiState.wordItems.single { it.isSelected }.word).isEqualTo(words[0])
    }

    @Test
    fun `start action mode then click items until stop should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        homeViewModel.selectItem("1")
        homeViewModel.selectItem("2")
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(wordsUiState.wordItems.any { it.isSelected }).isFalse()
    }

    @Test
    fun `start and stop action mode should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        homeViewModel.destroyActionMode()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.any { it.isSelected }).isFalse()
    }

    @Test
    fun `start action mode then update repository should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        val updatedWord = Word(id = "2", word = "updated", pos = "prep", ipa = "ipa", meaning = "meaning", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))

        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1")
        assertThat(wordsUiState.wordItems.single { it.isSelected }.word).isEqualTo(words[0])
        assertThat(wordsUiState.wordItems.find { it.word.id == "2" }?.word).isEqualTo(updatedWord)
    }

    @Test
    fun `delete items in action mode should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        homeViewModel.onActionModeMenuDelete()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactlyElementsIn(words)
    }

    @Test
    fun `delete items in action mode then dismiss undo should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onUndoDismissed()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[2])
    }

    @Test
    fun `delete items in action mode then undo deletion should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.undoDeletion()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactlyElementsIn(words)
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactlyElementsIn(words)
    }

    @Test
    fun `delete items continuously then dismiss undo should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onItemLongClicked("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onUndoDismissed()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[2])
    }

    @Test
    fun `delete items continuously then undo deletion should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onItemLongClicked("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.undoDeletion()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[1], words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[1], words[2])
    }

    @Test
    fun `delete all items should show empty screen then undo should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.onActionModeMenuSelectAll()
        homeViewModel.onActionModeMenuDelete()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(wordsUiState.isShowEmptyScreen).isTrue()

        homeViewModel.undoDeletion()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(wordsUiState.isShowEmptyScreen).isFalse()
        assertThat(wordsUiState.wordItems.any { it.isSelected }).isFalse()
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactlyElementsIn(words)
    }

    @Test
    fun `select all in action mode should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.onActionModeMenuSelectAll()

        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1", "2", "3")
        assertThat(wordsUiState.wordItems.all { it.isSelected }).isTrue()

        homeViewModel.onItemLongClicked("1")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("2", "3")
        assertThat(wordsUiState.wordItems.filter { it.isSelected }.map { it.word }).containsExactly(words[1], words[2])
    }

    @Test
    fun `remind item in action mode should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("3")
        homeViewModel.onActionModeMenuRemind()

        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(wordsUiState.wordItems.all { it.word.isRemind }).isTrue()
        assertThat((wordRepository.getWords() as Result.Success).data.all { it.isRemind }).isTrue()
    }

    @Test
    fun `start searching should update ui state correctly`() = runTest {
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEmpty()
        assertThat(searchUiState.searchResult).isEmpty()
    }

    @Test
    fun `search items should update ui state correctly`() = runTest {
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("wo")
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("wo")
        assertThat(searchUiState.searchResult.map { it.word }).containsExactlyElementsIn(words)

        homeViewModel.search("word1")
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("word1")
        assertThat(searchUiState.searchResult.single { it.word.id == "1" }.word).isEqualTo(words[0])

        homeViewModel.search("word4")
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("word4")
        assertThat(searchUiState.searchResult).isEmpty()
    }

    @Test
    fun `search items then stop searching should update ui state correctly`() = runTest {
        var searchUiState = SearchUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("word1")
        homeViewModel.stopSearching()
        assertThat(searchUiState.isSearching).isFalse()
        assertThat(searchUiState.searchQuery).isEmpty()
        assertThat(searchUiState.searchResult).isEmpty()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactlyElementsIn(words)
    }

    @Test
    fun `search items then update repository should update ui state correctly`() = runTest {
        var searchUiState = SearchUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("word")

        val updatedWord = Word(id = "2", word = "updated", pos = "prep", ipa = "ipa", meaning = "meaning", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))
        assertThat(searchUiState.searchResult.map { it.word }).containsExactly(words[0], words[2])
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[0], updatedWord, words[2])
    }

    @Test
    fun `search items then long click on an item should start action mode`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("word")
        homeViewModel.onItemLongClicked("1")
        assertThat(actionModeUiState.isActionMode).isTrue()
        assertThat(actionModeUiState.selectedIds).containsExactly("1")
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("word")
        assertThat(searchUiState.searchResult.map { it.word }).containsExactlyElementsIn(words)
        assertThat(searchUiState.searchResult.single { it.isSelected }.word).isEqualTo(words[0])
    }

    @Test
    fun `search items then start and stop action mode should update ui state correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }

        homeViewModel.startSearching()
        homeViewModel.search("word")
        homeViewModel.onItemLongClicked("1")
        homeViewModel.destroyActionMode()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(searchUiState.isSearching).isTrue()
        assertThat(searchUiState.searchQuery).isEqualTo("word")
        assertThat(searchUiState.searchResult.map { it.word }).containsExactlyElementsIn(words)
        assertThat(searchUiState.searchResult.any { it.isSelected }).isFalse()
    }

    @Test
    fun `search items then click remind menu in action mode should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("3")
        homeViewModel.onActionModeMenuRemind()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(searchUiState.searchResult.map { it.word }).containsExactlyElementsIn(words.map { it.copy(isRemind = true) })
        assertThat((wordRepository.getWords() as Result.Success).data.all { it.isRemind }).isTrue()
    }

    @Test
    fun `search items then click delete menu in action mode should update ui state and repository correctly`() = runTest {
        var actionModeUiState = ActionModeUiState()
        var searchUiState = SearchUiState()
        homeViewModel.actionModeUiState.collectIn(backgroundScope) { actionModeUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        homeViewModel.onItemLongClicked("1")
        homeViewModel.onActionModeMenuDelete()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(searchUiState.searchResult.map { it.word }).containsExactly(words[1], words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactlyElementsIn(words)

        homeViewModel.onItemLongClicked("2")
        homeViewModel.onActionModeMenuDelete()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(actionModeUiState.selectedIds).isEmpty()
        assertThat(searchUiState.searchResult.map { it.word }).containsExactly(words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[1], words[2])

        homeViewModel.undoDeletion()
        assertThat(searchUiState.searchResult.map { it.word }).containsExactly(words[1], words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[1], words[2])

        homeViewModel.onItemLongClicked("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onUndoDismissed()
        assertThat(actionModeUiState.isActionMode).isFalse()
        assertThat(searchUiState.searchResult.map { it.word }).containsExactly(words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[2])
    }

    @Test
    fun `start searching then delete items then stop searching should update ui state and repository correctly`() = runTest {
        var searchUiState = SearchUiState()
        var wordsUiState = WordsUiState()
        homeViewModel.wordsUiState.collectIn(backgroundScope) { wordsUiState = it }
        homeViewModel.searchUiState.collectIn(backgroundScope) { searchUiState = it }
        homeViewModel.startSearching()
        homeViewModel.search("word")

        homeViewModel.onItemLongClicked("1")
        homeViewModel.selectItem("2")
        homeViewModel.onActionModeMenuDelete()
        homeViewModel.onUndoDismissed()
        homeViewModel.stopSearching()
        assertThat(searchUiState.isSearching).isFalse()
        assertThat(searchUiState.searchResult).isEmpty()
        assertThat(searchUiState.searchQuery).isEmpty()
        assertThat(wordsUiState.wordItems.map { it.word }).containsExactly(words[2])
        assertThat((wordRepository.getWords() as Result.Success).data).containsExactly(words[2])
    }

    private fun <T> Flow<T>.collectIn(scope: CoroutineScope, callback: (T) -> Unit) {
        scope.launch(mainCoroutineRule.testDispatcher) {
            this@collectIn.collect {
                callback(it)
            }
        }
    }
}