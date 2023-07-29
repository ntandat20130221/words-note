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

    override fun observeWords(): Flow<Result<List<Word>>> {
        return wordsDao.observeWords().map {
            Result.Success(it)
        }
    }

    override fun observeWord(wordId: String): Flow<Result<Word>> {
        return wordsDao.observeWord(wordId).map {
            Result.Success(it)
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

    override suspend fun getLearningWords(): Result<List<Word>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(wordsDao.getLeaningWords())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.insertWord(word)
    }

    override suspend fun updateWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.updateWord(word)
    }

    override suspend fun deleteWords(id: List<String>) = withContext(ioDispatcher) {
        wordsDao.deleteWords(id)
    }
}