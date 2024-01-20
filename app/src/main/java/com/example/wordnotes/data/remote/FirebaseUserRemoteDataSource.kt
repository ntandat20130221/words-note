package com.example.wordnotes.data.remote

import android.net.Uri
import com.example.wordnotes.data.PROFILE_IMAGES_PATH
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.USERS_PATH
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.wrapWithResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRemoteDataSource @Inject constructor() : UserRemoteDataSource {

    override suspend fun signUp(user: User): Result<User> = wrapWithResult {
        Firebase.auth.createUserWithEmailAndPassword(user.email, user.password).await()
        val newUser = user.copy(id = Firebase.auth.uid!!)
        Firebase.database.reference.child("$USERS_PATH/${newUser.id}").setValue(newUser).await()
        newUser
    }

    override suspend fun signIn(user: User): Result<User> = wrapWithResult {
        Firebase.auth.signInWithEmailAndPassword(user.email, user.password).await()
        Firebase.database.reference.child("$USERS_PATH/${Firebase.auth.uid!!}").get()
            .await().getValue(User::class.java)!!
    }

    override suspend fun resetPassword(email: String): Result<Unit> = wrapWithResult { Firebase.auth.sendPasswordResetEmail(email) }

    override suspend fun signOut(): Result<Unit> = wrapWithResult { Firebase.auth.signOut() }

    override suspend fun updateProfile(user: User): Result<User> = wrapWithResult {
        Firebase.database.reference.child("$USERS_PATH/${user.id}").setValue(user).await()
        user
    }

    override suspend fun updateProfileImage(imageUri: Uri, user: User): Result<User> = wrapWithResult {
        val uploadTask = Firebase.storage.reference.child("$PROFILE_IMAGES_PATH/${user.id}").putFile(imageUri).await()
        val url = uploadTask.storage.downloadUrl.await().toString()
        with(user.copy(profileImageUrl = url)) {
            Firebase.database.reference.child("$USERS_PATH/${id}").setValue(this).await()
            this
        }
    }
}