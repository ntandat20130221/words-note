package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource

class FakeUserNetworkDataSource : UserNetworkDataSource {
    private val _users: MutableMap<String, User> = mutableMapOf()

    override suspend fun signUp(user: User): Result<User> {
        return if (_users.values.any { it.email == user.email }) {
            Result.Error(Exception("The user with email already exist."))
        } else {
            _users[user.email] = user
            Result.Success(user.copy(id = user.email))
        }
    }

    override suspend fun signIn(user: User): Result<User> {
        return _users.values.find { it.email == user.email }?.let { foundUser ->
            if (foundUser.password == user.password) {
                Result.Success(user)
            } else {
                Result.Error(Exception("Incorrect email or password."))
            }
        }
            ?: Result.Error(Exception("The user doesn't exist."))
    }
}