package com.app.iuafc.ui.screens.auth

import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.SessionState
import com.app.iuafc.testutil.FakeSessionRepository
import com.app.iuafc.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val session = FakeSessionRepository()
    private val viewModel = LoginViewModel(session)

    @Test
    fun emptyForm_showsErrors_andDoesNotCallServer() {
        viewModel.signIn()

        val state = viewModel.uiState.value
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertEquals(0, session.signInCalls)
    }

    @Test
    fun invalidEmail_showsEmailError() {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("church2026")
        viewModel.signIn()

        assertEquals("Enter a valid email address", viewModel.uiState.value.emailError)
        assertEquals(0, session.signInCalls)
    }

    @Test
    fun validCredentials_signIn() {
        viewModel.onEmailChange("member@iuafc.org")
        viewModel.onPasswordChange("church2026")
        viewModel.signIn()

        assertEquals(1, session.signInCalls)
        assertTrue(session.state.value is SessionState.SignedIn)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun wrongPassword_showsServerMessage() {
        session.signInResult = AuthResult.Error("Incorrect email or password.")
        viewModel.onEmailChange("member@iuafc.org")
        viewModel.onPasswordChange("wrongpass1")
        viewModel.signIn()

        assertEquals("Incorrect email or password.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun typing_clearsPreviousError() {
        viewModel.signIn()
        viewModel.onEmailChange("m")

        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun googleSignIn_success_signsInWithToken() {
        viewModel.onGoogleSignInStarted()
        viewModel.onGoogleCredential(GoogleCredentialResult.Success(idToken = "google-token", rawNonce = "nonce"))

        assertEquals("google-token", session.lastGoogleToken)
        assertTrue(session.state.value is SessionState.SignedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun googleSignIn_cancelled_stopsLoadingWithoutError() {
        viewModel.onGoogleSignInStarted()
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.onGoogleCredential(GoogleCredentialResult.Cancelled)

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(session.lastGoogleToken)
    }

    @Test
    fun googleSignIn_pickerError_isShown() {
        viewModel.onGoogleSignInStarted()
        viewModel.onGoogleCredential(GoogleCredentialResult.Error("No Google account found."))

        assertEquals("No Google account found.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun googleSignIn_serverError_isShown() {
        session.googleResult = AuthResult.Error("Something went wrong. Please try again.")
        viewModel.onGoogleCredential(GoogleCredentialResult.Success("google-token", "nonce"))

        assertEquals("Something went wrong. Please try again.", viewModel.uiState.value.errorMessage)
        assertTrue(session.state.value is SessionState.SignedOut)
    }
}
