package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWordRepository : WordRepository {
    private val _savedWords = MutableStateFlow(LinkedHashMap<String, Word>())
    private val savedWords: StateFlow<LinkedHashMap<String, Word>> = _savedWords.asStateFlow()

    private val observableWords: Flow<Result<List<Word>>> = savedWords.map { Result.Success(it.values.toList()) }

    override fun observeWords(): Flow<Result<List<Word>>> = observableWords

    override fun observeWord(wordId: String): Flow<Result<Word>> {
        return observableWords.map { result ->
            when (result) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(result.exception)
                is Result.Success -> {
                    val word = result.data.firstOrNull { it.id == wordId } ?: return@map Result.Error(Exception("Word not found"))
                    Result.Success(word)
                }
            }
        }
    }

    override suspend fun getWords(): Result<List<Word>> = observableWords.first()

    override suspend fun getWord(wordId: String): Result<Word> {
        return _savedWords.value[wordId]?.let { Result.Success(it) } ?: Result.Error(Exception("Word not found"))
    }

    override suspend fun saveWord(word: Word) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it[word.id] = word
            }
        }
    }

    override suspend fun updateWord(word: Word) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it[word.id] = word
            }
        }
    }

    override suspend fun deleteWords(id: List<String>) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it.keys.removeAll(id.toSet())
            }
        }
    }
}