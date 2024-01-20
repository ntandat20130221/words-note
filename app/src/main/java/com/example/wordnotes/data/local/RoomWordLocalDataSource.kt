package com.example.wordnotes.data.local

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.wrapWithResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomWordLocalDataSource @Inject constructor(private val wordDao: WordDao) : WordLocalDataSource {

    override fun getWordsFlow(): Flow<List<Word>> = wordDao.getWordsStream()

    override fun getWordFlow(wordId: String): Flow<Word> = wordDao.getWordStream(wordId)

    override suspend fun getWords(): Result<List<Word>> = wrapWithResult { wordDao.getWords() }

    override suspend fun getWord(wordId: String): Result<Word> = wrapWithResult { wordDao.getWord(wordId) }

    override suspend fun getRemindingWords(): Result<List<Word>> = wrapWithResult { wordDao.getRemindWords() }

    override suspend fun saveWord(word: Word): Result<Unit> = wrapWithResult { wordDao.insertWord(word) }

    override suspend fun saveWords(words: List<Word>): Result<Unit> = wrapWithResult { wordDao.insertWords(words) }

    override suspend fun updateWords(words: List<Word>): Result<Unit> = wrapWithResult { wordDao.updateWords(words) }

    override suspend fun deleteWords(ids: List<String>): Result<Unit> = wrapWithResult { wordDao.deleteWords(ids) }

    override suspend fun clearWords(): Result<Unit> = wrapWithResult { wordDao.clearWords() }
}