package com.example.wordnotes.ui.auth

import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.R
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.User
import com.example.wordnotes.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignInUiState(
    val isSignInSuccess: Boolean = false,
    val isRequesting: Boolean = false,
    val message: Int? = null
)

class SignInViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState: MutableStateFlow<SignInUiState> = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (checkValidity(email, password)) {
            _uiState.update { SignInUiState(isRequesting = true) }
            viewModelScope.launch {
                when (userRepository.signIn(User(email = email, password = password))) {
                    is Result.Success -> _uiState.update { SignInUiState(isSignInSuccess = true, isRequesting = false) }
                    is Result.Error -> _uiState.update { SignInUiState(message = R.string.authentication_failed, isRequesting = false) }
                    else -> _uiState.update { SignInUiState(isRequesting = false) }
                }
            }
        }
    }

    private fun checkValidity(email: String, password: String): Boolean {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { SignInUiState(message = R.string.please_complete_all_information) }
            return false
        } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { SignInUiState(message = R.string.the_email_address_isnt_in_the_correct_format) }
            return false
        }
        return true
    }

    fun snakeBarShown() {
        _uiState.update { SignInUiState(message = null) }
    }
}