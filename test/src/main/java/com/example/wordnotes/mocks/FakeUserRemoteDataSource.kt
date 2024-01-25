package com.example.wordnotes.mocks

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.remote.UserRemoteDataSource
import com.example.wordnotes.data.wrapWithResult
import java.util.UUID
import javax.inject.Inject

class FakeUserRemoteDataSource @Inject constructor() : UserRemoteDataSource {

    private val initialUsers = listOf(
        User(
            id = UUID.randomUUID().toString(), "user1", "image1", "user1@gmail.com",
            "111111", "0123456789", 1, 942220588000
        ),
        User(
            id = UUID.randomUUID().toString(), "user2", "image2", "user2@gmail.com",
            "222222", "0123456789", 0, 900384131000
        )
    )

    private val users: MutableMap<String, User> = initialUsers.associateBy { it.id }.toMutableMap()

    override suspend fun signUp(user: User): Result<User> {
        return if (users.values.any { it.email == user.email }) {
            Result.Error(Exception("The user with email already exist."))
        } else {
            user.copy(id = UUID.randomUUID().toString()).let { newUser ->
                users[newUser.id] = newUser
                Result.Success(newUser)
            }
        }
    }

    override suspend fun signIn(user: User): Result<User> {
        return users.values.find { it.email == user.email }?.let { foundUser ->
            if (foundUser.password == user.password) {
                Result.Success(foundUser)
            } else {
                Result.Error(Exception("Incorrect email or password."))
            }
        }
            ?: Result.Error(Exception("The user doesn't exist."))
    }

    override suspend fun resetPassword(email: String): Result<Unit> = wrapWithResult {
        users.values.find { it.email == email }?.let { foundUser ->
            users[foundUser.id] = foundUser.copy(password = "123456")
            Result.Success(Unit)
        }
    }

    override suspend fun signOut(): Result<Unit> = wrapWithResult { }

    override suspend fun updateProfile(user: User, imageUri: Uri?): Result<User> {
        return if (users.contains(user.id)) {
            users[user.id] = if (imageUri == null)
                user else
                user.copy(imageUrl = imageUri.toString())
            Result.Success(user)
        } else {
            Result.Error(Exception("The user doesn't exist."))
        }
    }
}