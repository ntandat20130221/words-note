package com.example.wordnotes.di

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource

class FakeUserNetworkDataSource : UserNetworkDataSource {
    override suspend fun signUp(user: User): Result<User> {
        return Result.Success(user)
    }

    override suspend fun signIn(user: User): Result<User> {
        return Result.Success(user)
    }
}