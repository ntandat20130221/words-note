package com.example.wordnotes.data.network

import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.Result

interface WordsNetworkDataSource {

    suspend fun loadWords(): Result<List<Word>>

    suspend fun saveWords(words: List<Word>)
}