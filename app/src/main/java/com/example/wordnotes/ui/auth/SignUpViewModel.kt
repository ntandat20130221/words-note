package com.example.wordnotes.ui.auth

import androidx.core.util.PatternsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.R
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

data class SignUpUiState(
    val isSignUpSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val message: Int? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState: MutableStateFlow<SignUpUiState> = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUp(username: String, email: String, password: String, confirmedPassword: String) {
        if (checkValidity(username, email, password, confirmedPassword)) {
            viewModelScope.launch {
                _uiState.update { SignUpUiState(isLoading = true) }
                when (userRepository.signUp(User(username = username, email = email, password = password))) {
                    is Result.Success -> _uiState.update { SignUpUiState(isSignUpSuccess = true, isLoading = false) }
                    is Result.Error -> _uiState.update { SignUpUiState(message = R.string.authentication_failed, isLoading = false) }
                }
            }
        }
    }

    private fun checkValidity(username: String, email: String, password: String, confirmedPassword: String): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmedPassword.isBlank()) {
            _uiState.update { SignUpUiState(message = R.string.please_complete_all_information) }
            return false
        } else if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { SignUpUiState(message = R.string.the_email_address_isnt_in_the_correct_format) }
            return false
        } else if (password.length < 6) {
            _uiState.update { SignUpUiState(message = R.string.password_must_be_at_least_6_characters) }
            return false
        } else if (password != confirmedPassword) {
            _uiState.update { SignUpUiState(message = R.string.confirm_password_didnt_match) }
            return false
        }
        return true
    }

    fun snakeBarShown() {
        _uiState.update { SignUpUiState(message = null) }
    }
}