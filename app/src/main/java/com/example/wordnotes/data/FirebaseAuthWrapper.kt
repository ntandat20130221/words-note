package com.example.wordnotes.data

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

interface FirebaseAuthWrapper {
    fun isLoggedIn(): Boolean
}

class FirebaseAuthWrapperImpl @Inject constructor() : FirebaseAuthWrapper {
    override fun isLoggedIn(): Boolean = FirebaseAuth.getInstance().currentUser != null
}