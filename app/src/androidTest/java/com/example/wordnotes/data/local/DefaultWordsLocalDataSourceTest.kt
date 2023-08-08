package com.example.wordnotes.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
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
    fun getWords_Empty() = runTest(testDispatcher.scheduler) {
        val words = (wordsLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(0)
    }

    @Test
    fun saveWords_getWords() = runTest(testDispatcher.scheduler) {
        val word1 = Word(word = "word1", pos = "pos1", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2")
        wordsLocalDataSource.saveWord(word1)
        wordsLocalDataSource.saveWord(word2)

        val words = (wordsLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(2)
    }

    @Test
    fun getRemindWords() = runTest(testDispatcher.scheduler) {
        val word1 = Word(word = "word1", pos = "pos1", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2")
        val word3 = Word(word = "word3", pos = "pos3", isRemind = true)
        wordsLocalDataSource.saveWord(word1)
        wordsLocalDataSource.saveWord(word2)
        wordsLocalDataSource.saveWord(word3)

        val words = (wordsLocalDataSource.getRemindWords() as Result.Success).data
        assertThat(words[0].id).isEqualTo(word1.id)
        assertThat(words[1].id).isEqualTo(word3.id)
    }

    @Test
    fun saveWord_GetWord() = runTest(testDispatcher.scheduler) {
        val newWord = Word(word = "word", pos = "pos", isRemind = true)
        wordsLocalDataSource.saveWord(newWord)

        val word = (wordsLocalDataSource.getWord(newWord.id) as Result.Success).data
        assertThat(word.id).isEqualTo(word.id)
        assertThat(word.word).isEqualTo("word")
        assertThat(word.pos).isEqualTo("pos")
        assertThat(word.ipa).isEqualTo("")
        assertThat(word.meaning).isEqualTo("")
        assertThat(word.timestamp).isEqualTo(word.timestamp)
        assertThat(word.isRemind).isTrue()
    }

    @Test
    fun updateWord_GetWord() = runTest(testDispatcher.scheduler) {
        val newWord = Word(word = "word", pos = "pos")
        wordsLocalDataSource.saveWord(newWord)

        val updatingWord = Word(id = newWord.id, word = "word2", pos = "pos2", isRemind = true)
        wordsLocalDataSource.updateWord(updatingWord)

        val word = (wordsLocalDataSource.getWord(newWord.id) as Result.Success).data
        assertThat(word.id).isEqualTo(newWord.id)
        assertThat(word.word).isEqualTo("word2")
        assertThat(word.pos).isEqualTo("pos2")
        assertThat(word.ipa).isEqualTo(updatingWord.ipa)
        assertThat(word.meaning).isEqualTo(updatingWord.meaning)
        assertThat(word.timestamp).isEqualTo(updatingWord.timestamp)
        assertThat(word.isRemind).isTrue()
    }

    @Test
    fun deleteWords_GetWords() = runTest(testDispatcher.scheduler) {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2", meaning = "meaning2", isRemind = true)
        val word3 = Word(word = "word3", pos = "pos3", meaning = "meaning3", isRemind = true)
        wordsLocalDataSource.saveWord(word)
        wordsLocalDataSource.saveWord(word2)
        wordsLocalDataSource.saveWord(word3)

        wordsLocalDataSource.deleteWords(listOf(word.id, word3.id))

        val words = (wordsLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(1)

        val nullWord = (wordsLocalDataSource.getWord(word.id) as Result.Success).data
        assertThat(nullWord).isNull()

        val loaded = (wordsLocalDataSource.getWord(word2.id) as Result.Success).data
        assertThat(loaded).isEqualTo(word2)
    }
}