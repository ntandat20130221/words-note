package com.example.wordnotes.data.repositories

interface DataStoreRepository {

    suspend fun putString(key: String, value: String)

    suspend fun getString(key: String): String?

    suspend fun removeString(key: String)

    suspend fun putInt(key: String, value: Int)

    suspend fun getInt(key: String): Int?

    suspend fun removeInt(key: String)

    suspend fun putLong(key: String, value: Long)

    suspend fun getLong(key: String): Long?

    suspend fun removeLong(key: String)

    suspend fun putBoolean(key: String, value: Boolean)

    suspend fun getBoolean(key: String): Boolean?
}