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

/** What the Forgot Password screen shows: the email field, its error, and whether the link was sent. */
data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailSent: Boolean = false,
)

/**
 * Sends a password reset link by email (Supabase Auth). The email is checked with
 * [Validators.email] first, so no request is made for an invalid address.
 *
 * Reference: Android Developers. 2026. ViewModel overview. [Online]. Available at:
 * https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 November 2026].
 */
class ForgotPasswordViewModel(private val session: SessionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    /** Updates the email and clears the previous errors. */
    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }

    /** Validates the email and asks Supabase to send the reset link. Ignored while a request is running. */
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
