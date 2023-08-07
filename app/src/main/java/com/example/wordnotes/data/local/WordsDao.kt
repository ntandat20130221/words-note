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

    @Query("SELECT * FROM words WHERE learning = 1")
    suspend fun getRemindWords(): List<Word>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Update
    suspend fun updateWord(word: Word)

    @Query("DELETE FROM words WHERE id IN (:id)")
    suspend fun deleteWords(id: List<String>)
}