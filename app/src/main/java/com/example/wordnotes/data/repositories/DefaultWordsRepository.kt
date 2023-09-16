package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class DefaultWordsRepository constructor(
    private val wordsLocalDataSource: WordsLocalDataSource
) : WordsRepository {

    override fun getWordsStream(): Flow<Result<List<Word>>> = wordsLocalDataSource.getWordsStream()

    override fun getWordStream(wordId: String): Flow<Result<Word>> = wordsLocalDataSource.getWordStream(wordId)

    override suspend fun refreshWords() {
        delay(1000)
    }

    override suspend fun getWords(): Result<List<Word>> = wordsLocalDataSource.getWords()

    override suspend fun getWord(wordId: String): Result<Word> = wordsLocalDataSource.getWord(wordId)

    override suspend fun getRemindWords(): Result<List<Word>> = wordsLocalDataSource.getRemindWords()

    override suspend fun saveWord(word: Word) {
        wordsLocalDataSource.saveWord(word)
    }

    override suspend fun updateWord(word: Word) {
        wordsLocalDataSource.updateWord(word)
    }

    override suspend fun remindWords(ids: List<String>) {
        wordsLocalDataSource.remindWords(ids)
    }

    override suspend fun deleteWords(ids: List<String>) {
        wordsLocalDataSource.deleteWords(ids)
    }
}