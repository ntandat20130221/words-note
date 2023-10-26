package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository

class FakeUserRepository(
    initialUsers: List<User> = listOf(
        User(id = "1", username = "user1", email = "user1@gmail.com", password = "123456"),
        User(id = "2", username = "user2", email = "user2@gmail.com", password = "123456"),
        User(id = "3", username = "user3", email = "user3@gmail.com", password = "123456")
    )
) : UserRepository {
    private val _users: MutableMap<String, User> = mutableMapOf()

    init {
        _users.putAll(initialUsers.associateBy { it.id })
    }

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

    override suspend fun logOut() {

    }

    override suspend fun getUser(): User {
        return User()
    }
}