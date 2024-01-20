package com.example.wordnotes.data.local

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class RoomWordLocalDataSourceTest {
    private lateinit var wordLocalDataSource: WordLocalDataSource

    @Inject
    lateinit var wordDatabase: WordDatabase

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
        wordLocalDataSource = RoomWordLocalDataSource(wordDao = wordDatabase.wordDao())
    }

    @After
    fun tearDown() {
        wordDatabase.close()
    }

    @Test
    fun getAllWords_ShouldReturnEmpty() = runTest {
        val words = (wordLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(0)
    }

    @Test
    fun saveWords_getWords() = runTest {
        val word1 = Word(word = "word1", pos = "pos1", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2")
        wordLocalDataSource.saveWord(word1)
        wordLocalDataSource.saveWord(word2)

        val words = (wordLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(2)
    }

    @Test
    fun getRemindWords() = runTest {
        val word1 = Word(word = "word1", pos = "pos1", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2")
        val word3 = Word(word = "word3", pos = "pos3", isRemind = true)
        wordLocalDataSource.saveWord(word1)
        wordLocalDataSource.saveWord(word2)
        wordLocalDataSource.saveWord(word3)

        val words = (wordLocalDataSource.getRemindingWords() as Result.Success).data
        assertThat(words[0].id).isEqualTo(word1.id)
        assertThat(words[1].id).isEqualTo(word3.id)
    }

    @Test
    fun saveWord_GetWord() = runTest {
        val newWord = Word(word = "word", pos = "pos", isRemind = true)
        wordLocalDataSource.saveWord(newWord)

        val word = (wordLocalDataSource.getWord(newWord.id) as Result.Success).data
        assertThat(word.id).isEqualTo(word.id)
        assertThat(word.word).isEqualTo("word")
        assertThat(word.pos).isEqualTo("pos")
        assertThat(word.ipa).isEqualTo("")
        assertThat(word.meaning).isEqualTo("")
        assertThat(word.timestamp).isEqualTo(word.timestamp)
        assertThat(word.isRemind).isTrue()
    }

    @Test
    fun updateWord_GetWord() = runTest {
        val newWord = Word(word = "word", pos = "pos")
        wordLocalDataSource.saveWord(newWord)

        val updatingWord = Word(id = newWord.id, word = "word2", pos = "pos2", isRemind = true)
        wordLocalDataSource.updateWords(listOf(updatingWord))

        val word = (wordLocalDataSource.getWord(newWord.id) as Result.Success).data
        assertThat(word.id).isEqualTo(newWord.id)
        assertThat(word.word).isEqualTo("word2")
        assertThat(word.pos).isEqualTo("pos2")
        assertThat(word.ipa).isEqualTo(updatingWord.ipa)
        assertThat(word.meaning).isEqualTo(updatingWord.meaning)
        assertThat(word.timestamp).isEqualTo(updatingWord.timestamp)
        assertThat(word.isRemind).isTrue()
    }

    @Test
    fun remindWords_GetWords() = runTest {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2", meaning = "meaning2")
        val word3 = Word(word = "word3", pos = "pos3", meaning = "meaning3")
        wordLocalDataSource.saveWord(word)
        wordLocalDataSource.saveWord(word2)
        wordLocalDataSource.saveWord(word3)

        wordLocalDataSource.updateWords(
            listOf(
                word.copy(isRemind = true),
                word2.copy(isRemind = true),
                word3.copy(isRemind = true),
            )
        )
        val words = (wordLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(3)
        assertThat(words[0].isRemind).isTrue()
        assertThat(words[1].isRemind).isTrue()
        assertThat(words[2].isRemind).isTrue()
    }

    @Test
    fun deleteWords_GetWords() = runTest {
        val word = Word(word = "word", pos = "pos", meaning = "meaning", isRemind = true)
        val word2 = Word(word = "word2", pos = "pos2", meaning = "meaning2", isRemind = true)
        val word3 = Word(word = "word3", pos = "pos3", meaning = "meaning3", isRemind = true)
        wordLocalDataSource.saveWord(word)
        wordLocalDataSource.saveWord(word2)
        wordLocalDataSource.saveWord(word3)

        wordLocalDataSource.deleteWords(listOf(word.id, word3.id))

        val words = (wordLocalDataSource.getWords() as Result.Success).data
        assertThat(words).hasSize(1)

        val nullWord = (wordLocalDataSource.getWord(word.id) as Result.Success).data
        assertThat(nullWord).isNull()

        val loaded = (wordLocalDataSource.getWord(word2.id) as Result.Success).data
        assertThat(loaded).isEqualTo(word2)
    }
}