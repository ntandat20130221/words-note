package com.example.wordnotes

import android.app.Application
import com.example.wordnotes.data.repositories.WordRepository

class WordNotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WordRepository.initialize(this)
    }
}