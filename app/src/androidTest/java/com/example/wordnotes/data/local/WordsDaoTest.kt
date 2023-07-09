package com.example.wordnotes.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class WordsDaoTest {
    private lateinit var wordsDatabase: WordDatabase
    private lateinit var wordsDao: WordsDao

    @Before
    fun createDatabase() {
        wordsDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), WordDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        wordsDao = wordsDatabase.wordDao()
    }

    @After
    fun closeDatabase() {
        wordsDatabase.close()
    }

    @Test
    fun writeWordAndReadWords() = runTest {
        val word = Word()
        wordsDao.insertWord(word)

        assertThat(wordsDao.getWords().size, `is`(1))
        assertThat(wordsDao.getWords()[0], equalTo(word))
    }

    @Test
    fun writeWordAndReadById() = runTest {
        val word = Word(word = "word", pos = "pos", isLearning = true)
        wordsDao.insertWord(word)
        val loaded = wordsDao.getWord(word.id)

        assertThat(loaded, notNullValue())
        assertThat(loaded.id, `is`(word.id))
        assertThat(loaded.word, `is`(word.word))
        assertThat(loaded.pos, `is`(word.pos))
        assertThat(loaded.ipa, `is`(""))
        assertThat(loaded.meaning, `is`(""))
        assertThat(loaded.isLearning, `is`(true))
        assertThat(loaded.timestamp, `is`(word.timestamp))
    }

    @Test
    fun writeWordReplacesOnConflict() = runTest {
        val word1 = Word(word = "word")
        wordsDao.insertWord(word1)

        val word2 = Word(id = word1.id, word = "word2", isLearning = true)
        wordsDao.insertWord(word2)

        assertThat(wordsDao.getWords().size, `is`(1))

        val loaded = wordsDao.getWord(word1.id)
        assertThat(loaded.id, `is`(word1.id))
        assertThat(loaded.word, `is`("word2"))
        assertThat(loaded.isLearning, `is`(true))
    }

    @Test
    fun updateWordAndReadById() = runTest {
        val word = Word(word = "word", pos = "pos")
        wordsDao.insertWord(word)

        val updatedWord = Word(word.id, word = "word2", pos = "pos2", isLearning = true)
        wordsDao.insertWord(updatedWord)

        val loaded = wordsDao.getWord(word.id)
        assertThat(loaded.id, `is`(word.id))
        assertThat(loaded.word, `is`("word2"))
        assertThat(loaded.pos, `is`("pos2"))
        assertThat(loaded.isLearning, `is`(true))
    }
}