package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource
import java.util.UUID

class FakeUserNetworkDataSource(
    initialUser: List<User> = listOf(
        User(
            id = UUID.randomUUID().toString(), "user1", "user1@gmail.com", "123456",
            "0123456789", "Male", "23/05/2000"
        ),
        User(
            id = UUID.randomUUID().toString(), "user2", "user2@gmail.com", "123456",
            "0123456789", "Female", "23/05/2000"
        )
    )
) : UserNetworkDataSource {
    private val _users: MutableMap<String, User> = mutableMapOf()

    init {
        _users.putAll(initialUser.associateBy { it.id })
    }

    override suspend fun signUp(user: User): Result<User> {
        return if (_users.values.any { it.email == user.email }) {
            Result.Error(Exception("The user with email already exist."))
        } else {
            val uuid = UUID.randomUUID().toString()
            _users[uuid] = user.copy(id = uuid)
            Result.Success(_users[uuid]!!)
        }
    }

    override suspend fun signIn(user: User): Result<User> {
        return _users.values.find { it.email == user.email }?.let { foundUser ->
            if (foundUser.password == user.password) {
                Result.Success(foundUser)
            } else {
                Result.Error(Exception("Incorrect email or password."))
            }
        }
            ?: Result.Error(Exception("The user doesn't exist."))
    }
}