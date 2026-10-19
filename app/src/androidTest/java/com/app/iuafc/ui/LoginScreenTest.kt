package com.app.iuafc.ui

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.ui.screens.auth.LoginScreen
import com.app.iuafc.ui.screens.auth.LoginViewModel
import com.app.iuafc.ui.theme.IUAFCTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the sign-in form: validation and the message shown when sign-in fails. */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule val compose = createComposeRule()

    private val session = FakeSessionRepository()

    @Before
    fun showLoginScreen() {
        compose.setContent {
            IUAFCTheme {
                LoginScreen(onRegister = {}, onForgotPassword = {}, viewModel = LoginViewModel(session))
            }
        }
    }

    private fun field(label: String) = compose.onNode(hasSetTextAction() and hasText(label))

    @Test
    fun emptyForm_showsRequiredErrors_andDoesNotSignIn() {
        compose.onNodeWithText("Sign in").performClick()

        compose.onNodeWithText("Email is required").assertExists()
        compose.onNodeWithText("Password is required").assertExists()
        assertEquals(0, session.signInCalls)
    }

    @Test
    fun invalidEmail_showsError_andDoesNotSignIn() {
        field("Email").performTextInput("not-an-email")
        field("Password").performTextInput("Secret123")
        compose.onNodeWithText("Sign in").performClick()

        compose.onNodeWithText("Enter a valid email address").assertExists()
        assertEquals(0, session.signInCalls)
    }

    @Test
    fun wrongPassword_showsTheErrorMessage() {
        session.signInResult = AuthResult.Error("Incorrect email or password.")

        field("Email").performTextInput("grace@iuafc.org")
        field("Password").performTextInput("WrongPass1")
        compose.onNodeWithText("Sign in").performClick()

        compose.waitUntil(5_000) {
            compose.onAllNodes(hasText("Incorrect email or password.")).fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(1, session.signInCalls)
    }
}
