package com.example.wordnotes.ui.account

import com.example.wordnotes.sharedtest.FakeUserRepository
import com.example.wordnotes.sharedtest.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule

@ExperimentalCoroutinesApi
class AccountViewModelTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var accountViewModel: AccountViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createViewModel() {
        userRepository = FakeUserRepository(testDispatcher = mainCoroutineRule.testDispatcher)
        accountViewModel = AccountViewModel(userRepository)
    }
}