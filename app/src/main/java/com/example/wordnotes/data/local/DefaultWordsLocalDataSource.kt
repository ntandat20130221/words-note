package com.example.wordnotes.data.local

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DefaultWordsLocalDataSource internal constructor(
    private val wordsDao: WordsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WordsLocalDataSource {

    override fun getWordsStream(): Flow<Result<List<Word>>> {
        return wordsDao.getWordsStream().map {
            Result.Success(it)
        }
    }

    override fun getWordStream(wordId: String): Flow<Result<Word>> {
        return wordsDao.getWordStream(wordId).map { word ->
            if (word != null)
                Result.Success(word)
            else
                Result.Error(Exception("Word not found"))
        }
    }

    override suspend fun getWords(): Result<List<Word>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(wordsDao.getWords())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getWord(wordId: String): Result<Word> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(wordsDao.getWord(wordId))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getRemindWords(): Result<List<Word>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(wordsDao.getRemindWords())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.insertWord(word)
    }

    override suspend fun saveWords(words: List<Word>) {
        wordsDao.insertWords(words)
    }

    override suspend fun updateWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.updateWord(word)
    }

    override suspend fun remindWords(ids: List<String>) {
        wordsDao.remindWords(ids)
    }

    override suspend fun deleteWords(ids: List<String>) = withContext(ioDispatcher) {
        wordsDao.deleteWords(ids)
    }

    override suspend fun clearWords() {
        wordsDao.clearWords()
    }
}