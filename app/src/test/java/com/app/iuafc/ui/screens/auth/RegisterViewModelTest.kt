package com.app.iuafc.ui.screens.auth

import com.app.iuafc.data.repository.SignUpResult
import com.app.iuafc.testutil.FakeSessionRepository
import com.app.iuafc.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val session = FakeSessionRepository()
    private val viewModel = RegisterViewModel(session)

    private fun fillValidForm() {
        viewModel.onFullNameChange("Thabo Nkosi")
        viewModel.onEmailChange("thabo@iuafc.org")
        viewModel.onPasswordChange("church2026")
        viewModel.onConfirmPasswordChange("church2026")
    }

    @Test
    fun emptyForm_showsAllErrors() {
        viewModel.register()

        val state = viewModel.uiState.value
        assertNotNull(state.fullNameError)
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertNotNull(state.confirmPasswordError)
        assertEquals(0, session.signUpCalls)
    }

    @Test
    fun weakPassword_isRejected() {
        fillValidForm()
        viewModel.onPasswordChange("short")
        viewModel.onConfirmPasswordChange("short")
        viewModel.register()

        assertNotNull(viewModel.uiState.value.passwordError)
        assertEquals(0, session.signUpCalls)
    }

    @Test
    fun differentPasswords_areRejected() {
        fillValidForm()
        viewModel.onConfirmPasswordChange("church2025")
        viewModel.register()

        assertEquals("Passwords do not match", viewModel.uiState.value.confirmPasswordError)
        assertEquals(0, session.signUpCalls)
    }

    @Test
    fun validForm_createsAccount() {
        fillValidForm()
        viewModel.register()

        assertEquals(1, session.signUpCalls)
        assertEquals("Thabo Nkosi", session.lastSignUpName)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun emailConfirmationRequired_showsCheckEmail() {
        session.signUpResult = SignUpResult.ConfirmEmail
        fillValidForm()
        viewModel.register()

        assertEquals("thabo@iuafc.org", viewModel.uiState.value.confirmationSentTo)
    }

    @Test
    fun existingEmail_showsServerMessage() {
        session.signUpResult = SignUpResult.Error("An account with this email already exists.")
        fillValidForm()
        viewModel.register()

        assertEquals("An account with this email already exists.", viewModel.uiState.value.errorMessage)
    }
}
