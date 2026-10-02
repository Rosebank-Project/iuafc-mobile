package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

sealed interface SessionState {
    /** Restoring the saved session when the app starts. */
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: UserProfile) : SessionState
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}

sealed interface SignUpResult {
    /** Account created and the user is already signed in. */
    data object SignedIn : SignUpResult
    /** Account created; the user must click the link sent by email before signing in. */
    data object ConfirmEmail : SignUpResult
    data class Error(val message: String) : SignUpResult
}

/**
 * Login, registration and the current user.
 *
 * - [SupabaseSessionRepository]: real accounts (Supabase Auth hashes the passwords).
 * - [DemoSessionRepository]: used when the Supabase keys are missing from local.properties,
 *   so the app can still be opened as any role.
 */
interface SessionRepository {
    val state: StateFlow<SessionState>
    val isDemo: Boolean

    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signUp(fullName: String, email: String, password: String): SignUpResult
    suspend fun sendPasswordReset(email: String): AuthResult
    suspend fun signOut()

    /** Only works in demo mode. */
    fun signInAsDemo(role: Role) = Unit
}
