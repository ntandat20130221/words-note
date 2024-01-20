package com.example.wordnotes.mocks

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository
import com.example.wordnotes.data.wrapWithResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepository(private val testDispatcher: CoroutineDispatcher) : UserRepository {

    @Inject
    constructor() : this(testDispatcher = Dispatchers.IO)

    private val initialUsers = listOf(
        User(id = "1", username = "user1", email = "user1@gmail.com", password = "111111"),
        User(id = "2", username = "user2", email = "user2@gmail.com", password = "222222"),
        User(id = "3", username = "user3", email = "user3@gmail.com", password = "333333")
    )

    private val users: MutableMap<String, User> = initialUsers.associateBy { it.id }.toMutableMap()
    var currentUser: User? = null

    override suspend fun signUp(user: User): Result<User> = withContext(testDispatcher) {
        if (users.values.any { it.email == user.email }) {
            Result.Error(Exception("The user with email already exist."))
        } else {
            users[user.email] = user
            Result.Success(user.copy(id = user.email).also { currentUser = it })
        }
    }

    override suspend fun signIn(user: User): Result<User> = withContext(testDispatcher) {
        users.values.find { it.email == user.email }?.let { foundUser ->
            if (foundUser.password == user.password) {
                Result.Success(foundUser.also { currentUser = it })
            } else {
                Result.Error(Exception("Incorrect email or password."))
            }
        }
            ?: Result.Error(Exception("The user doesn't exist."))
    }

    override suspend fun resetPassword(email: String): Result<Unit> = withContext(testDispatcher) {
        wrapWithResult {
            users.values.find { it.email == email }?.let { foundUser ->
                users[foundUser.id] = foundUser.copy(password = "123456")
            }
            Unit
        }
    }

    override suspend fun logOut() {
        withContext(testDispatcher) {
            currentUser = null
        }
    }

    override suspend fun setUser(user: User, imageUri: Uri): Result<User> = withContext(testDispatcher) {
        currentUser = user
        Result.Success(user)
    }

    override suspend fun getUser(): Result<User> = withContext(testDispatcher) {
        wrapWithResult { currentUser ?: throw NullPointerException() }
    }
}