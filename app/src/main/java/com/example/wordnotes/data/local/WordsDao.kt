package com.example.wordnotes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface WordsDao {

    @Query("SELECT * FROM words")
    fun getWordsStream(): Flow<List<Word>>

    @Query("SELECT * FROM words WHERE id = :wordId")
    fun getWordStream(wordId: String): Flow<Word?>

    @Query("SELECT * FROM words")
    suspend fun getWords(): List<Word>

    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWord(wordId: String): Word

    @Query("SELECT * FROM words WHERE remind = 1")
    suspend fun getRemindWords(): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<Word>)

    @Update
    suspend fun updateWord(word: Word)

    @Query("UPDATE words SET remind = 1 WHERE id IN (:ids)")
    suspend fun remindWords(ids: List<String>)

    @Query("DELETE FROM words WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<String>)

    @Query("DELETE FROM words")
    suspend fun clearWords()
}