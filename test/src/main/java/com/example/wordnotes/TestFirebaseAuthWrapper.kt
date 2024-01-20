package com.example.wordnotes

import com.example.wordnotes.data.FirebaseAuthWrapper
import javax.inject.Inject

class TestFirebaseAuthWrapper @Inject constructor() : FirebaseAuthWrapper {
    override fun isLoggedIn(): Boolean = true
}