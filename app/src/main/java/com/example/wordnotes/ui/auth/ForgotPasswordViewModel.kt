package com.example.wordnotes.ui.auth

import androidx.core.util.PatternsCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.R
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val message: Int? = null,
    val isSending: Boolean = false,
    val resetPasswordSuccessful: Boolean = false
)

class ForgotPasswordViewModel(
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState: MutableStateFlow<ForgotPasswordUiState> = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>(EMAIL_KEY)?.let { email ->
            _uiState.update {
                it.copy(email = email)
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email).also { savedStateHandle[EMAIL_KEY] = email } }
    }

    fun send() {
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(uiState.value.email).matches()) {
            _uiState.update { it.copy(message = R.string.the_email_address_isnt_in_the_correct_format) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            when (userRepository.resetPassword(uiState.value.email)) {
                is Result.Success -> {
                    _uiState.update { it.copy(resetPasswordSuccessful = true) }
                }

                else -> {
                    _uiState.update { it.copy(message = R.string.something_went_wrong) }
                }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun snackBarShown() {
        _uiState.update { it.copy(message = null) }
    }
}

private const val EMAIL_KEY = "EMAIL_KEY"
