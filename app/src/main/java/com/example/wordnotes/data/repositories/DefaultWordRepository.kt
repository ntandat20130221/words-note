package com.example.wordnotes.data.repositories

import android.content.Context
import androidx.room.Room
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.DATABASE_NAME
import com.example.wordnotes.data.local.DefaultWordsLocalDataSource
import com.example.wordnotes.data.local.WordDatabase
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

class DefaultWordRepository private constructor(
    private val wordsLocalDataSource: WordsLocalDataSource
) : WordRepository {

    override fun observeWords(): Flow<Result<List<Word>>> = wordsLocalDataSource.observeWords()

    override fun observeWord(wordId: String): Flow<Result<Word>> = wordsLocalDataSource.observeWord(wordId)

    override suspend fun getWords(): Result<List<Word>> = wordsLocalDataSource.getWords()

    override suspend fun getWord(wordId: String): Result<Word> = wordsLocalDataSource.getWord(wordId)

    override suspend fun saveWord(word: Word) {
        wordsLocalDataSource.saveWord(word)
    }

    override suspend fun updateWord(word: Word) {
        wordsLocalDataSource.updateWord(word)
    }

    override suspend fun deleteWords(id: List<String>) {
        wordsLocalDataSource.deleteWords(id)
    }

    companion object {
        private var INSTANCE: DefaultWordRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                val wordDatabase = Room.databaseBuilder(context.applicationContext, WordDatabase::class.java, DATABASE_NAME).build()
                val wordsLocalDataSource = DefaultWordsLocalDataSource(wordDatabase.wordDao())
                INSTANCE = DefaultWordRepository(wordsLocalDataSource)
            }
        }

        fun get() = INSTANCE ?: throw IllegalStateException("WordRepository must be initialized")
    }
}