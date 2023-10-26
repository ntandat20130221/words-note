package com.example.wordnotes.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wordnotes.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLogOut: Boolean = false
)

class AccountViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<AccountUiState> = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun logOut() {
        viewModelScope.launch {
            userRepository.logOut()
            _uiState.update { it.copy(isLogOut = true) }
        }
    }
}