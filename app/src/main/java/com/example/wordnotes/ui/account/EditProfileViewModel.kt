package com.example.wordnotes.ui.account

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val user: User = User(),
    val imageUri: Uri = Uri.EMPTY,
    val isCommitting: Boolean = false,
    val isCommitSuccessful: Boolean = false,
)

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState: MutableStateFlow<EditProfileUiState> = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    var genderIndex = -1

    init {
        if (savedStateHandle.contains(USER_SAVED_STATE_KEY) || savedStateHandle.contains(PROFILE_IMAGE_SAVED_STATE_KEY)) {
            initFromSavedState()
        } else {
            initFromRepository()
        }
    }

    private fun initFromSavedState() {
        savedStateHandle.get<User>(USER_SAVED_STATE_KEY)?.let {
            _uiState.update { currentState ->
                currentState.copy(user = it).also {
                    genderIndex = it.user.gender
                }
            }
        }

        savedStateHandle.get<Uri>(PROFILE_IMAGE_SAVED_STATE_KEY)?.let { uri ->
            _uiState.update { it.copy(imageUri = uri) }
        }
    }

    private fun initFromRepository() {
        viewModelScope.launch {
            userRepository.getUser().let { result ->
                if (result is Result.Success) {
                    _uiState.update { currentState ->
                        currentState.copy(user = result.data).also {
                            genderIndex = it.user.gender
                        }
                    }
                }
            }
        }
    }

    fun updateProfile(onUpdate: (User) -> User) {
        _uiState.update { currentState ->
            currentState.copy(user = onUpdate(currentState.user)).also {
                savedStateHandle[USER_SAVED_STATE_KEY] = it.user
            }
        }
    }

    fun updateProfileImage(imageUri: Uri) {
        _uiState.update { currentState ->
            currentState.copy(imageUri = imageUri).also {
                savedStateHandle[PROFILE_IMAGE_SAVED_STATE_KEY] = imageUri
            }
        }
    }

    fun commitChanges() {
        _uiState.update { it.copy(isCommitting = true) }
        viewModelScope.launch {
            if (userRepository.setUser(uiState.value.user, uiState.value.imageUri) is Result.Success) {
                _uiState.update { it.copy(isCommitting = false, isCommitSuccessful = true) }
            }
        }
    }
}

const val USER_SAVED_STATE_KEY = "USER_SAVED_STATE_KEY"
const val PROFILE_IMAGE_SAVED_STATE_KEY = "PROFILE_IMAGE_SAVED_STATE_KEY"