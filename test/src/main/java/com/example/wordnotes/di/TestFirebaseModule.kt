package com.example.wordnotes.di

import com.example.wordnotes.TestFirebaseAuthWrapper
import com.example.wordnotes.data.FirebaseAuthWrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseModule::class]
)
@Module
abstract class TestFirebaseModule {

    @Binds
    abstract fun provideFirebaseAuthWrapper(impl: TestFirebaseAuthWrapper): FirebaseAuthWrapper
}