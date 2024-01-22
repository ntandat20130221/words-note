package com.example.wordnotes.mocks

import com.example.wordnotes.data.FirebaseAuthWrapper
import javax.inject.Inject

class TestFirebaseAuthWrapper @Inject constructor(private val fakeUserRepository: FakeUserRepository) : FirebaseAuthWrapper {
    override fun isLoggedIn(): Boolean = fakeUserRepository.currentUser != null
}

class TestFirebaseAuthWrapperLogged @Inject constructor() : FirebaseAuthWrapper {
    override fun isLoggedIn(): Boolean = true
}

class TestFirebaseAuthWrapperNotLogged @Inject constructor() : FirebaseAuthWrapper {
    override fun isLoggedIn(): Boolean = false
}
