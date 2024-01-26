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

    @Binds
    abstract fun bindsWordRepository(impl: DefaultWordRepository): WordRepository

    @Binds
    abstract fun bindsUserRepository(impl: DefaultUserRepository): UserRepository

    @Binds
    abstract fun bindsDataStoreRepository(impl: DefaultDataStoreRepository): DataStoreRepository
}