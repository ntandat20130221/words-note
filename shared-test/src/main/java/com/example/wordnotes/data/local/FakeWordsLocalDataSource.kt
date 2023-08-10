package com.example.wordnotes.data.local

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow

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
        TODO("Not yet implemented")
    }

    override fun getWordStream(wordId: String): Flow<Result<Word>> {
        TODO("Not yet implemented")
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

    override suspend fun updateWord(word: Word) {
        _words?.put(word.id, word)
    }

    override suspend fun remindWords(ids: List<String>) {
        _words?.putAll(ids.map { it to _words?.get(it)!!.copy(isRemind = true) })
    }

    override suspend fun deleteWords(ids: List<String>) {
        _words?.keys?.removeAll(ids.toSet())
    }
}