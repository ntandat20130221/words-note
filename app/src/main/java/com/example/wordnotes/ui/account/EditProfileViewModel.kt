package com.example.wordnotes.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val user: User = User()
)

class EditProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<EditProfileUiState> = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getUser().let { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    var genderIndex = 0

    fun updateProfile(onUpdate: (User) -> User) {
        _uiState.update { currentState ->
            currentState.copy(user = onUpdate(currentState.user))
        }
    }

    fun commitChange() {

    }
}