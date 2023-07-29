package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

interface WordsRepository {

    fun observeWords(): Flow<Result<List<Word>>>

    fun observeWord(wordId: String): Flow<Result<Word>>

    suspend fun getWords(): Result<List<Word>>

    suspend fun getWord(wordId: String): Result<Word>

    suspend fun getLearningWords(): Result<List<Word>>

    suspend fun saveWord(word: Word)

    suspend fun updateWord(word: Word)

    suspend fun deleteWords(id: List<String>)
}