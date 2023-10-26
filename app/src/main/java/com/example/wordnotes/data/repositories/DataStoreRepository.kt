package com.example.wordnotes.data.repositories

interface DataStoreRepository {

    suspend fun putString(key: String, value: String)

    suspend fun getString(key: String): String?

    suspend fun removeString(key: String)

    suspend fun putBoolean(key: String, value: Boolean)

    suspend fun getBoolean(key: String): Boolean?
}