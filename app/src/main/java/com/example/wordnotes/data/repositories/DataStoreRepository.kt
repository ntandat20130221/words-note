package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.model.User

interface DataStoreRepository {

    suspend fun setUser(user: User)

    suspend fun getUser(): User

    suspend fun clearUser()
}