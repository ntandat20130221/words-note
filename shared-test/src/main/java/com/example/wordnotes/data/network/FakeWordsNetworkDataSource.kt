package com.example.wordnotes.data.network

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word

class FakeWordsNetworkDataSource(var words: MutableList<Word>? = mutableListOf()) : WordsNetworkDataSource {

    override suspend fun loadWords(): Result<List<Word>> {
        return words?.let { Result.Success(it) } ?: Result.Error(Exception("Network error"))
    }

    override suspend fun saveWords(words: List<Word>) {
        this.words = words.toMutableList()
    }
}