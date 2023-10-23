package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.network.WordsNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultWordsRepository constructor(
    private val wordsLocalDataSource: WordsLocalDataSource,
    private val wordsNetworkDataSource: WordsNetworkDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WordsRepository {

    override fun getWordsStream(): Flow<Result<List<Word>>> = wordsLocalDataSource.getWordsStream()

    override fun getWordStream(wordId: String): Flow<Result<Word>> = wordsLocalDataSource.getWordStream(wordId)

    override suspend fun refreshWords(): Result<Unit> = withContext(ioDispatcher) {
        when (val result = wordsNetworkDataSource.loadWords()) {
            is Result.Success -> {
                wordsLocalDataSource.clearWords()
                wordsLocalDataSource.saveWords(result.data)
                Result.Success(Unit)
            }

            is Result.Error -> Result.Error(result.exception)

            else -> Result.Loading
        }
    }

    override suspend fun getWords(): Result<List<Word>> = withContext(ioDispatcher) {
        wordsLocalDataSource.getWords()
    }

    override suspend fun getWord(wordId: String): Result<Word> = withContext(ioDispatcher) {
        wordsLocalDataSource.getWord(wordId)
    }

    override suspend fun getRemindWords(): Result<List<Word>> = withContext(ioDispatcher) {
        wordsLocalDataSource.getRemindWords()
    }

    override suspend fun saveWord(word: Word) {
        withContext(ioDispatcher) {
            launch { wordsLocalDataSource.saveWord(word) }
            launch { wordsNetworkDataSource.saveWord(word) }
        }
    }

    override suspend fun updateWords(words: List<Word>) {
        withContext(ioDispatcher) {
            launch { wordsLocalDataSource.updateWords(words) }
            launch { wordsNetworkDataSource.updateWords(words) }
        }
    }

    override suspend fun deleteWords(ids: List<String>) {
        withContext(ioDispatcher) {
            launch { wordsLocalDataSource.deleteWords(ids) }
            launch { wordsNetworkDataSource.deleteWords(ids) }
        }
    }
}