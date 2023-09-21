package com.example.wordnotes.data.network

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word

interface WordsNetworkDataSource {

    suspend fun loadWords(onCompleted: (Result<List<Word>>) -> Unit)

    suspend fun saveWords(words: List<Word>)

    suspend fun updateWord(word: Word)

    suspend fun deleteWords(ids: List<String>)

    suspend fun remindWords(ids: List<String>)
}