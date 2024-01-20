package com.example.wordnotes.di

import com.example.wordnotes.data.local.RoomWordLocalDataSource
import com.example.wordnotes.data.local.WordLocalDataSource
import com.example.wordnotes.data.remote.FirebaseUserRemoteDataSource
import com.example.wordnotes.data.remote.FirebaseWordRemoteDataSource
import com.example.wordnotes.data.remote.UserRemoteDataSource
import com.example.wordnotes.data.remote.WordRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DataSourceModule {

    @Singleton
    @Binds
    abstract fun provideWordLocalDataSource(impl: RoomWordLocalDataSource): WordLocalDataSource

    @Singleton
    @Binds
    abstract fun provideWordRemoteDataSource(impl: FirebaseWordRemoteDataSource): WordRemoteDataSource

    @Singleton
    @Binds
    abstract fun provideUserRemoteDataSource(impl: FirebaseUserRemoteDataSource): UserRemoteDataSource
}