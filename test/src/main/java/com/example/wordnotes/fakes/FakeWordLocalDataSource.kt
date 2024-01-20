package com.example.wordnotes.fakes

import androidx.annotation.VisibleForTesting
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordLocalDataSource
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.wrapWithResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FakeWordLocalDataSource(initialWords: List<Word> = emptyList()) : WordLocalDataSource {

    @Inject
    constructor() : this(
        listOf(
            Word(id = "1", word = "word1", pos = "verb", ipa = "ipa1", meaning = "meaning1", isRemind = false),
            Word(id = "2", word = "word2", pos = "verb", ipa = "ipa2", meaning = "meaning2", isRemind = true),
            Word(id = "3", word = "word3", pos = "verb", ipa = "ipa3", meaning = "meaning3", isRemind = true),
        )
    )

    private val savedWords = MutableStateFlow<LinkedHashMap<String, Word>>(LinkedHashMap())

    init {
        addWords(*initialWords.toTypedArray())
    }

    private var shouldThrowError = false

    fun shouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    @VisibleForTesting
    fun addWords(vararg words: Word) {
        savedWords.update { oldWords ->
            LinkedHashMap<String, Word>(oldWords).apply {
                words.forEach { this[it.id] = it }
            }
        }
    }

    override fun getWordsFlow(): Flow<List<Word>> = flow {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.collect { emit(it.values.toList()) }
    }

    override fun getWordFlow(wordId: String): Flow<Word> = flow {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.collect { emit(it[wordId]!!) }
    }

    override suspend fun getWords(): Result<List<Word>> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.value.values.toList()
    }

    override suspend fun getWord(wordId: String): Result<Word> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.value[wordId]!!
    }

    override suspend fun getRemindingWords(): Result<List<Word>> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.value.values.filter { it.isRemind }
    }

    override suspend fun saveWord(word: Word): Result<Unit> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        addWords(word)
    }

    override suspend fun saveWords(words: List<Word>): Result<Unit> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        addWords(*words.toTypedArray())
    }

    override suspend fun updateWords(words: List<Word>): Result<Unit> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.update { oldWords ->
            LinkedHashMap<String, Word>(oldWords).also { map -> map.putAll(words.associateBy { it.id }) }
        }
    }

    override suspend fun deleteWords(ids: List<String>): Result<Unit> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also { it.keys.removeAll(ids.toSet()) }
        }
    }

    override suspend fun clearWords(): Result<Unit> = wrapWithResult {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.update { LinkedHashMap() }
    }
}