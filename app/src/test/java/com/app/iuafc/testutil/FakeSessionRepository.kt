package com.app.iuafc.testutil

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SessionState
import com.app.iuafc.data.repository.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [SessionRepository] for unit tests. Set the results before calling the ViewModel. */
class FakeSessionRepository : SessionRepository {
    private val _state = MutableStateFlow<SessionState>(SessionState.SignedOut)
    override val state: StateFlow<SessionState> = _state
    override val isDemo = false

    var signInResult: AuthResult = AuthResult.Success
    var signUpResult: SignUpResult = SignUpResult.SignedIn
    var resetResult: AuthResult = AuthResult.Success

    var signInCalls = 0
        private set
    var signUpCalls = 0
        private set
    var lastSignUpName: String? = null
        private set

    override suspend fun signIn(email: String, password: String): AuthResult {
        signInCalls++
        if (signInResult == AuthResult.Success) {
            _state.value = SessionState.SignedIn(UserProfile("1", "Test User", email, Role.CHURCH_MEMBER))
        }
        return signInResult
    }

    override suspend fun signUp(fullName: String, email: String, password: String): SignUpResult {
        signUpCalls++
        lastSignUpName = fullName
        return signUpResult
    }

    override suspend fun sendPasswordReset(email: String): AuthResult = resetResult

    override suspend fun signOut() {
        _state.value = SessionState.SignedOut
    }
}
