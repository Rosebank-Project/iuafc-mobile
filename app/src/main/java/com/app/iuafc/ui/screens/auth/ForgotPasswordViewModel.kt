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

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailSent: Boolean = false,
)

class ForgotPasswordViewModel(private val session: SessionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }

    fun sendResetLink() {
        val current = _uiState.value
        if (current.isLoading) return

        Validators.email(current.email)?.let { error ->
            _uiState.update { it.copy(emailError = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = session.sendPasswordReset(current.email)
            _uiState.update {
                when (result) {
                    AuthResult.Success -> it.copy(isLoading = false, emailSent = true)
                    is AuthResult.Error -> it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ForgotPasswordViewModel(appContainer.sessionRepository) }
        }
    }
}
