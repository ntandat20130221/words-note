package com.example.wordnotes

import android.app.Application
import com.example.wordnotes.data.repositories.DefaultWordRepository
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.di.AppContainer

class WordNotesApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}