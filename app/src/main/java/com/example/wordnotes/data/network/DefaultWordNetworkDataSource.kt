package com.example.wordnotes.data.network

import androidx.work.WorkManager
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class DefaultWordNetworkDataSource(private val workManager: WorkManager) : WordsNetworkDataSource {

    override suspend fun loadWords(onCompleted: (Result<List<Word>>) -> Unit) {
        Firebase.database.reference.get()
            .addOnSuccessListener { dataSnapshot ->
                val words = mutableListOf<Word>()
                for (word in dataSnapshot.children) {
                    word.getValue<Word>()?.let {
                        words.add(it)
                    }
                }
                onCompleted(Result.Success(words.sortedBy { it.timestamp }))
            }
            .addOnFailureListener {
                onCompleted(Result.Error(it))
            }
    }

    override suspend fun saveWords(words: List<Word>) {
        Firebase.database.reference.updateChildren(words.associateBy { it.id })
    }

    override suspend fun updateWord(word: Word) {
        Firebase.database.reference.updateChildren(mapOf(word.id to word))
    }

    override suspend fun deleteWords(ids: List<String>) {
        Firebase.database.reference.updateChildren(ids.associateWith { null })
    }

    override suspend fun remindWords(ids: List<String>) {
        Firebase.database.reference.updateChildren(ids.associate { "$it/remind" to true })
    }
}