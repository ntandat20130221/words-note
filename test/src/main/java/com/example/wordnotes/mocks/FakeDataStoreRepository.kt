package com.example.wordnotes.mocks

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.DataStoreRepository
import com.example.wordnotes.data.wrapWithResult
import javax.inject.Inject

class FakeDataStoreRepository @Inject constructor() : DataStoreRepository {
    private var user: User? = null

    override suspend fun setUser(user: User): Result<User> = wrapWithResult { user.also { this.user = it } }

    override suspend fun getUser(): Result<User> = wrapWithResult { user!! }

    override suspend fun clearUser(): Result<Unit> = wrapWithResult { user = null }

    override suspend fun clear(): Result<Unit> = wrapWithResult {
        user = null
    }
}