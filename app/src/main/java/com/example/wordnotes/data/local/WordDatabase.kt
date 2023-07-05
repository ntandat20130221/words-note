package com.example.wordnotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.wordnotes.data.model.Word

@Database(entities = [Word::class], version = 1)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordsDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE words ADD learning INTEGER")
    }
}