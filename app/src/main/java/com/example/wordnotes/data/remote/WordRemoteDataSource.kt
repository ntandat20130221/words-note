package com.example.wordnotes.data.remote

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word

interface WordRemoteDataSource {

    suspend fun loadWords(): Result<List<Word>>

    suspend fun saveWord(word: Word): Result<Unit>

    suspend fun updateWords(words: List<Word>): Result<Unit>

    suspend fun deleteWords(ids: List<String>): Result<Unit>
}