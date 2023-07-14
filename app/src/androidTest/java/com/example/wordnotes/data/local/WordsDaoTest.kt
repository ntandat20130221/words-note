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
    fun insertWord_AndGetWords() = runTest {
        val word = Word()
        wordsDao.insertWord(word)

        assertThat(wordsDao.getWords().size, `is`(1))
        assertThat(wordsDao.getWords()[0], equalTo(word))
    }

    @Test
    fun insertWord_AndGetById() = runTest {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isLearning = true)
        wordsDao.insertWord(word)
        val loaded = wordsDao.getWord(word.id)

        assertThat(loaded, notNullValue())
        assertThat(loaded.id, `is`(word.id))
        assertThat(loaded.word, `is`(word.word))
        assertThat(loaded.pos, `is`(word.pos))
        assertThat(loaded.ipa, `is`(""))
        assertThat(loaded.meaning, `is`("meaning"))
        assertThat(loaded.isLearning, `is`(true))
        assertThat(loaded.timestamp, `is`(word.timestamp))
    }

    @Test
    fun insertWord_ReplacesOnConflict() = runTest {
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
    fun updateWord_AndGetById() = runTest {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isLearning = false)
        wordsDao.insertWord(word)

        val updatedWord = Word(word.id, word = "word2", pos = "pos2", isLearning = true)
        wordsDao.insertWord(updatedWord)

        val loaded = wordsDao.getWord(word.id)
        assertThat(loaded.id, `is`(word.id))
        assertThat(loaded.word, `is`("word2"))
        assertThat(loaded.pos, `is`("pos2"))
        assertThat(loaded.meaning, `is`(""))
        assertThat(loaded.isLearning, `is`(true))
    }

    @Test
    fun deleteWords_AndGetWords() = runTest {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isLearning = true)
        val word2 = Word(word = "word2", pos = "pos2", meaning = "meaning2", isLearning = true)
        val word3 = Word(word = "word3", pos = "pos3", meaning = "meaning3", isLearning = true)
        wordsDao.insertWord(word)
        wordsDao.insertWord(word2)
        wordsDao.insertWord(word3)

        wordsDao.deleteWords(listOf(word.id, word3.id))
        assertThat(wordsDao.getWords().size, `is`(1))
        assertThat(wordsDao.getWords()[0], equalTo(word2))

        wordsDao.deleteWords(listOf(word.id, word2.id, word3.id))
        assertThat(wordsDao.getWords().isEmpty(), `is`(true))
    }
}