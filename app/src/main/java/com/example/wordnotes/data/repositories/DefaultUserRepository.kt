package com.example.wordnotes.data.repositories

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.remote.UserRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultUserRepository(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val dataStoreRepository: DataStoreRepository,
    private val dispatcher: CoroutineDispatcher
) : UserRepository {

    @Inject
    constructor(
        userRemoteDataSource: UserRemoteDataSource,
        dataStoreRepository: DataStoreRepository
    ) : this(userRemoteDataSource, dataStoreRepository, Dispatchers.IO)

    private val scope = CoroutineScope(dispatcher)

    override suspend fun signUp(user: User): Result<User> = withContext(dispatcher) {
        userRemoteDataSource.signUp(user).also { result ->
            if (result is Result.Success) {
                scope.launch { dataStoreRepository.setUser(result.data) }
            }
        }
    }

    override suspend fun signIn(user: User): Result<User> = withContext(dispatcher) {
        userRemoteDataSource.signIn(user).also { result ->
            if (result is Result.Success) {
                scope.launch { dataStoreRepository.setUser(result.data) }
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> = withContext(dispatcher) {
        userRemoteDataSource.resetPassword(email)
    }

    override suspend fun logOut(): Result<Unit> = withContext(dispatcher) {
        userRemoteDataSource.signOut().also { result ->
            if (result is Result.Success) {
                scope.launch { dataStoreRepository.clearUser() }
            }
        }
    }

    override suspend fun setUser(user: User, imageUri: Uri): Result<User> = withContext(dispatcher) {
        if (imageUri == Uri.EMPTY) {
            userRemoteDataSource.updateProfile(user)
        } else {
            userRemoteDataSource.updateProfileImage(imageUri, user)
        }.also { result ->
            if (result is Result.Success) {
                dataStoreRepository.setUser(result.data)
            }
        }
    }

    override suspend fun getUser(): Result<User> = withContext(dispatcher) {
        dataStoreRepository.getUser()
    }
}