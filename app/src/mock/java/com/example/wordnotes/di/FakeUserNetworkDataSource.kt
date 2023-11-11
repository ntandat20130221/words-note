package com.example.wordnotes.di

import android.net.Uri
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

    override suspend fun resetPassword(email: String): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun signOut() {
        // Do nothing
    }

    override suspend fun updateProfile(user: User): Result<User> {
        return Result.Success(user)
    }

    override suspend fun updateProfileImage(imageUri: Uri, user: User): Result<User> {
        return Result.Success(user)
    }
}