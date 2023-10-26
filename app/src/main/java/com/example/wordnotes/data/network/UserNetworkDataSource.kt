package com.example.wordnotes.data.network

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User

interface UserNetworkDataSource {

    suspend fun signUp(user: User): Result<User>

    suspend fun signIn(user: User): Result<User>
}