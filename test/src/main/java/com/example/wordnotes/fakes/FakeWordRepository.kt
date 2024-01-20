package com.example.wordnotes.fakes

import androidx.annotation.VisibleForTesting
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.data.wrapWithResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FakeWordRepository @Inject constructor() : WordRepository {
    private var shouldThrowError = false

    private val savedWords: MutableStateFlow<LinkedHashMap<String, Word>> = MutableStateFlow(LinkedHashMap())

    init {
        addWords(
            Word(id = "1", word = "word", pos = "noun", ipa = "ipa", meaning = "meaning", isRemind = true),
            Word(id = "2", word = "word2", pos = "prep.", ipa = "ipa2", meaning = "meaning2", isRemind = true),
            Word(id = "3", word = "word3", pos = "", ipa = "ipa3", meaning = "meaning3")
        )
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override fun getWordsFlow(): Flow<List<Word>> = flow {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.collect { emit(it.values.toList()) }
    }

    override fun getWordFlow(wordId: String): Flow<Word> = flow {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.collect { emit(it[wordId]!!) }
    }

    override suspend fun refresh() = if (shouldThrowError) throw Exception("Test exception") else delay(1000)

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

    override suspend fun saveWord(word: Word) {
        if (shouldThrowError) throw Exception("Test exception")
        addWords(word)
    }

    override suspend fun updateWords(words: List<Word>) {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.update { oldWords ->
            LinkedHashMap<String, Word>(oldWords).also { map -> map.putAll(words.associateBy { it.id }) }
        }
    }

    override suspend fun deleteWords(ids: List<String>) {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also { it.keys.removeAll(ids.toSet()) }
        }
    }

    @VisibleForTesting
    fun addWords(vararg words: Word) {
        savedWords.update { oldWords ->
            LinkedHashMap<String, Word>(oldWords).apply {
                words.forEach { this[it.id] = it }
            }
        }
    }
}