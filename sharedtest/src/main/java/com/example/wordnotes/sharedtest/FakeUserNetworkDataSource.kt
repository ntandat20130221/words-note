package com.example.wordnotes.sharedtest

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource
import java.util.UUID

class FakeUserNetworkDataSource(
    initialUser: List<User> = listOf(
        User(
            id = UUID.randomUUID().toString(), "user1", "image1", "user1@gmail.com",
            "123456", "0378988098", 1, 942220588000
        ),
        User(
            id = UUID.randomUUID().toString(), "user2", "image2", "user2@gmail.com",
            "123456", "0378988098", 0, 900384131000
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

    override suspend fun resetPassword(email: String): Result<Unit> {
        return _users.values.find { it.email == email }?.let { foundUser ->
            _users[foundUser.id] = foundUser.copy(password = "123456")
            Result.Success(Unit)
        }
            ?: Result.Error()
    }

    override suspend fun signOut() {
        // Do nothing
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return if (_users.contains(user.id)) {
            _users[user.id] = user
            Result.Success(user)
        } else {
            Result.Error(Exception("The user doesn't exist."))
        }
    }

    override suspend fun updateProfileImage(imageUri: Uri, user: User): Result<User> {
        return if (_users.contains(user.id)) {
            _users[user.id] = user.copy(profileImageUrl = imageUri.toString())
            Result.Success(user)
        } else {
            Result.Error(Exception("The user doesn't exist."))
        }
    }
}