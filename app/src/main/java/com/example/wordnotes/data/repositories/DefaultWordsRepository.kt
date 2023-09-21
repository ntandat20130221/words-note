package com.example.wordnotes.data.repositories

import android.util.Log
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.network.WordsNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DefaultWordsRepository constructor(
    private val wordsLocalDataSource: WordsLocalDataSource,
    private val wordsNetworkDataSource: WordsNetworkDataSource
) : WordsRepository {

    override fun getWordsStream(): Flow<Result<List<Word>>> = wordsLocalDataSource.getWordsStream()

    override fun getWordStream(wordId: String): Flow<Result<Word>> = wordsLocalDataSource.getWordStream(wordId)

    override suspend fun refreshWords() {
        wordsNetworkDataSource.loadWords { result ->
            when (result) {
                is Result.Success -> {
                    CoroutineScope(Job()).launch {
                        wordsLocalDataSource.clearWords()
                        wordsLocalDataSource.saveWords(result.data)
                    }
                }

                is Result.Error -> {
                    Log.d("TAG", "refreshWords: ${result.exception}")
                }

                else -> {

                }
            }
        }
    }

    override suspend fun getWords(): Result<List<Word>> = wordsLocalDataSource.getWords()

    override suspend fun getWord(wordId: String): Result<Word> = wordsLocalDataSource.getWord(wordId)

    override suspend fun getRemindWords(): Result<List<Word>> = wordsLocalDataSource.getRemindWords()

    override suspend fun saveWord(word: Word) {
        wordsLocalDataSource.saveWord(word)
        wordsNetworkDataSource.saveWords(listOf(word))
    }

    override suspend fun updateWord(word: Word) {
        wordsLocalDataSource.updateWord(word)
        wordsNetworkDataSource.updateWord(word)
    }

    override suspend fun remindWords(ids: List<String>) {
        wordsLocalDataSource.remindWords(ids)
        wordsNetworkDataSource.remindWords(ids)
    }

    override suspend fun deleteWords(ids: List<String>) {
        wordsLocalDataSource.deleteWords(ids)
        wordsNetworkDataSource.deleteWords(ids)
    }
}