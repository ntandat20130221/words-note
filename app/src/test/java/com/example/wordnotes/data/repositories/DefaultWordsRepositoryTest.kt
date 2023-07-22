package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.FakeWordsLocalDataSource
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.onSuccess
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DefaultWordsRepositoryTest {
    private val words = listOf(
        Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isLearning = true),
        Word(id = "2", word = "word2", pos = "po2s", ipa = "ipa2", meaning = "meaning2", isLearning = true),
        Word(id = "3", word = "word3", pos = "pos3", ipa = "ipa3", meaning = "meaning3")
    )

    private lateinit var wordsLocalDataSource: FakeWordsLocalDataSource
    private lateinit var wordRepository: DefaultWordsRepository

    @Before
    fun createRepository() {
        wordsLocalDataSource = FakeWordsLocalDataSource(words)
        wordRepository = DefaultWordsRepository(wordsLocalDataSource)
    }

    @Test
    fun getWords() = runTest {
        val result = wordRepository.getWords()

        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data.size).isEqualTo(words.size)
            assertThat(data).containsExactlyElementsIn(words)
        }
    }

    @Test
    fun getWords_EmptyRepository() = runTest {
        wordsLocalDataSource.deleteWords(words.map { it.id })
        val result = wordRepository.getWords()

        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data).isEmpty()
        }
    }

    @Test
    fun getWords_WithLocalDataSourceUnavailable_ReturnsError() = runTest {
        wordsLocalDataSource.words = null
        val result = wordRepository.getWords()

        assertThat(result is Result.Error).isTrue()
    }

    @Test
    fun saveWord_AndGetWord() = runTest {
        val word = Word(id = "4", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning")
        wordRepository.saveWord(word)

        val result = wordRepository.getWord(word.id)
        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data).isEqualTo(word)
        }
    }

    @Test
    fun saveWord_WithDuplicateId_ReplacesWithNewWord() = runTest {
        val word = Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning")
        wordRepository.saveWord(word)

        val wordsResult = wordRepository.getWords()
        assertThat(wordsResult is Result.Success).isTrue()
        wordsResult.onSuccess { data ->
            assertThat(data).hasSize(3)
        }

        val result = wordRepository.getWord(word.id)
        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data).isEqualTo(word)
        }
    }

    @Test
    fun updateWord_AndGetWord() = runTest {
        val word = Word(id = "1", word = "word2", pos = "pos2", ipa = "ipa", meaning = "meaning", isLearning = true)
        wordRepository.updateWord(word)

        val wordsResult = wordRepository.getWords()
        assertThat(wordsResult is Result.Success).isTrue()
        wordsResult.onSuccess { data ->
            assertThat(data).hasSize(3)
        }

        val result = wordRepository.getWord(word.id)
        assertThat(result is Result.Success).isTrue()
        result.onSuccess { data ->
            assertThat(data).isEqualTo(word)
        }
    }

    @Test
    fun deleteWords_AndGetWords() = runTest {
        wordRepository.deleteWords(listOf("1", "2"))

        val wordsResult = wordRepository.getWords()
        assertThat(wordsResult is Result.Success).isTrue()
        wordsResult.onSuccess { data ->
            assertThat(data).hasSize(1)
            assertThat(data).containsExactly(words[2])
        }
    }

    @Test
    fun deleteWords_ThenGetDeletedWord_ReturnsError() = runTest {
        wordRepository.deleteWords(listOf("1", "2"))

        val loaded = wordRepository.getWord(wordId = "1")
        assertThat(loaded is Result.Error).isTrue()
    }
}