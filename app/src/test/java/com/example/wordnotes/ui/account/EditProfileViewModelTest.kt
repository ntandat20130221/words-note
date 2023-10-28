package com.example.wordnotes.ui.account

import com.example.wordnotes.data.model.User
import com.example.wordnotes.sharedtest.FakeUserRepository
import com.example.wordnotes.sharedtest.MainCoroutineRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class EditProfileViewModelTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var editProfileViewModel: EditProfileViewModel

    private val user = User(id = "1", username = "user1", email = "user1@gmail.com", password = "123456")

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun createViewModel() = runTest {
        userRepository = FakeUserRepository(testDispatcher = mainCoroutineRule.testDispatcher)
        userRepository.signIn(user)
        editProfileViewModel = EditProfileViewModel(userRepository)
    }

    @Test
    fun `test first state`() = runTest {
        val currentUser = editProfileViewModel.uiState.first().user
        assertThat(currentUser).isEqualTo(user)
    }

    @Test
    fun `update profile then check ui state`() = runTest {
        editProfileViewModel.updateProfile { currentUser ->
            currentUser.copy(username = "updated")
        }
        val user1 = editProfileViewModel.uiState.value.user
        assertThat(user1.username).isEqualTo("updated")

        editProfileViewModel.updateProfile { currentUser ->
            currentUser.copy(email = "updated@gmail.com")
        }
        val user2 = editProfileViewModel.uiState.value.user
        assertThat(user2.email).isEqualTo("updated@gmail.com")
    }
}