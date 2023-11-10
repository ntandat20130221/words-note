package com.example.wordnotes.data.network

import android.net.Uri
import com.example.wordnotes.data.PROFILE_IMAGES_PATH
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.USERS_PATH
import com.example.wordnotes.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class FirebaseUserNetworkDataSource : UserNetworkDataSource {

    override suspend fun signUp(user: User): Result<User> {
        return try {
            Firebase.auth.createUserWithEmailAndPassword(user.email, user.password).await()
            val newUser = user.copy(id = Firebase.auth.uid!!)
            Firebase.database.reference.child("$USERS_PATH/${newUser.id}").setValue(newUser).await()
            Result.Success(newUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signIn(user: User): Result<User> {
        return try {
            Firebase.auth.signInWithEmailAndPassword(user.email, user.password).await()
            val currentUser = Firebase.database.reference.child("$USERS_PATH/${Firebase.auth.uid!!}").get().await()
                .getValue(User::class.java)!!
            Result.Success(currentUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            Firebase.auth.sendPasswordResetEmail(email).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error()
        }
    }

    override suspend fun signOut() = Firebase.auth.signOut()

    override suspend fun updateProfile(user: User): Result<User> {
        return try {
            Firebase.database.reference.child("$USERS_PATH/${user.id}").setValue(user).await()
            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateProfileImage(imageUri: Uri, user: User): Result<User> {
        return try {
            val uploadTask = Firebase.storage.reference.child("$PROFILE_IMAGES_PATH/${user.id}").putFile(imageUri).await()
            val url = uploadTask.storage.downloadUrl.await().toString()
            val updatedUser = user.copy(profileImageUrl = url)
            Firebase.database.reference.child("$USERS_PATH/${updatedUser.id}").setValue(updatedUser).await()
            Result.Success(updatedUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}