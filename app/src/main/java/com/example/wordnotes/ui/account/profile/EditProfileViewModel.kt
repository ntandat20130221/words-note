package com.example.wordnotes.ui.account.profile

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val USER_KEY = "user"
const val PROFILE_IMAGE_KEY = "profile_image"

data class EditProfileUiState(
    val user: User = User(),
    val imageUri: Uri? = null,
    val isCommitting: Boolean = false,
    val isCommitSuccessful: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState: MutableStateFlow<EditProfileUiState> = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        if (savedStateHandle.contains(USER_KEY) ||
            savedStateHandle.contains(PROFILE_IMAGE_KEY)
        ) {
            initFromSavedState()
        } else {
            initFromRepository()
        }
    }

    private fun initFromSavedState() {
        savedStateHandle.get<User>(USER_KEY)?.let {
            _uiState.update { currentState ->
                currentState.copy(user = it)
            }
        }

        savedStateHandle.get<Uri>(PROFILE_IMAGE_KEY)?.let { uri ->
            _uiState.update { it.copy(imageUri = uri) }
        }
    }

    private fun initFromRepository() {
        viewModelScope.launch {
            userRepository.getUser().let { result ->
                if (result is Result.Success) {
                    _uiState.update { currentState ->
                        currentState.copy(user = result.data)
                    }
                }
            }
        }
    }

    fun updateProfile(onUpdate: (User) -> User) {
        _uiState.update { currentState ->
            currentState.copy(user = onUpdate(currentState.user)).also {
                savedStateHandle[USER_KEY] = it.user
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(imageUri = imageUri).also {
                savedStateHandle[PROFILE_IMAGE_KEY] = imageUri
            }
        }
    }

    fun commitChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommitting = true) }
            if (userRepository.setUser(uiState.value.user, uiState.value.imageUri) is Result.Success) {
                _uiState.update { it.copy(isCommitSuccessful = true, isCommitting = false) }
            }
        }
    }

    fun getUserGender() = uiState.value.user.gender
}