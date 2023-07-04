package com.example.wordnotes.data.local

import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WordsLocalDataSource internal constructor(
    private val wordsDao: WordsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun observeWords(): Flow<List<Word>> = wordsDao.observeWords()

    fun observeWord(wordId: String): Flow<Word> = wordsDao.observeWord(wordId)

    suspend fun getWord(wordId: String): Word = withContext(ioDispatcher) {
        wordsDao.getWord(wordId)
    }

    suspend fun saveWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.insertWord(word)
    }

    suspend fun updateWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.updateWord(word)
    }
}