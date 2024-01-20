package com.example.wordnotes.di

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.wordnotes.data.local.WordDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
@Module
object TestDatabaseModule {

    @Singleton
    @Provides
    fun provideTestWordDatabase(): WordDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WordDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
}