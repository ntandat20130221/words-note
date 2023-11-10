package com.example.wordnotes.data.repositories

import android.net.Uri
import com.example.wordnotes.data.KEY_IS_SIGNED_IN
import com.example.wordnotes.data.KEY_USER_DOB
import com.example.wordnotes.data.KEY_USER_EMAIL
import com.example.wordnotes.data.KEY_USER_GENDER
import com.example.wordnotes.data.KEY_USER_ID
import com.example.wordnotes.data.KEY_USER_NAME
import com.example.wordnotes.data.KEY_USER_PASSWORD
import com.example.wordnotes.data.KEY_USER_PHONE
import com.example.wordnotes.data.KEY_USER_PROFILE_IMAGE
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.network.UserNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultUserRepository(
    private val userNetworkDataSource: UserNetworkDataSource,
    private val dataStoreRepository: DataStoreRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : UserRepository {
    private val scope = CoroutineScope(ioDispatcher)

    override suspend fun signUp(user: User): Result<User> = withContext(ioDispatcher) {
        userNetworkDataSource.signUp(user).also { result ->
            if (result is Result.Success) {
                dataStoreRepository.putBoolean(KEY_IS_SIGNED_IN, true)
                scope.launch { setUserInfo(result.data) }
            }
        }
    }

    override suspend fun signIn(user: User): Result<User> = withContext(ioDispatcher) {
        userNetworkDataSource.signIn(user).also { result ->
            if (result is Result.Success) {
                dataStoreRepository.putBoolean(KEY_IS_SIGNED_IN, true)
                scope.launch { setUserInfo(result.data) }
            }
        }
    }

    override suspend fun logOut(): Unit = withContext(ioDispatcher) {
        // TODO: LogOut using FirebaseAuth to sync with Realtime Database Rule (auth != null)
        //  One way to do this is disable log out when networking unavailable.
        dataStoreRepository.putBoolean(KEY_IS_SIGNED_IN, false)
        scope.launch { removeUserInfo() }
    }

    override suspend fun setUser(user: User, imageUri: Uri): Result<User> = withContext(ioDispatcher) {
        if (imageUri == Uri.EMPTY) {
            userNetworkDataSource.updateProfile(user)
        } else {
            userNetworkDataSource.updateProfileImage(imageUri, user)
        }.also { result ->
            if (result is Result.Success) {
                setUserInfo(result.data)
            }
        }
    }

    override suspend fun getUser(): Result<User> = withContext(ioDispatcher) {
        val id = async { dataStoreRepository.getString(KEY_USER_ID) }
        val username = async { dataStoreRepository.getString(KEY_USER_NAME) }
        val profileImage = async { dataStoreRepository.getString(KEY_USER_PROFILE_IMAGE) }
        val email = async { dataStoreRepository.getString(KEY_USER_EMAIL) }
        val password = async { dataStoreRepository.getString(KEY_USER_PASSWORD) }
        val phone = async { dataStoreRepository.getString(KEY_USER_PHONE) }
        val gender = async { dataStoreRepository.getInt(KEY_USER_GENDER) }
        val dob = async { dataStoreRepository.getLong(KEY_USER_DOB) }

        Result.Success(
            User(
                id = id.await() ?: "",
                username = username.await() ?: "",
                profileImageUrl = profileImage.await() ?: "",
                email = email.await() ?: "",
                password = password.await() ?: "",
                phone = phone.await() ?: "",
                gender = gender.await() ?: -1,
                dob = dob.await() ?: 0,
            )
        )
    }

    private suspend fun setUserInfo(user: User) {
        dataStoreRepository.putString(KEY_USER_ID, user.id)
        dataStoreRepository.putString(KEY_USER_NAME, user.username)
        dataStoreRepository.putString(KEY_USER_PROFILE_IMAGE, user.profileImageUrl)
        dataStoreRepository.putString(KEY_USER_EMAIL, user.email)
        dataStoreRepository.putString(KEY_USER_PASSWORD, user.password)
        dataStoreRepository.putString(KEY_USER_PHONE, user.phone)
        dataStoreRepository.putInt(KEY_USER_GENDER, user.gender)
        dataStoreRepository.putLong(KEY_USER_DOB, user.dob)
    }

    private suspend fun removeUserInfo() {
        dataStoreRepository.removeString(KEY_USER_ID)
        dataStoreRepository.removeString(KEY_USER_NAME)
        dataStoreRepository.removeString(KEY_USER_PROFILE_IMAGE)
        dataStoreRepository.removeString(KEY_USER_EMAIL)
        dataStoreRepository.removeString(KEY_USER_PASSWORD)
        dataStoreRepository.removeString(KEY_USER_PHONE)
        dataStoreRepository.removeInt(KEY_USER_GENDER)
        dataStoreRepository.removeLong(KEY_USER_DOB)
    }
}