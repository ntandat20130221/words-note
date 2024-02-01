package com.example.wordnotes.mocks

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.remote.UserRemoteDataSource
import com.example.wordnotes.data.wrapWithResult
import java.util.UUID
import javax.inject.Inject

class FakeUserRemoteDataSource(initialUsers: List<User>?) : UserRemoteDataSource {

    @Inject
    constructor() : this(
        listOf(
            User(
                id = "id_user1",
                username = "user1",
                imageUrl = "image1",
                email = "user1@gmail.com",
                password = "111111",
                phone = "0123456789",
                gender = 1,
                dob = 942220588000
            ),
            User(
                id = "id_user2",
                username = "user2",
                imageUrl = "image2",
                email = "user2@gmail.com",
                password = "222222",
                phone = "0123456789",
                gender = 0,
                dob = 900384131000
            )
        )
    )

    val users: MutableMap<String, User> = initialUsers?.associateBy { it.id }?.toMutableMap() ?: mutableMapOf()

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
            val updatedUser = if (imageUri == null)
                user else
                user.copy(imageUrl = "network_$imageUri")
            users[user.id] = updatedUser
            Result.Success(updatedUser)
        } else {
            Result.Error(Exception("The user doesn't exist."))
        }
    }
}