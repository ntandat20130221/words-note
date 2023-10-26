package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.repositories.DataStoreRepository

class FakeDataStoreRepository : DataStoreRepository {
    private val _dataStore: MutableMap<String, Any> = mutableMapOf()

    override suspend fun putString(key: String, value: String) {
        _dataStore[key] = value
    }

    override suspend fun getString(key: String): String? {
        return _dataStore[key] as String?
    }

    override suspend fun removeString(key: String) {
        _dataStore.remove(key)
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        _dataStore[key] = value
    }

    override suspend fun getBoolean(key: String): Boolean? {
        return _dataStore[key] as Boolean?
    }
}