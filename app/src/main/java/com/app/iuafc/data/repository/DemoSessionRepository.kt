package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Offline session used when Supabase is not configured: pick a role to sign in. */
class DemoSessionRepository : SessionRepository {

    private val _state = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val state: StateFlow<SessionState> = _state.asStateFlow()
    override val isDemo = true

    override fun signInAsDemo(role: Role) {
        _state.value = SessionState.SignedIn(
            UserProfile(
                id = "demo-${role.name.lowercase()}",
                fullName = "Demo ${role.displayName}",
                email = "${role.name.lowercase()}@iuafc.demo",
                role = role,
            )
        )
    }

    override suspend fun signIn(email: String, password: String) = AuthResult.Error(NOT_CONFIGURED)
    override suspend fun signUp(fullName: String, email: String, password: String) = SignUpResult.Error(NOT_CONFIGURED)
    override suspend fun signInWithGoogle(idToken: String, rawNonce: String) = AuthResult.Error(NOT_CONFIGURED)
    override suspend fun sendPasswordReset(email: String) = AuthResult.Error(NOT_CONFIGURED)

    override suspend fun signOut() {
        _state.value = SessionState.SignedOut
    }

    private companion object {
        const val NOT_CONFIGURED = "Supabase is not configured. Add the keys to local.properties."
    }
}
