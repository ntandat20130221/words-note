package com.example.wordnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.ui.addeditword.AddEditWordViewModel
import com.example.wordnotes.ui.words.WordsViewModel

@Suppress("UNCHECKED_CAST")
val WordViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        with(modelClass) {
            val wordRepository = WordRepository.get()
            when {
                isAssignableFrom(WordsViewModel::class.java) -> WordsViewModel(wordRepository)
                isAssignableFrom(AddEditWordViewModel::class.java) -> AddEditWordViewModel(wordRepository)
                else -> IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}