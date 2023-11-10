package com.example.wordnotes.data.network

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User

interface UserNetworkDataSource {

    suspend fun signUp(user: User): Result<User>

    suspend fun signIn(user: User): Result<User>

    suspend fun updateProfile(user: User): Result<User>

    suspend fun updateProfileImage(imageUri: Uri, user: User): Result<User>
}