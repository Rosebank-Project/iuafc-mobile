package com.app.iuafc.ui.screens.auth

import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.testutil.FakeSessionRepository
import com.app.iuafc.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ForgotPasswordViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val session = FakeSessionRepository()
    private val viewModel = ForgotPasswordViewModel(session)

    @Test
    fun invalidEmail_showsError() {
        viewModel.onEmailChange("abc")
        viewModel.sendResetLink()

        assertNotNull(viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.emailSent)
    }

    @Test
    fun validEmail_sendsLink() {
        viewModel.onEmailChange("member@iuafc.org")
        viewModel.sendResetLink()

        assertTrue(viewModel.uiState.value.emailSent)
    }

    @Test
    fun serverError_isShown() {
        session.resetResult = AuthResult.Error("No internet connection. Check your connection and try again.")
        viewModel.onEmailChange("member@iuafc.org")
        viewModel.sendResetLink()

        assertEquals(
            "No internet connection. Check your connection and try again.",
            viewModel.uiState.value.errorMessage,
        )
        assertFalse(viewModel.uiState.value.emailSent)
    }
}
