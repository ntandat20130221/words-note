package com.example.wordnotes.ui.account.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.data.model.User
import com.example.wordnotes.mocks.FakeUserRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class EditProfileViewModelTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var editProfileViewModel: EditProfileViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    private val user = User(id = "1", username = "user1", email = "user1@gmail.com", password = "111111")

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpMock() {
        val mockUri = mockk<Uri>()
        mockkStatic(Uri::class)
        every { Uri.parse("content://path_to_file") } returns mockUri
    }

    @Before
    fun createViewModel() = runTest {
        userRepository = FakeUserRepository(testDispatcher = mainCoroutineRule.testDispatcher)
        userRepository.signIn(user)
        savedStateHandle = SavedStateHandle()
        editProfileViewModel = EditProfileViewModel(userRepository, savedStateHandle)
    }

    @Test
    fun `test first state`() = runTest {
        val uiState = editProfileViewModel.uiState.value
        assertThat(uiState.user).isEqualTo(user)
        assertThat(uiState.imageUri).isEqualTo(null)
        assertThat(uiState.isCommitting).isEqualTo(false)
        assertThat(uiState.isCommitSuccessful).isEqualTo(false)
    }

    @Test
    fun `update profile then check ui state`() = runTest {
        editProfileViewModel.updateProfile { currentUser ->
            currentUser.copy(username = "updated", phone = "0123456789")
        }
        val updatedUser = editProfileViewModel.uiState.value.user
        assertThat(updatedUser).isEqualTo(user.copy(username = "updated", phone = "0123456789"))
        assertThat(savedStateHandle.get<User>(USER_KEY)).isEqualTo(updatedUser)

        val imageUri = Uri.parse("content://path_to_file")
        editProfileViewModel.updateProfileImage(imageUri)
        val updatedImageUri = editProfileViewModel.uiState.value.imageUri
        assertThat(updatedImageUri).isEqualTo(imageUri)
        assertThat(savedStateHandle.get<Uri>(PROFILE_IMAGE_KEY)).isEqualTo(imageUri)
    }

    @Test
    fun `update profile then check ui state should successful`() = runTest {
        // Collect uiState in a background scope
        var uiState = EditProfileUiState()
        backgroundScope.launch(mainCoroutineRule.testDispatcher) {
            editProfileViewModel.uiState.collect { uiState = it }
        }

        // Update profile
        editProfileViewModel.updateProfile { currentUser ->
            currentUser.copy(username = "updated", phone = "0123456789")
        }
        editProfileViewModel.updateProfileImage(Uri.parse("content://path_to_file"))

        // Commit changes should successful
        editProfileViewModel.commitChanges()
        assertThat(uiState.isCommitSuccessful).isTrue()
    }
}