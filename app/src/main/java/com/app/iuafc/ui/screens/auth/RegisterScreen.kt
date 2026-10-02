package com.app.iuafc.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.ui.components.ValidatedTextField
import com.app.iuafc.util.Validators

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val sentTo = state.confirmationSentTo
    if (sentTo != null) {
        AuthLayout(
            title = "Check your email",
            subtitle = "We sent a confirmation link to $sentTo. Open it to activate your account, then sign in.",
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
        title = "Create an account",
        subtitle = "Register to access church events, announcements and more",
    ) {
        AuthErrorBanner(state.errorMessage)

        ValidatedTextField(
            value = state.fullName,
            onValueChange = viewModel::onFullNameChange,
            label = "Full name",
            error = state.fullNameError,
            leadingIcon = Icons.Outlined.Person,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        ValidatedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = "Email",
            error = state.emailError,
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        ValidatedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            error = state.passwordError,
            supportingText = "At least ${Validators.MIN_PASSWORD_LENGTH} characters, with letters and numbers",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        ValidatedTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm password",
            error = state.confirmPasswordError,
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            enabled = !state.isLoading,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { viewModel.register() }),
            modifier = Modifier.fillMaxWidth(),
        )

        AuthButton(text = "Register", isLoading = state.isLoading, onClick = viewModel::register)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Already have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onBackToLogin) { Text("Sign in") }
        }
    }
}
