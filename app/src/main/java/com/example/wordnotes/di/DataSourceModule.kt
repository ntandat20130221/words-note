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

@InstallIn(SingletonComponent::class)
@Module
abstract class DataSourceModule {

    @Binds
    abstract fun bindsWordLocalDataSource(impl: RoomWordLocalDataSource): WordLocalDataSource

    @Binds
    abstract fun bindsWordRemoteDataSource(impl: FirebaseWordRemoteDataSource): WordRemoteDataSource

    @Binds
    abstract fun bindsUserRemoteDataSource(impl: FirebaseUserRemoteDataSource): UserRemoteDataSource
}