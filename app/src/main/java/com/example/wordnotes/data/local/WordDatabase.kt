package com.example.wordnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WordEntity::class], version = 1)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordsDao
}