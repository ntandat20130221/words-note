package com.example.wordnotes.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.example.wordnotes.datastore.User
import kotlinx.coroutines.flow.first

val Context.userDataStore: DataStore<User> by dataStore(
    fileName = "user.pb",
    serializer = UserSerializer
)

class DefaultDataStoreRepository(private val context: Context) : DataStoreRepository {

    override suspend fun setUser(user: com.example.wordnotes.data.model.User) {
        context.userDataStore.updateData { currentUser ->
            currentUser.toBuilder()
                .setId(user.id)
                .setUsername(user.username)
                .setImageUrl(user.profileImageUrl)
                .setEmail(user.email)
                .setPassword(user.password)
                .setPhone(user.phone)
                .setGender(user.gender)
                .setDob(user.dob)
                .build()
        }
    }

    override suspend fun getUser(): com.example.wordnotes.data.model.User {
        val userProto = context.userDataStore.data.first()
        return com.example.wordnotes.data.model.User(
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

    override suspend fun clearUser() {
        context.userDataStore.updateData { currentUser ->
            currentUser.toBuilder().clear().build()
        }
    }
}