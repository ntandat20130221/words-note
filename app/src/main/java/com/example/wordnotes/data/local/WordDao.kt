package com.example.wordnotes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words ORDER BY timestamp DESC")
    fun getWordsStream(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE id = :wordId")
    fun getWordStream(wordId: String): Flow<Word>

    @Query("SELECT * FROM words")
    suspend fun getWords(): List<Word>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWord(wordId: String): Word

    @Query("SELECT * FROM words WHERE remind = 1")
    suspend fun getRemindWords(): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateWords(words: List<Word>)

    @Query("DELETE FROM words WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<String>)

    @Query("DELETE FROM words")
    suspend fun clearWords()

    @Transaction
    suspend fun clearAndInsert(words: List<Word>) {
        clearWords()
        insertWords(words)
    }
}