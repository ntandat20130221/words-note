package com.example.wordnotes.mocks

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.remote.WordRemoteDataSource
import com.example.wordnotes.data.wrapWithResult
import javax.inject.Inject

class FakeWordRemoteDataSource(initialWords: List<Word>?) : WordRemoteDataSource {

    @Inject
    constructor() : this(
        listOf(
            Word(id = "1", word = "word", pos = "verb", ipa = "ipa", meaning = "meaning", isRemind = false),
            Word(id = "2", word = "word2", pos = "verb", ipa = "ipa2", meaning = "meaning2", isRemind = true),
            Word(id = "3", word = "word3", pos = "verb", ipa = "ipa3", meaning = "meaning3", isRemind = true),
        )
    )

    private var _words: MutableMap<String, Word>? = null
    var words: List<Word>?
        get() = _words?.values?.toList()
        set(value) {
            _words = value?.associateBy { it.id }?.toMutableMap()
        }

    init {
        words = initialWords
    }

    override suspend fun loadWords(): Result<List<Word>> = wrapWithResult { words ?: throw Exception("Words not found") }

    override suspend fun saveWords(words: List<Word>): Result<Unit> = wrapWithResult { _words?.putAll(words.associateBy { it.id }) }

    override suspend fun updateWords(words: List<Word>): Result<Unit> = wrapWithResult { _words?.putAll(words.associateBy { it.id }) }

    override suspend fun deleteWords(ids: List<String>): Result<Unit> = wrapWithResult { _words?.keys?.removeAll(ids.toSet()) }
}