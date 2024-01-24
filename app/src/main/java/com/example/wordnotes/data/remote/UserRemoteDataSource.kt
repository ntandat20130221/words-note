package com.example.wordnotes.data.remote

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User

interface UserRemoteDataSource {

    suspend fun signUp(user: User): Result<User>

    suspend fun signIn(user: User): Result<User>

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun signOut(): Result<Unit>

    suspend fun updateProfile(user: User, imageUri: Uri): Result<User>
}