package com.example.wordnotes.mocks

import androidx.annotation.VisibleForTesting
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.data.wrapWithResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class FakeWordRepository(initialWords: List<Word>?) : WordRepository {

    @Inject
    constructor() : this(
        listOf(
            Word(id = "1", word = "word1", pos = "noun", ipa = "ipa1", meaning = "meaning1", isRemind = true),
            Word(id = "2", word = "word2", pos = "prep", ipa = "ipa2", meaning = "meaning2", isRemind = true),
            Word(id = "3", word = "word3", pos = "verb", ipa = "ipa3", meaning = "meaning3", isRemind = false),
            Word(id = "4", word = "word4", pos = "verb", ipa = "ipa4", meaning = "meaning4", isRemind = true),
            Word(id = "5", word = "word5", pos = "adj", ipa = "ipa5", meaning = "meaning5", isRemind = false),
            Word(id = "6", word = "word6", pos = "verb", ipa = "ipa6", meaning = "meaning6", isRemind = false),
            Word(id = "7", word = "word7", pos = "adv", ipa = "ipa7", meaning = "meaning7", isRemind = true),
            Word(id = "8", word = "word8", pos = "noun", ipa = "ipa8", meaning = "meaning8", isRemind = false),
            Word(id = "9", word = "word9", pos = "noun", ipa = "ipa9", meaning = "meaning9", isRemind = true),
            Word(id = "10", word = "word10", pos = "prep", ipa = "ipa10", meaning = "meaning10", isRemind = true),
            Word(id = "11", word = "word11", pos = "verb", ipa = "ipa11", meaning = "meaning11", isRemind = false),
        )
    )

    private var shouldThrowError = false

    private val savedWords: MutableStateFlow<LinkedHashMap<String, Word>> = MutableStateFlow(LinkedHashMap(initialWords?.associateBy { it.id }))

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override fun getWordsFlow(): Flow<List<Word>> = flow {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.collect { emit(it.values.toList()) }
    }

    override fun getWordFlow(wordId: String): Flow<Word> = flow {
        if (shouldThrowError) throw Exception("Test exception")
        savedWords.collect { words -> words[wordId]?.let { emit(it) } }
    }

    override suspend fun refresh() = if (shouldThrowError)
        throw Exception("Test exception") else
        savedWords.update { it }

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

    override suspend fun saveWords(words: List<Word>) {
        if (shouldThrowError) throw Exception("Test exception")
        addWords(*words.toTypedArray())
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