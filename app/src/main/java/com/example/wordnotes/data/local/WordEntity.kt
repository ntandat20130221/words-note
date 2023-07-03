package com.example.wordnotes.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val id: String,
    val word: String,
    val pos: String,
    val ipa: String,
    val meaning: String,
)