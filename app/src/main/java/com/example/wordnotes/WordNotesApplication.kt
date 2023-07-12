package com.example.wordnotes

import android.app.Application
import com.example.wordnotes.data.repositories.DefaultWordRepository

class WordNotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DefaultWordRepository.initialize(this)
    }
}