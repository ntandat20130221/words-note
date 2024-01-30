package com.example.wordnotes.data.local

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

interface WordLocalDataSource {

    fun getWordsFlow(): Flow<List<Word>>

    fun getWordFlow(wordId: String): Flow<Word>

    suspend fun getWords(): Result<List<Word>>

    suspend fun getWord(wordId: String): Result<Word>

    suspend fun getRemindingWords(): Result<List<Word>>

    suspend fun saveWords(words: List<Word>): Result<Unit>

    suspend fun updateWords(words: List<Word>): Result<Unit>

    suspend fun deleteWords(ids: List<String>): Result<Unit>

    suspend fun clearWords(): Result<Unit>

    suspend fun clearAndSaveWords(words: List<Word>): Result<Unit>
}