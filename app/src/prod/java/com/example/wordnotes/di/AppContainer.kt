package com.example.wordnotes.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.wordnotes.data.local.DATABASE_NAME
import com.example.wordnotes.data.local.DefaultWordsLocalDataSource
import com.example.wordnotes.data.local.WordDatabase
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.network.DefaultWordNetworkDataSource
import com.example.wordnotes.data.network.FirebaseUserNetworkDataSource
import com.example.wordnotes.data.network.UserNetworkDataSource
import com.example.wordnotes.data.network.WordsNetworkDataSource
import com.example.wordnotes.data.repositories.DataStoreRepository
import com.example.wordnotes.data.repositories.DefaultDataStoreRepository
import com.example.wordnotes.data.repositories.DefaultUserRepository
import com.example.wordnotes.data.repositories.DefaultWordsRepository
import com.example.wordnotes.data.repositories.UserRepository
import com.example.wordnotes.data.repositories.WordsRepository
import com.example.wordnotes.ui.settings.WordPreferences
import com.example.wordnotes.ui.settings.WordReminder

interface Factory<out T> {
    fun create(): T
}

class AppContainer(val context: Context) {
    private val wordDatabase = Room.databaseBuilder(context.applicationContext, WordDatabase::class.java, DATABASE_NAME).build()
    private val wordsLocalDataSource: WordsLocalDataSource = DefaultWordsLocalDataSource(wordDatabase.wordDao())
    private val wordsNetworkDataSource: WordsNetworkDataSource = DefaultWordNetworkDataSource(WorkManager.getInstance(context))
    private val userNetworkDataSource: UserNetworkDataSource = FirebaseUserNetworkDataSource()

    val dataStoreRepository: DataStoreRepository = DefaultDataStoreRepository(context)

    val wordsRepository: WordsRepository = DefaultWordsRepository(wordsLocalDataSource, wordsNetworkDataSource)
    val userRepository: UserRepository = DefaultUserRepository(userNetworkDataSource, dataStoreRepository)

    val wordPreferencesFactory: Factory<WordPreferences> = object : Factory<WordPreferences> {
        override fun create() = WordPreferences(context)
    }

    val wordReminderFactory: Factory<WordReminder> = object : Factory<WordReminder> {
        override fun create() = WordReminder(context, wordPreferencesFactory.create())
    }
}