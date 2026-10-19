package com.app.iuafc.ui

import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SessionState
import com.app.iuafc.data.repository.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Session for UI tests: never calls Supabase. Set [signInResult] before signing in. */
class FakeSessionRepository : SessionRepository {
    override val state: StateFlow<SessionState> = MutableStateFlow(SessionState.SignedOut)
    override val isDemo = false

    var signInResult: AuthResult = AuthResult.Success
    var signInCalls = 0
        private set

    override suspend fun signIn(email: String, password: String): AuthResult {
        signInCalls++
        return signInResult
    }

    override suspend fun signUp(fullName: String, email: String, password: String) = SignUpResult.ConfirmEmail
    override suspend fun signInWithGoogle(idToken: String, rawNonce: String): AuthResult = AuthResult.Success
    override suspend fun sendPasswordReset(email: String): AuthResult = AuthResult.Success
    override suspend fun signOut() = Unit
    override fun updateCurrentUserFullName(userId: String, fullName: String) = Unit
}
