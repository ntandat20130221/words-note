package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.DataStoreRepository

class FakeDataStoreRepository : DataStoreRepository {
    private var user: User? = null

    override suspend fun setUser(user: User) {
        this.user = user
    }

    override suspend fun getUser(): User = user ?: User()

    override suspend fun clearUser() {
        user = null
    }
}