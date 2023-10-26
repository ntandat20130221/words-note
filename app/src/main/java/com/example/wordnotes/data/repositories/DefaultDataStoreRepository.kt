package com.example.wordnotes.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "com.example.wordnotes.data.repositories.datastore")

class DefaultDataStoreRepository(private val context: Context) : DataStoreRepository {

    override suspend fun putString(key: String, value: String) {
        val stringKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[stringKey] = value
        }
    }

    override suspend fun getString(key: String): String? {
        val stringKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[stringKey]
    }

    override suspend fun removeString(key: String) {
        val stringKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(stringKey)
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        val booleanKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[booleanKey] = value
        }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        val booleanKey = booleanPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[booleanKey]
    }
}