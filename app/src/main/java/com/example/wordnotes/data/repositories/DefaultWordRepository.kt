package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordLocalDataSource
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.remote.WordRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultWordRepository(
    private val wordLocalDataSource: WordLocalDataSource,
    private val wordRemoteDataSource: WordRemoteDataSource,
    private val dispatcher: CoroutineDispatcher
) : WordRepository {

    @Inject
    constructor(
        wordsLocalDataSource: WordLocalDataSource,
        wordRemoteDataSource: WordRemoteDataSource
    ) : this(wordsLocalDataSource, wordRemoteDataSource, Dispatchers.IO)

    override fun getWordsFlow(): Flow<List<Word>> = wordLocalDataSource.getWordsFlow()

    override fun getWordFlow(wordId: String): Flow<Word> = wordLocalDataSource.getWordFlow(wordId).filterNotNull()

    override suspend fun refresh() = withContext(dispatcher) {
        val result = wordRemoteDataSource.loadWords()
        if (result is Result.Success) {
            wordLocalDataSource.clearAndSaveWords(result.data)
        }
    }

    override suspend fun getWords(): Result<List<Word>> = withContext(dispatcher) {
        wordLocalDataSource.getWords()
    }

    override suspend fun getWord(wordId: String): Result<Word> = withContext(dispatcher) {
        wordLocalDataSource.getWord(wordId)
    }

    override suspend fun getRemindingWords(): Result<List<Word>> = withContext(dispatcher) {
        wordLocalDataSource.getRemindingWords()
    }

    override suspend fun saveWords(words: List<Word>) = withContext<Unit>(dispatcher) {
        launch { wordLocalDataSource.saveWords(words) }
        launch { wordRemoteDataSource.saveWords(words) }
    }

    override suspend fun updateWords(words: List<Word>) = withContext<Unit>(dispatcher) {
        launch { wordLocalDataSource.updateWords(words) }
        launch { wordRemoteDataSource.updateWords(words) }
    }

    override suspend fun deleteWords(ids: List<String>) = withContext<Unit>(dispatcher) {
        launch { wordLocalDataSource.deleteWords(ids) }
        launch { wordRemoteDataSource.deleteWords(ids) }
    }
}