package com.example.wordnotes.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.onSuccess
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@SmallTest
@RunWith(AndroidJUnit4::class)
class DefaultWordsLocalDataSourceTest {
    private lateinit var wordsLocalDataSource: WordsLocalDataSource
    private lateinit var wordsDatabase: WordDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        wordsDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), WordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wordsLocalDataSource = DefaultWordsLocalDataSource(wordsDao = wordsDatabase.wordDao(), testDispatcher)
    }

    @After
    fun tearDown() {
        wordsDatabase.close()
    }

    @Test
    fun getWords_ShouldReturnOneWord() = runTest(testDispatcher.scheduler) {
        val word1 = Word(word = "word1", pos = "pos1", isLearning = true)
        val word2 = Word(word = "word2", pos = "pos2")
        wordsLocalDataSource.saveWord(word1)
        wordsLocalDataSource.saveWord(word2)

        val results = wordsLocalDataSource.getWords()
        results.onSuccess { data ->
            assertThat(data).hasSize(2)
        }
    }

    @Test
    fun getLearningWords() = runTest(testDispatcher.scheduler) {
        val word1 = Word(word = "word1", pos = "pos1", isLearning = true)
        val word2 = Word(word = "word2", pos = "pos2")
        val word3 = Word(word = "word3", pos = "pos3", isLearning = true)
        wordsLocalDataSource.saveWord(word1)
        wordsLocalDataSource.saveWord(word2)
        wordsLocalDataSource.saveWord(word3)

        val results = wordsLocalDataSource.getLearningWords()
        assertThat(results is Result.Success).isTrue()
        results.onSuccess { data ->
            assertThat(data[0].id).isEqualTo(word1.id)
            assertThat(data[1].id).isEqualTo(word3.id)
        }
    }

    @Test
    fun saveWord_ThenRetrievesWord() = runTest(testDispatcher.scheduler) {
        val word = Word(word = "word", pos = "pos", isLearning = true)
        wordsLocalDataSource.saveWord(word)

        val result = wordsLocalDataSource.getWord(word.id)
        result.onSuccess {
            assertThat(it.id).isEqualTo(word.id)
            assertThat(it.word).isEqualTo("word")
            assertThat(it.pos).isEqualTo("pos")
            assertThat(it.ipa).isEqualTo("")
            assertThat(it.meaning).isEqualTo("")
            assertThat(it.timestamp).isEqualTo(word.timestamp)
            assertThat(it.isLearning).isTrue()
        }
    }

    @Test
    fun updateWord_ThenRetrievesWord() = runTest {
        val word = Word(word = "word", pos = "pos")
        wordsLocalDataSource.saveWord(word)

        val updatedWord = Word(id = word.id, word = "word2", pos = "pos2", isLearning = true)
        wordsLocalDataSource.updateWord(updatedWord)

        val result = wordsLocalDataSource.getWord(word.id)
        result.onSuccess {
            assertThat(it.id).isEqualTo(word.id)
            assertThat(it.word).isEqualTo("word2")
            assertThat(it.pos).isEqualTo("pos2")
            assertThat(it.ipa).isEqualTo(updatedWord.ipa)
            assertThat(it.meaning).isEqualTo(updatedWord.meaning)
            assertThat(it.timestamp).isEqualTo(updatedWord.timestamp)
            assertThat(it.isLearning).isTrue()
        }
    }

    @Test
    fun deleteWords_ThenRetrievesWords() = runTest {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isLearning = true)
        val word2 = Word(word = "word2", pos = "pos2", meaning = "meaning2", isLearning = true)
        val word3 = Word(word = "word3", pos = "pos3", meaning = "meaning3", isLearning = true)
        wordsLocalDataSource.saveWord(word)
        wordsLocalDataSource.saveWord(word2)
        wordsLocalDataSource.saveWord(word3)

        wordsLocalDataSource.deleteWords(listOf(word.id, word3.id))

        val sizeResult = wordsLocalDataSource.getWords()
        sizeResult.onSuccess {
            assertThat(it).hasSize(1)
        }

        val nullResult = wordsLocalDataSource.getWord(word.id)
        assertThat(nullResult is Result.Success).isTrue()
        nullResult.onSuccess {
            assertThat(it).isNull()
        }

        val result = wordsLocalDataSource.getWord(word2.id)
        assertThat(result is Result.Success).isTrue()
        result.onSuccess {
            assertThat(it).isEqualTo(word2)
        }
    }
}