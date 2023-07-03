package com.example.wordnotes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsDao {

    @Query("SELECT * FROM words")
    fun observeWords(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE id = :wordId")
    fun observeWord(wordId: String): Flow<Word>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWord(wordId: String): Word

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)
}