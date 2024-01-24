package com.example.wordnotes.ui.account

import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.data.model.User
import com.example.wordnotes.mocks.FakeUserRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AccountViewModelTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var accountViewModel: AccountViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createViewModel() = runTest {
        userRepository = FakeUserRepository(testDispatcher = mainCoroutineRule.testDispatcher)
        userRepository.signIn(User(email = "user1@gmail.com", password = "111111"))
        accountViewModel = AccountViewModel(userRepository)
    }

    @Test
    fun `test initial state`() = runTest {
        assertThat(accountViewModel.uiState.value.user).isEqualTo(
            User(
                id = "1",
                username = "user1",
                email = "user1@gmail.com",
                password = "111111"
            )
        )
        assertThat(accountViewModel.uiState.value.isLogOut).isFalse()
    }

    @Test
    fun `log out then check ui state`() = runTest {
        accountViewModel.logOut()
        assertThat(accountViewModel.uiState.value.isLogOut).isTrue()
    }
}