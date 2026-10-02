package com.app.iuafc.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel(private val session: SessionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun signIn() {
        val current = _uiState.value
        if (current.isLoading) return

        val emailError = Validators.email(current.email)
        val passwordError = Validators.required(current.password, "Password")
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // On success the session changes and MainActivity opens the app.
            val result = session.signIn(current.email, current.password)
            _uiState.update {
                it.copy(isLoading = false, errorMessage = (result as? AuthResult.Error)?.message)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { LoginViewModel(appContainer.sessionRepository) }
        }
    }
}
