package com.example.wordnotes.data.repositories

import android.content.Context
import androidx.room.Room
import com.example.wordnotes.data.local.MIGRATION_1_2
import com.example.wordnotes.data.local.WordDatabase
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class WordRepository private constructor(
    private val wordsLocalDataSource: WordsLocalDataSource
) {

    fun observeWords() = wordsLocalDataSource.observeWords()

    fun observeWord(wordId: String) = wordsLocalDataSource.observeWord(wordId)

    suspend fun getWord(wordId: String) = wordsLocalDataSource.getWord(wordId)

    suspend fun saveWord(word: Word) {
        coroutineScope {
            launch { wordsLocalDataSource.saveWord(word) }
        }
    }

    suspend fun updateWord(word: Word) {
        coroutineScope {
            launch { wordsLocalDataSource.updateWord(word) }
        }
    }

    companion object {
        private var INSTANCE: WordRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                val wordDatabase = Room.databaseBuilder(context.applicationContext, WordDatabase::class.java, "words.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                val wordsLocalDataSource = WordsLocalDataSource(wordDatabase.wordDao())
                INSTANCE = WordRepository(wordsLocalDataSource)
            }
        }

        fun get() = INSTANCE ?: throw IllegalStateException("WordRepository must be initialized")
    }
}