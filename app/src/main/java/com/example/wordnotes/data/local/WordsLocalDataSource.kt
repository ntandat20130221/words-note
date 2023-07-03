package com.example.wordnotes.data.local

import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.wordnotes.data.Result
import kotlinx.coroutines.withContext

class WordsLocalDataSource internal constructor(
    private val wordsDao: WordsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    fun observeWords(): Flow<Result<List<Word>>> = wordsDao.observeWords().map { Result.Success(it) }

    fun observeWord(wordId: String) = wordsDao.observeWord(wordId).map { Result.Success(it) }

    suspend fun saveWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.insertWord(word)
    }
}