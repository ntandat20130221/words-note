package com.example.wordnotes.data

import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class WordRepository(private val wordsLocalDataSource: WordsLocalDataSource) {

    fun observeWords() = wordsLocalDataSource.observeWords()

    fun observeWord(wordId: String) = wordsLocalDataSource.observeWord(wordId)

    suspend fun saveWord(word: Word) {
        coroutineScope {
            launch { wordsLocalDataSource.saveWord(word) }
        }
    }
}