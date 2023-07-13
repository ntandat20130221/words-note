package com.example.wordnotes.data

import com.example.wordnotes.data.model.Word
import com.example.wordnotes.ui.words.WordUiState

fun Word.toWordUiState() = WordUiState(
    id = this.id,
    word = this.word,
    pos = this.pos,
    ipa = this.ipa,
    meaning = this.meaning,
    isLearning = this.isLearning,
    timestamp = this.timestamp,
    isSelected = false,
)