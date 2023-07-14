package com.example.wordnotes.di

import android.content.Context
import androidx.room.Room
import com.example.wordnotes.data.local.DATABASE_NAME
import com.example.wordnotes.data.local.DefaultWordsLocalDataSource
import com.example.wordnotes.data.local.WordDatabase
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.repositories.DefaultWordsRepository
import com.example.wordnotes.data.repositories.WordsRepository

class AppContainer(context: Context) {
    private val wordDatabase = Room.databaseBuilder(context.applicationContext, WordDatabase::class.java, DATABASE_NAME).build()
    private val wordsLocalDataSource: WordsLocalDataSource = DefaultWordsLocalDataSource(wordDatabase.wordDao())

    val wordsRepository: WordsRepository = DefaultWordsRepository(wordsLocalDataSource)
}