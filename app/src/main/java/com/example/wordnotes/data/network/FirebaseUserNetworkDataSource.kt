package com.example.wordnotes.data.network

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.USERS_PATH
import com.example.wordnotes.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirebaseUserNetworkDataSource : UserNetworkDataSource {

    override suspend fun signUp(user: User): Result<User> {
        return try {
            val authResult = Firebase.auth.createUserWithEmailAndPassword(user.email, user.password).await()
            val newUser = user.copy(id = authResult.user!!.uid)
            Firebase.database.reference.child(USERS_PATH).child(newUser.id).setValue(newUser).await()
            Result.Success(newUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun signIn(user: User): Result<User> {
        return try {
            val authResult = Firebase.auth.signInWithEmailAndPassword(user.email, user.password).await()
            val currentUser = Firebase.database.reference.child(USERS_PATH).child(authResult.user!!.uid).get().await().getValue(User::class.java)!!
            Result.Success(currentUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}