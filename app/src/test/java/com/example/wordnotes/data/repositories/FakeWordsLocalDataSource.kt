package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.local.WordsLocalDataSource
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

class FakeWordsLocalDataSource(initialWords: List<Word>? = emptyList()) : WordsLocalDataSource {
    private var _words: MutableMap<String, Word>? = null

    private var words: List<Word>?
        get() = _words?.values?.toList()
        set(value) {
            _words = value?.associateBy { it.id }?.toMutableMap()
        }

    init {
        words = initialWords
    }

    override fun observeWords(): Flow<Result<List<Word>>> {
        TODO("Not yet implemented")
    }

    override fun observeWord(wordId: String): Flow<Result<Word>> {
        TODO("Not yet implemented")
    }

    override suspend fun getWords(): Result<List<Word>> {
        words?.let { return Result.Success(it) }
        return Result.Error(Exception("Words not found"))
    }

    override suspend fun getWord(wordId: String): Result<Word> {
        _words?.get(wordId)?.let { Result.Success(it) }
        return Result.Error(Exception("Word with id = $wordId not found"))
    }

    override suspend fun saveWord(word: Word) {
        _words?.put(word.id, word)
    }

    override suspend fun updateWord(word: Word) {
        _words?.put(word.id, word)
    }

    override suspend fun deleteWords(id: List<String>) {
        _words?.keys?.removeAll(id.toSet())
    }
}