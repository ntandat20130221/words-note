package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.fakes.FakeWordLocalDataSource
import com.example.wordnotes.fakes.FakeWordRemoteDataSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultWordRepositoryTest {
    private val data = listOf(
        Word(id = "1", word = "word1", pos = "pos1", ipa = "ipa1", meaning = "meaning1", isRemind = true),
        Word(id = "2", word = "word2", pos = "pos2", ipa = "ipa2", meaning = "meaning2", isRemind = true),
        Word(id = "3", word = "word3", pos = "pos3", ipa = "ipa3", meaning = "meaning3")
    )

    private lateinit var wordLocalDataSource: FakeWordLocalDataSource
    private lateinit var wordRemoteDataSource: FakeWordRemoteDataSource
    private lateinit var wordRepository: DefaultWordRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun createRepository() {
        wordLocalDataSource = FakeWordLocalDataSource(data)
        wordRemoteDataSource = FakeWordRemoteDataSource(data)
        wordRepository = DefaultWordRepository(wordLocalDataSource, wordRemoteDataSource, dispatcher = testDispatcher)
    }

    @Test
    fun `test words flow`() = runTest(testDispatcher) {
        var words = emptyList<Word>()
        backgroundScope.launch(testDispatcher) {
            wordRepository.getWordsFlow().collect { words = it }
        }
        assertThat(words).hasSize(3)
        wordLocalDataSource.addWords(Word(id = "4", word = "word4", pos = "pos4", ipa = "ipa4", meaning = "meaning4", isRemind = true))
        assertThat(words).hasSize(4)
    }

    @Test
    fun `delete all words then get words flow should return empty`() = runTest(testDispatcher) {
        var words = emptyList<Word>()
        backgroundScope.launch(testDispatcher) {
            wordRepository.getWordsFlow().collect { words = it }
        }
        wordLocalDataSource.clearWords()
        assertThat(words).isEmpty()
    }

    @Test
    fun `test word flow`() = runTest(testDispatcher) {
        var word: Word? = null
        backgroundScope.launch(testDispatcher) {
            wordRepository.getWordFlow("1").collect { word = it }
        }
        assertThat(word).isEqualTo(data[0])
        wordLocalDataSource.updateWords(listOf(word!!.copy(word = "word")))
        assertThat(word!!.word).isEqualTo("word")
    }

    @Test(expected = NullPointerException::class)
    fun `delete word then get word flow should throw exception`() = runTest(testDispatcher) {
        var word: Word? = null
        backgroundScope.launch(testDispatcher) {
            wordRepository.getWordFlow("1").collect { word = it }
        }
        wordLocalDataSource.deleteWords(listOf(word!!.id))
    }

    @Test
    fun `get all words should return the same as data`() = runTest(testDispatcher) {
        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words.size).isEqualTo(data.size)
        assertThat(words).containsExactlyElementsIn(data)
    }

    @Test
    fun `delete all words then get words should return empty`() = runTest(testDispatcher) {
        wordLocalDataSource.deleteWords(data.map { it.id })
        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).isEmpty()
    }

    @Test
    fun `make local data source unavailable then get words should return error`() = runTest(testDispatcher) {
        wordLocalDataSource.shouldThrowError(true)
        val result = wordRepository.getWords()
        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun `get reminded words and check value should successful`() = runTest(testDispatcher) {
        val remindWords = (wordRepository.getRemindingWords() as Result.Success).data
        assertThat(remindWords).hasSize(2)
        assertThat(remindWords[0].id).isEqualTo("1")
        assertThat(remindWords[1].id).isEqualTo("2")
    }

    @Test
    fun `save new word then check data should successful`() = runTest(testDispatcher) {
        val newWord = Word(id = "4", word = "word4", pos = "pos4", ipa = "ipa4", meaning = "meaning4")
        wordRepository.saveWord(newWord)
        val word = (wordRepository.getWord(newWord.id) as Result.Success).data
        assertThat(word).isEqualTo(newWord)
    }

    @Test
    fun `save new word with existing id should replaces the new one and the size remains unchanged`() = runTest(testDispatcher) {
        val newWord = Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning")
        wordRepository.saveWord(newWord)

        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(3)

        val word = (wordRepository.getWord(newWord.id) as Result.Success).data
        assertThat(word).isEqualTo(newWord)
    }

    @Test
    fun `update words then check data should successful`() = runTest(testDispatcher) {
        val updatingWords = listOf(
            Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isRemind = false),
            Word(id = "2", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isRemind = false),
        )
        wordRepository.updateWords(updatingWords)

        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(3)

        val word = (wordRepository.getWord("1") as Result.Success).data
        assertThat(word).isEqualTo(updatingWords[0])
    }

    @Test
    fun `delete words then check data should successful`() = runTest(testDispatcher) {
        wordRepository.deleteWords(listOf("1", "2"))
        val words = (wordRepository.getWords() as Result.Success).data
        assertThat(words).hasSize(1)
        assertThat(words).containsExactly(data[2])
    }

    @Test
    fun `delete word then get the deleted word should return error`() = runTest(testDispatcher) {
        wordRepository.deleteWords(listOf("1", "2"))
        val result = wordRepository.getWord(wordId = "1")
        assertThat(result is Result.Error).isTrue()
    }
}