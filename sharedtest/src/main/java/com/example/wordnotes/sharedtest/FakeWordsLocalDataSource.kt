package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeWordsLocalDataSource(initialWords: List<Word>? = emptyList()) : WordsLocalDataSource {
    private var _words: MutableMap<String, Word>? = null

    var words: List<Word>?
        get() = _words?.values?.toList()
        set(value) {
            _words = value?.associateBy { it.id }?.toMutableMap()
        }

    init {
        words = initialWords
    }

    override fun getWordsStream(): Flow<Result<List<Word>>> {
        words?.let { return flow { emit(Result.Success(it)) } }
        return flow { emit(Result.Error(Exception("Words not found"))) }
    }

    override fun getWordStream(wordId: String): Flow<Result<Word>> {
        _words?.let {
            val word = _words?.get(wordId) ?: return flow { emit(Result.Error(Exception("Word not found"))) }
            return flow { emit(Result.Success(word)) }
        }
            ?: return flow { emit(Result.Error(Exception("Word not found"))) }
    }

    override suspend fun getWords(): Result<List<Word>> {
        return words?.let { Result.Success(it) } ?: Result.Error(Exception("Words not found"))
    }

    override suspend fun getWord(wordId: String): Result<Word> {
        return _words?.get(wordId)?.let { Result.Success(it) } ?: Result.Error(Exception("Word with id = $wordId not found"))
    }

    override suspend fun getRemindWords(): Result<List<Word>> {
        val remindWords = words?.let { it.filter { word -> word.isRemind } }
        return remindWords?.let { Result.Success(it) } ?: Result.Error(Exception("Words not found"))
    }

    override suspend fun saveWord(word: Word) {
        _words?.put(word.id, word)
    }

    override suspend fun saveWords(words: List<Word>) {
        _words?.putAll(words.associateBy { it.id })
    }

    override suspend fun updateWords(words: List<Word>) {
        _words?.putAll(words.associateBy { it.id })
    }

    override suspend fun deleteWords(ids: List<String>) {
        _words?.keys?.removeAll(ids.toSet())
    }

    override suspend fun clearWords() {
        _words?.clear()
    }
}