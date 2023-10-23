package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User

interface UserRepository {
    suspend fun signUp(user: User): Result<User>
    suspend fun signIn(user: User): Result<User>
}