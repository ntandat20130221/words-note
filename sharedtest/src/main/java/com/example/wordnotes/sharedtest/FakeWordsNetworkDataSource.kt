package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.network.WordsNetworkDataSource

class FakeWordsNetworkDataSource(
    initialWords: List<Word>? = listOf(
        Word(id = "1", word = "word", pos = "verb", ipa = "ipa", meaning = "meaning", isRemind = false),
        Word(id = "2", word = "word2", pos = "verb", ipa = "ipa2", meaning = "meaning2", isRemind = true),
        Word(id = "3", word = "word3", pos = "verb", ipa = "ipa3", meaning = "meaning3", isRemind = true),
    )
) : WordsNetworkDataSource {

    private var _words: MutableMap<String, Word>? = null

    private var words: List<Word>?
        get() = _words?.values?.toList()
        set(value) {
            _words = value?.associateBy { it.id }?.toMutableMap()
        }

    init {
        words = initialWords
    }

    override suspend fun loadWords(): Result<List<Word>> {
        return words?.let { Result.Success(it) } ?: Result.Error(Exception("Word not found"))
    }

    override suspend fun saveWord(word: Word) {
        _words?.put(word.id, word)
    }

    override suspend fun updateWords(words: List<Word>) {
        _words?.putAll(words.associateBy { it.id })
    }

    override suspend fun deleteWords(ids: List<String>) {
        _words?.keys?.removeAll(ids.toSet())
    }
}