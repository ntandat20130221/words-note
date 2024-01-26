package com.example.wordnotes.di

import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.data.FirebaseAuthWrapperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class FirebaseModule {

    @Binds
    abstract fun bindsFirebaseAuthWrapper(impl: FirebaseAuthWrapperImpl): FirebaseAuthWrapper
}