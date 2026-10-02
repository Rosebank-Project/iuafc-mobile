package com.app.iuafc.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.ui.components.ValidatedTextField

@Composable
fun LoginScreen(
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AuthLayout(
        title = "Welcome back",
        subtitle = "Sign in to the IUAFC Church Management System",
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
            modifier = Modifier.fillMaxWidth(),
        )
        ValidatedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Password",
            error = state.passwordError,
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            enabled = !state.isLoading,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { viewModel.signIn() }),
            modifier = Modifier.fillMaxWidth(),
        )

        TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
            Text("Forgot password?")
        }

        AuthButton(text = "Sign in", isLoading = state.isLoading, onClick = viewModel::signIn)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRegister) { Text("Register") }
        }
    }
}
