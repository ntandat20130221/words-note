package com.example.wordnotes.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.onSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@SmallTest
@RunWith(AndroidJUnit4::class)
class WordsDataSourceTest {
    private lateinit var wordsLocalDataSource: WordsLocalDataSource
    private lateinit var wordsDatabase: WordDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        wordsDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), WordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wordsLocalDataSource = WordsLocalDataSource(wordsDao = wordsDatabase.wordDao(), testDispatcher)
    }

    @After
    fun tearDown() {
        wordsDatabase.close()
    }

    @Test
    fun getWordsShouldReturnOneWord() = runTest(testDispatcher.scheduler) {
        val word1 = Word(word = "word1", pos = "pos1", isLearning = true)
        val word2 = Word(word = "word2", pos = "pos2")
        wordsLocalDataSource.saveWord(word1)
        wordsLocalDataSource.saveWord(word2)

        val results = wordsLocalDataSource.getWords()
        results.onSuccess { data ->
            assertThat(data.size, `is`(2))
        }
    }

    @Test
    fun saveWordThenRetrievesWord() = runTest(testDispatcher.scheduler) {
        val word = Word(word = "word", pos = "pos", isLearning = true)
        wordsLocalDataSource.saveWord(word)

        val result = wordsLocalDataSource.getWord(word.id)
        result.onSuccess {
            assertThat(it.id, `is`(word.id))
            assertThat(it.word, `is`("word"))
            assertThat(it.pos, `is`("pos"))
            assertThat(it.ipa, `is`(""))
            assertThat(it.meaning, `is`(""))
            assertThat(it.timestamp, `is`(word.timestamp))
            assertThat(it.isLearning, `is`(true))
        }
    }

    @Test
    fun updateWordThenRetrievesWord() = runTest {
        val word = Word(word = "word", pos = "pos")
        wordsLocalDataSource.saveWord(word)

        val updatedWord = Word(id = word.id, word = "word2", pos = "pos2", isLearning = true)
        wordsLocalDataSource.updateWord(updatedWord)

        val result = wordsLocalDataSource.getWord(word.id)
        result.onSuccess {
            assertThat(it.id, `is`(word.id))
            assertThat(it.word, `is`("word2"))
            assertThat(it.pos, `is`("pos2"))
            assertThat(it.ipa, `is`(updatedWord.ipa))
            assertThat(it.meaning, `is`(updatedWord.meaning))
            assertThat(it.timestamp, `is`(updatedWord.timestamp))
            assertThat(it.isLearning, `is`(true))
        }
    }
}