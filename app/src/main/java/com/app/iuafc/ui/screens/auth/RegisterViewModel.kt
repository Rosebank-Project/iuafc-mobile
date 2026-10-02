package com.app.iuafc.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SignUpResult
import com.app.iuafc.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Set when the account was created and a confirmation email was sent. */
    val confirmationSentTo: String? = null,
)

class RegisterViewModel(private val session: SessionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) =
        _uiState.update { it.copy(fullName = value, fullNameError = null, errorMessage = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null) }

    fun register() {
        val current = _uiState.value
        if (current.isLoading) return

        val validated = current.copy(
            fullNameError = Validators.required(current.fullName, "Full name"),
            emailError = Validators.email(current.email),
            passwordError = Validators.password(current.password),
            confirmPasswordError = Validators.confirmPassword(current.password, current.confirmPassword),
        )
        val hasErrors = listOf(
            validated.fullNameError, validated.emailError,
            validated.passwordError, validated.confirmPasswordError,
        ).any { it != null }
        if (hasErrors) {
            _uiState.value = validated
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = session.signUp(current.fullName, current.email, current.password)
            _uiState.update {
                when (result) {
                    // The session changes and MainActivity opens the app.
                    SignUpResult.SignedIn -> it.copy(isLoading = false)
                    SignUpResult.ConfirmEmail -> it.copy(isLoading = false, confirmationSentTo = current.email.trim())
                    is SignUpResult.Error -> it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { RegisterViewModel(appContainer.sessionRepository) }
        }
    }
}
