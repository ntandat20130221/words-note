package com.example.wordnotes.sharedtest

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FakeUserRepository(
    initialUsers: List<User> = listOf(
        User(id = "1", username = "user1", email = "user1@gmail.com", password = "123456"),
        User(id = "2", username = "user2", email = "user2@gmail.com", password = "123456"),
        User(id = "3", username = "user3", email = "user3@gmail.com", password = "123456")
    ),
    private val testDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    private val _users: MutableMap<String, User> = mutableMapOf()
    private var currentUser: User? = null

    init {
        _users.putAll(initialUsers.associateBy { it.id })
    }

    override suspend fun signUp(user: User): Result<User> = withContext(testDispatcher) {
        if (_users.values.any { it.email == user.email }) {
            Result.Error(Exception("The user with email already exist."))
        } else {
            _users[user.email] = user
            Result.Success(user.copy(id = user.email).also { currentUser = it })
        }
    }

    override suspend fun signIn(user: User): Result<User> = withContext(testDispatcher) {
        _users.values.find { it.email == user.email }?.let { foundUser ->
            if (foundUser.password == user.password) {
                Result.Success(foundUser.also { currentUser = it })
            } else {
                Result.Error(Exception("Incorrect email or password."))
            }
        }
            ?: Result.Error(Exception("The user doesn't exist."))
    }

    override suspend fun logOut() {
        withContext(testDispatcher) {
            currentUser = null
        }
    }

    override suspend fun getUser(): Result<User> = withContext(testDispatcher) {
        currentUser?.let { Result.Success(it) } ?: Result.Error()
    }
}