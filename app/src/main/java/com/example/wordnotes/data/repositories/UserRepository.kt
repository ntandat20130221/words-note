package com.example.wordnotes.data.repositories

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User

interface UserRepository {

    suspend fun signUp(user: User): Result<User>

    suspend fun signIn(user: User): Result<User>

    suspend fun resetPassword(email: String): Result<Unit>

    suspend fun logOut()

    suspend fun setUser(user: User, imageUri: Uri): Result<User>

    suspend fun getUser(): Result<User>
}