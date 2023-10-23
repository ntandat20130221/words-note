package com.example.wordnotes.data.network

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word

interface WordsNetworkDataSource {

    suspend fun loadWords(): Result<List<Word>>

    suspend fun saveWord(word: Word)

    suspend fun updateWords(words: List<Word>)

    suspend fun deleteWords(ids: List<String>)
}