package com.example.wordnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wordnotes.data.model.Word

const val DATABASE_NAME = "words.db"

@Database(entities = [Word::class], version = 1)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordsDao
}