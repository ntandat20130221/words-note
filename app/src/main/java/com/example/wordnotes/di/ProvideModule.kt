package com.example.wordnotes.di

import android.content.Context
import androidx.work.WorkManager
import com.example.wordnotes.data.local.WordDao
import com.example.wordnotes.data.local.WordDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object ProvideModule {

    @Provides
    fun provideWordDao(wordDatabase: WordDatabase): WordDao = wordDatabase.wordDao()

    @Provides
    fun provideWordManager(@ApplicationContext context: Context) = WorkManager.getInstance(context)
}