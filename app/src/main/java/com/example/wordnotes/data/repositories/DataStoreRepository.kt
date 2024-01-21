package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User

interface DataStoreRepository {

    suspend fun setUser(user: User): Result<User>

    suspend fun getUser(): Result<User>

    suspend fun clearUser(): Result<Unit>

    suspend fun clear(): Result<Unit>
}