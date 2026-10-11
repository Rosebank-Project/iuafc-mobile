
package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {

    data object Loading : SessionState

    data object SignedOut : SessionState

    data class SignedIn(
        val user: UserProfile,
    ) : SessionState
}

sealed interface AuthResult {

    data object Success : AuthResult

    data class Error(
        val message: String,
    ) : AuthResult
}

sealed interface SignUpResult {

    data object SignedIn : SignUpResult

    data object ConfirmEmail : SignUpResult

    data class Error(
        val message: String,
    ) : SignUpResult
}

interface SessionRepository {

    val state: StateFlow<SessionState>

    val isDemo: Boolean

    suspend fun signIn(
        email: String,
        password: String,
    ): AuthResult

    suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
    ): SignUpResult

    suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
    ): AuthResult

    suspend fun sendPasswordReset(
        email: String,
    ): AuthResult

    suspend fun signOut()

    /**
     * Updates the name in the active session after the profile save succeeds.
     * This does not save the name to the database.
     */
    fun updateCurrentUserFullName(
        userId: String,
        fullName: String,
    )

    fun signInAsDemo(role: Role) = Unit
}
