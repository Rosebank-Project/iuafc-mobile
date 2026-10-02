package com.app.iuafc.ui.screens.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.ui.components.ValidatedTextField

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(factory = ForgotPasswordViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.emailSent) {
        AuthLayout(
            title = "Check your email",
            subtitle = "If an account exists for ${state.email.trim()}, you will receive a link to reset your password.",
        ) {
            Icon(
                Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            )
            OutlinedButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Back to sign in")
            }
        }
        return
    }

    AuthLayout(
        title = "Forgot your password?",
        subtitle = "Enter your email and we will send you a link to reset it",
    ) {
        AuthErrorBanner(state.errorMessage)

        ValidatedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            error = state.emailError,
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            enabled = !state.isLoading,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { viewModel.sendResetLink() }),
            modifier = Modifier.fillMaxWidth(),
        )

        AuthButton(text = "Send reset link", isLoading = state.isLoading, onClick = viewModel::sendResetLink)

        TextButton(onClick = onBackToLogin) { Text("Back to sign in") }
    }
}
