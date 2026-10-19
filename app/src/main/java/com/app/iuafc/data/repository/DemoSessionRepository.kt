
package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Offline session used when Supabase is not configured:
 * pick a role to sign in.
 */
class DemoSessionRepository : SessionRepository {

    private val _state =
        MutableStateFlow<SessionState>(SessionState.SignedOut)

    override val state: StateFlow<SessionState> =
        _state.asStateFlow()

    override val isDemo = true

    /**
     * Signs in as the sample user with this role (e.g. Church Member = Grace Adeyemi), so
     * screens that depend on the user's member record or ministry also have data.
     */
    override fun signInAsDemo(role: Role) {
        val user = MockData.users.firstOrNull { it.role == role }
            ?: UserProfile(
                id = "demo-${role.name.lowercase()}",
                fullName = "Demo ${role.displayName}",
                email = "${role.name.lowercase()}@iuafc.demo",
                role = role,
            )
        _state.value = SessionState.SignedIn(user)
    }

    override fun updateCurrentUserFullName(
        userId: String,
        fullName: String,
    ) {
        val current =
            _state.value as? SessionState.SignedIn ?: return

        if (current.user.id != userId) return

        _state.value = SessionState.SignedIn(
            current.user.copy(
                fullName = fullName.trim(),
            )
        )
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ) = AuthResult.Error(NOT_CONFIGURED)

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
    ) = SignUpResult.Error(NOT_CONFIGURED)

    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
    ) = AuthResult.Error(NOT_CONFIGURED)

    override suspend fun sendPasswordReset(
        email: String,
    ) = AuthResult.Error(NOT_CONFIGURED)

    override suspend fun signOut() {
        _state.value = SessionState.SignedOut
    }

    private companion object {

        const val NOT_CONFIGURED =
            "Supabase is not configured. Add the keys to local.properties."
    }
}
