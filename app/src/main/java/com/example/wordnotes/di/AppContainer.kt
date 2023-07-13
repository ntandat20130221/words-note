package com.example.wordnotes.di

import android.content.Context
import androidx.room.Room
import com.example.wordnotes.data.local.DATABASE_NAME
import com.example.wordnotes.data.local.DefaultWordsLocalDataSource
import com.example.wordnotes.data.local.WordDatabase
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.repositories.DefaultWordRepository
import com.example.wordnotes.data.repositories.WordRepository

class AppContainer(context: Context) {
    private val wordDatabase = Room.databaseBuilder(context.applicationContext, WordDatabase::class.java, DATABASE_NAME).build()
    private val wordsLocalDataSource: WordsLocalDataSource = DefaultWordsLocalDataSource(wordDatabase.wordDao())

    val wordRepository: WordRepository = DefaultWordRepository(wordsLocalDataSource)
}