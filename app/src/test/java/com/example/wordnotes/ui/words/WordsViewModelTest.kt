package com.example.wordnotes.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.wordnotes.data.MainCoroutineRule
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
class WordsViewModelTest {
    private lateinit var wordsViewModel: WordsViewModel
    private lateinit var wordRepository: FakeWordsRepository

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        wordRepository = FakeWordsRepository().apply {
            addWords(Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isLearning = true))
            addWords(Word(id = "2", word = "word2", pos = "pos2", ipa = "ipa2", meaning = "meaning2", isLearning = true))
            addWords(Word(id = "3", word = "word3", pos = "pos3", ipa = "ipa3", meaning = "meaning3"))
        }
        wordsViewModel = WordsViewModel(wordRepository)
    }

    @Test
    fun checkSize() = runTest {
        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.words).hasSize(3)
    }

    @Test
    fun addWordsFromRepository_CheckUiState() = runTest {
        wordRepository.saveWord(Word(id = "4", word = "word4", isLearning = true))

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.words).hasSize(4)
    }

    @Test
    fun startActionMode_ThenCheckUitState() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")

        assertThat(wordsViewModel.actionModeEvent.value?.getContent()).isEqualTo(ActionModeState.STARTED)

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.isActionMode).isTrue()
        assertThat(firstUiState.selectedCount).isEqualTo(1)
        assertThat(firstUiState.words.filter { it.isSelected }).hasSize(1)
        assertThat(firstUiState.words[0].isSelected).isTrue()
    }

    @Test
    fun clickItemInActionMode_UntilDestroy_CheckUiState() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.itemClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.actionModeEvent.value?.getContent()).isEqualTo(ActionModeState.STOPPED)

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.isActionMode).isFalse()
        assertThat(firstUiState.selectedCount).isEqualTo(0)
        assertThat(firstUiState.words.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun clickItemNotInActionMode_CheckOneTimeEvent() = runTest {
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isNull()
    }

    @Test
    fun startActionMode_ThenDestroyActionMode_ThenCheckUiState() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.destroyActionMode()

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.isActionMode).isFalse()
        assertThat(firstUiState.selectedCount).isEqualTo(0)
        assertThat(firstUiState.words.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_ThenDestroyActionMode_ThenClickItem() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.destroyActionMode()
        wordsViewModel.itemClicked(wordId = "2")

        assertThat(wordsViewModel.clickItemEvent.value?.getContentIfHasNotBeenHandled()).isEqualTo("2")
    }

    @Test
    fun startActionMode_ThenClickItems_ThenClickEditMenu_CheckUiState_NothingHappened() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")

        wordsViewModel.onActionModeMenuEdit()

        assertThat(wordsViewModel.actionModeEvent.value?.getContent()).isEqualTo(ActionModeState.STARTED)
        assertThat(wordsViewModel.clickItemEvent.value?.getContent()).isNull()

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.isActionMode).isTrue()
        assertThat(firstUiState.selectedCount).isEqualTo(2)
        assertThat(firstUiState.words.filter { it.isSelected }).hasSize(2)
        assertThat(firstUiState.words[2].isSelected).isFalse()
    }

    @Test
    fun startActionMode_ClickEditMenu_CheckUiState() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.itemClicked(wordId = "1")

        wordsViewModel.onActionModeMenuEdit()

        assertThat(wordsViewModel.clickItemEvent.value?.getContent()).isEqualTo("2")
        assertThat(wordsViewModel.actionModeEvent.value?.getContent()).isEqualTo(ActionModeState.STOPPED)

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.isActionMode).isFalse()
        assertThat(firstUiState.selectedCount).isEqualTo(0)
        assertThat(firstUiState.words.filter { it.isSelected }).hasSize(0)
    }

    @Test
    fun startActionMode_ClickDeleteMenu_CheckUiState() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")
        wordsViewModel.itemClicked(wordId = "2")
        wordsViewModel.itemClicked(wordId = "3")
        wordsViewModel.itemClicked(wordId = "1")

        wordsViewModel.onActionModeMenuDelete()

        assertThat(wordsViewModel.actionModeEvent.value?.getContent()).isEqualTo(ActionModeState.STOPPED)

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.words).hasSize(1)
        assertThat(firstUiState.words[0].id).isEqualTo("1")
    }

    @Test
    fun startActionMode_ClickSelectAllMenu_CheckUiState() = runTest {
        wordsViewModel.itemLongClicked(wordId = "1")

        wordsViewModel.onActionModeMenuSelectAll()

        assertThat(wordsViewModel.actionModeEvent.value?.getContent()).isEqualTo(ActionModeState.STARTED)

        val firstUiState = wordsViewModel.uiState.first()
        assertThat(firstUiState.words.filter { it.isSelected }).hasSize(3)
    }
}