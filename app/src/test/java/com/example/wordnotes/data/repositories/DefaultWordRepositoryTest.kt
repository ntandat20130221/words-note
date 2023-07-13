package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.local.FakeWordsLocalDataSource
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before

@ExperimentalCoroutinesApi
class DefaultWordRepositoryTest {
    private val words = listOf(
        Word(id = "1", word = "word", pos = "pos", ipa = "ipa", meaning = "meaning", isLearning = true),
        Word(id = "2", word = "word2", pos = "po2s", ipa = "ipa2", meaning = "meaning2", isLearning = true),
        Word(id = "3", word = "word3", pos = "pos3", ipa = "ipa3", meaning = "meaning3")
    )

    private lateinit var wordsLocalDataSource: WordsLocalDataSource
    private lateinit var wordRepository: WordRepository

    @Before
    fun createRepository() {
        wordsLocalDataSource = FakeWordsLocalDataSource(words)
        TODO("Make DefaultWordRepository available for unit test")
    }
}