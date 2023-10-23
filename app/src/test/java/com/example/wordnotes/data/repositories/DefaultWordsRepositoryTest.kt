package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.sharedtest.FakeWordsLocalDataSource
import com.example.wordnotes.sharedtest.FakeWordsNetworkDataSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultWordsRepositoryTest {
    private val data = listOf(
        Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isRemind = true),
        Word(id = "2", word = "word2", pos = "po2s", ipa = "ipa2", meaning = "meaning2", isRemind = true),
        Word(id = "3", word = "word3", pos = "pos3", ipa = "ipa3", meaning = "meaning3")
    )

    private lateinit var wordsLocalDataSource: FakeWordsLocalDataSource
    private lateinit var wordsNetworkDataSource: FakeWordsNetworkDataSource
    private lateinit var wordRepository: DefaultWordsRepository

    @Before
    fun createRepository() {
        wordsLocalDataSource = FakeWordsLocalDataSource(data)
        wordsNetworkDataSource = FakeWordsNetworkDataSource(data)
        wordRepository = DefaultWordsRepository(wordsLocalDataSource, wordsNetworkDataSource)
    }

    @Test
    fun getWords() = runTest {
        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words.size).isEqualTo(data.size)
        assertThat(words).containsExactlyElementsIn(data)
    }

    @Test
    fun deleteAllWords_getWords() = runTest {
        wordsLocalDataSource.deleteWords(data.map { it.id })
        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).isEmpty()
    }

    @Test
    fun getWordsWithLocalDataSourceUnavailable_ReturnsError() = runTest {
        wordsLocalDataSource.words = null
        val result = wordRepository.getWords()
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun getRemindWords() = runTest {
        val remindWords = (wordRepository.getRemindWords() as Result.Success).data
        assertThat(remindWords).hasSize(2)
        assertThat(remindWords[0].id).isEqualTo("1")
        assertThat(remindWords[1].id).isEqualTo("2")
    }

    @Test
    fun saveWord_GetWord() = runTest {
        val newWord = Word(id = "4", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning")
        wordRepository.saveWord(newWord)

        val word = (wordRepository.getWord(newWord.id) as Result.Success).data
        assertThat(word).isEqualTo(newWord)
    }

    @Test
    fun saveWordWithDuplicateId_ReplacesWithNewWord() = runTest {
        val newWord = Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning")
        wordRepository.saveWord(newWord)

        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(3)

        val word = (wordRepository.getWord(newWord.id) as Result.Success).data
        assertThat(word).isEqualTo(newWord)
    }

    @Test
    fun updateWord_GetWord() = runTest {
        val updatingWord = Word(id = "1", word = "word2", pos = "pos2", ipa = "ipa", meaning = "meaning", isRemind = true)
        wordRepository.updateWords(listOf(updatingWord))

        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(3)

        val word = (wordRepository.getWord(updatingWord.id) as Result.Success).data
        assertThat(word).isEqualTo(updatingWord)
    }

    @Test
    fun deleteWords_GetWords() = runTest {
        wordRepository.deleteWords(listOf("1", "2"))

        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(1)
        assertThat(words).containsExactly(data[2])
    }

    @Test
    fun deleteWords_GetDeletedWord_ReturnsError() = runTest {
        wordRepository.deleteWords(listOf("1", "2"))

        val result = wordRepository.getWord(wordId = "1")
        assertThat(result is Result.Error).isTrue()
    }
}