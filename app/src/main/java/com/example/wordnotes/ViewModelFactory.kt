package com.example.wordnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.wordnotes.data.repositories.WordsRepository
import com.example.wordnotes.ui.addeditword.AddEditWordViewModel
import com.example.wordnotes.ui.words.WordsViewModel

@Suppress("UNCHECKED_CAST")
val WordViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T = with(modelClass) {
        val application = extras[APPLICATION_KEY] as WordNotesApplication
        val wordsRepository: WordsRepository = application.appContainer.wordsRepository

        when {
            isAssignableFrom(WordsViewModel::class.java) -> WordsViewModel(wordsRepository)
            isAssignableFrom(AddEditWordViewModel::class.java) -> AddEditWordViewModel(wordsRepository, extras.createSavedStateHandle())
            else -> IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}