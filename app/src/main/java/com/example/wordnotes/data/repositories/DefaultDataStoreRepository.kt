package com.example.wordnotes.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.wrapWithResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.example.wordnotes.datastore.User as ProtobufUser

val Context.userDataStore: DataStore<ProtobufUser> by dataStore(
    fileName = "user.pb",
    serializer = UserSerializer
)

class DefaultDataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : DataStoreRepository {

    override suspend fun setUser(user: User): Result<User> = wrapWithResult {
        with(user) {
            context.userDataStore.updateData { currentUser ->
                currentUser.toBuilder()
                    .setId(id)
                    .setUsername(username)
                    .setImageUrl(profileImageUrl)
                    .setEmail(email)
                    .setPassword(password)
                    .setPhone(phone)
                    .setGender(gender)
                    .setDob(dob)
                    .build()
            }
            this
        }
    }

    override suspend fun getUser(): Result<User> = wrapWithResult {
        context.userDataStore.data.first().let { userProto ->
            User(
                id = userProto.id,
                username = userProto.username,
                profileImageUrl = userProto.imageUrl,
                email = userProto.email,
                password = userProto.password,
                phone = userProto.phone,
                gender = userProto.gender,
                dob = userProto.dob
            )
        }
    }

    override suspend fun clearUser(): Result<Unit> = wrapWithResult {
        context.userDataStore.updateData { currentUser ->
            currentUser.toBuilder().clear().build()
        }
    }
}