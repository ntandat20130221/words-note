package com.example.wordnotes.data.repositories

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultUserRepository(
    private val userNetworkDataSource: UserNetworkDataSource,
    private val dataStoreRepository: DataStoreRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {

    override suspend fun signUp(user: User): Result<User> = withContext(ioDispatcher) {
        userNetworkDataSource.signUp(user).also { result ->
            if (result is Result.Success) {
                dataStoreRepository.putBoolean(KEY_IS_SIGNED_IN, true)
                dataStoreRepository.putString(KEY_USER_ID, result.data.id)
            }
        }
    }

    override suspend fun signIn(user: User): Result<User> = withContext(ioDispatcher) {
        userNetworkDataSource.signIn(user).also { result ->
            if (result is Result.Success) {
                dataStoreRepository.putBoolean(KEY_IS_SIGNED_IN, true)
                dataStoreRepository.putString(KEY_USER_ID, result.data.id)
            }
        }
    }
}