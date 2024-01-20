package com.example.wordnotes.di

import com.example.wordnotes.data.repositories.DataStoreRepository
import com.example.wordnotes.data.repositories.DefaultDataStoreRepository
import com.example.wordnotes.data.repositories.DefaultUserRepository
import com.example.wordnotes.data.repositories.DefaultWordRepository
import com.example.wordnotes.data.repositories.UserRepository
import com.example.wordnotes.data.repositories.WordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun provideWordRepository(impl: DefaultWordRepository): WordRepository

    @Singleton
    @Binds
    abstract fun provideUserRepository(impl: DefaultUserRepository): UserRepository

    @Singleton
    @Binds
    abstract fun provideDataStoreRepository(impl: DefaultDataStoreRepository): DataStoreRepository
}