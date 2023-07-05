package com.example.wordnotes.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "words")
data class Word(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val word: String = "",
    val pos: String = "",
    val ipa: String = "",
    val meaning: String = "",
    @ColumnInfo(name = "learning") val isLearning: Boolean = false,
    val timestamp: Long = 0
)