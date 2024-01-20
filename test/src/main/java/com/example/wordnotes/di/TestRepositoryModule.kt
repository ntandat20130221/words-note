package com.example.wordnotes.di

import com.example.wordnotes.data.repositories.DataStoreRepository
import com.example.wordnotes.data.repositories.UserRepository
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.mocks.FakeDataStoreRepository
import com.example.wordnotes.mocks.FakeUserRepository
import com.example.wordnotes.mocks.FakeWordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
@Module
abstract class TestRepositoryModule {

    @Singleton
    @Binds
    abstract fun provideWordRepository(impl: FakeWordRepository): WordRepository

    @Singleton
    @Binds
    abstract fun provideUserRepository(impl: FakeUserRepository): UserRepository

    @Singleton
    @Binds
    abstract fun provideDataStoreRepository(impl: FakeDataStoreRepository): DataStoreRepository
}