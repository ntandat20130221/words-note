package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

interface WordRepository {

    fun getWordsFlow(): Flow<List<Word>>

    fun getWordFlow(wordId: String): Flow<Word>

    suspend fun refresh()

    suspend fun getWords(): Result<List<Word>>

    suspend fun getWord(wordId: String): Result<Word>

    suspend fun getRemindingWords(): Result<List<Word>>

    suspend fun saveWords(words: List<Word>)

    suspend fun updateWords(words: List<Word>)

    suspend fun deleteWords(ids: List<String>)
}