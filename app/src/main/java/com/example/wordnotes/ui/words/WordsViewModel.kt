package com.example.wordnotes.ui.words

import androidx.lifecycle.ViewModel
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.data.model.Word
import java.util.UUID

class WordsViewModel(private val wordRepository: WordRepository) : ViewModel() {

    val words: List<Word> = listOf(
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
        Word(UUID.randomUUID().toString(), "contemptuous", "adjective", "/kənˈtemptʃuəs/", "khinh khỉnh", 0L),
    )
}