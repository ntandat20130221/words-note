package com.example.wordnotes.ui.auth

import com.example.wordnotes.R
import com.example.wordnotes.sharedtest.FakeUserRepository
import com.example.wordnotes.sharedtest.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SignInViewModelTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var signInViewModel: SignInViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        userRepository = FakeUserRepository(testDispatcher = mainCoroutineRule.testDispatcher)
        signInViewModel = SignInViewModel(userRepository)
    }

    @Test
    fun `check initial state`() = runTest {
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
        assertThat(signInViewModel.uiState.value.isRequesting).isFalse()
        assertThat(signInViewModel.uiState.value.message).isNull()
    }

    @Test
    fun `enter correct email and password then signing in successfully`() = runTest {
        signInViewModel.signIn(email = "user1@gmail.com", password = "123456")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isTrue()
    }

    @Test
    fun `enter wrong email then signing in failed`() = runTest {
        signInViewModel.signIn(email = "user12@gmail.com", password = "123456")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
    }

    @Test
    fun `enter wrong password then signing in failed`() = runTest {
        signInViewModel.signIn(email = "user1@gmail.com", password = "1234567")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
    }

    @Test
    fun `enter incomplete information then signing in failed and showing error message`() = runTest {
        signInViewModel.signIn(email = "", password = "123456")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
        assertThat(signInViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)

        signInViewModel.signIn(email = "john@", password = "")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
        assertThat(signInViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)

        signInViewModel.signIn(email = "", password = "")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
        assertThat(signInViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)
    }

    @Test
    fun `enter wrong email format then signing in failed and showing error message`() = runTest {
        signInViewModel.signIn(email = "john@", password = "123456")
        assertThat(signInViewModel.uiState.value.isSignInSuccess).isFalse()
        assertThat(signInViewModel.uiState.value.message).isEqualTo(R.string.the_email_address_isnt_in_the_correct_format)
    }
}