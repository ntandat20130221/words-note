package com.example.wordnotes.data.repositories

import android.net.Uri
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultUserRepository(
    private val userNetworkDataSource: UserNetworkDataSource,
    private val dataStoreRepository: DataStoreRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    private val scope = CoroutineScope(ioDispatcher)

    var isSignedIn = false  // For testing

    override suspend fun signUp(user: User): Result<User> = withContext(ioDispatcher) {
        userNetworkDataSource.signUp(user).also { result ->
            if (result is Result.Success) {
                isSignedIn = true
                scope.launch { dataStoreRepository.setUser(result.data) }
            }
        }
    }

    override suspend fun signIn(user: User): Result<User> = withContext(ioDispatcher) {
        userNetworkDataSource.signIn(user).also { result ->
            if (result is Result.Success) {
                isSignedIn = true
                scope.launch { dataStoreRepository.setUser(result.data) }
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> = withContext(ioDispatcher) {
        userNetworkDataSource.resetPassword(email)
    }

    override suspend fun logOut(): Unit = withContext(ioDispatcher) {
        userNetworkDataSource.signOut()
        isSignedIn = false
        scope.launch { dataStoreRepository.clearUser() }
    }

    override suspend fun setUser(user: User, imageUri: Uri): Result<User> = withContext(ioDispatcher) {
        if (imageUri == Uri.EMPTY) {
            userNetworkDataSource.updateProfile(user)
        } else {
            userNetworkDataSource.updateProfileImage(imageUri, user)
        }.also { result ->
            if (result is Result.Success) {
                dataStoreRepository.setUser(result.data)
            }
        }
    }

    override suspend fun getUser(): Result<User> = withContext(ioDispatcher) {
        Result.Success(dataStoreRepository.getUser())
    }
}