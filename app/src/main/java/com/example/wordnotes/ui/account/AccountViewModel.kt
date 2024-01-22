package com.example.wordnotes.ui.account

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

data class AccountUiState(
    val user: User = User(),
    val isLogOut: Boolean = false
)

@HiltViewModel
class AccountViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState: MutableStateFlow<AccountUiState> = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userRepository.getUser().let { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(user = result.data) }
                }
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            if (userRepository.logOut() is Result.Success) {
                _uiState.update { it.copy(isLogOut = true) }
            }
        }
    }
}