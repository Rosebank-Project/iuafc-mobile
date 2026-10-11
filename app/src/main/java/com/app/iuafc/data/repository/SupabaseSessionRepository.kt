package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Real accounts with Supabase Auth.
 *
 * Passwords are hashed (bcrypt) by Supabase and never stored by the app.
 * The session is saved on the device, so the user stays signed in
 * after closing the app.
 */
class SupabaseSessionRepository(
    private val supabase: SupabaseClient,
    scope: CoroutineScope,
) : SessionRepository {

    override val isDemo = false

    private val _state =
        MutableStateFlow<SessionState>(SessionState.Loading)

    override val state: StateFlow<SessionState> =
        _state.asStateFlow()

    init {
        scope.launch {
            supabase.auth.sessionStatus.collect { status ->
                _state.value = when (status) {
                    is SessionStatus.Authenticated ->
                        (
                                status.session.user
                                    ?: supabase.auth.currentUserOrNull()
                                )?.let {
                                SessionState.SignedIn(loadProfile(it))
                            } ?: SessionState.SignedOut

                    is SessionStatus.Initializing ->
                        SessionState.Loading

                    is SessionStatus.NotAuthenticated ->
                        SessionState.SignedOut

                    // If token refresh fails, keep the user signed in
                    // when a local user session is still available.
                    is SessionStatus.RefreshFailure ->
                        supabase.auth.currentUserOrNull()
                            ?.let {
                                SessionState.SignedIn(loadProfile(it))
                            } ?: SessionState.SignedOut
                }
            }
        }
    }

    override fun updateCurrentUserFullName(
        userId: String,
        fullName: String,
    ) {
        val cleanedName = fullName.trim()

        if (cleanedName.isBlank()) return

        val current =
            _state.value as? SessionState.SignedIn ?: return

        if (current.user.id != userId) return

        _state.value = SessionState.SignedIn(
            current.user.copy(
                fullName = cleanedName,
            )
        )
    }

    override suspend fun signIn(
        email: String,
        password: String,
    ): AuthResult = attempt(AuthResult::Error) {
        supabase.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }

        AuthResult.Success
    }

    override suspend fun signUp(
        fullName: String,
        email: String,
        password: String,
    ): SignUpResult = attempt(SignUpResult::Error) {
        supabase.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password

            data = buildJsonObject {
                put("full_name", fullName.trim())
            }
        }

        // With email confirmation enabled, there may be no session yet.
        if (supabase.auth.currentSessionOrNull() != null) {
            SignUpResult.SignedIn
        } else {
            SignUpResult.ConfirmEmail
        }
    }

    override suspend fun signInWithGoogle(
        idToken: String,
        rawNonce: String,
    ): AuthResult = attempt(AuthResult::Error) {
        // Supabase validates the Google token and creates the user
        // on the first successful sign-in.
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            nonce = rawNonce
        }

        AuthResult.Success
    }

    override suspend fun sendPasswordReset(
        email: String,
    ): AuthResult = attempt(AuthResult::Error) {
        supabase.auth.resetPasswordForEmail(email.trim())

        AuthResult.Success
    }

    override suspend fun signOut() {
        try {
            supabase.auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Offline: the local session is cleared anyway.
        }
    }

    /**
     * Reads the user's role from the profiles table.
     *
     * If the profile cannot be read (for example, while offline),
     * the user is treated as a Church Member.
     */
    private suspend fun loadProfile(
        user: UserInfo,
    ): UserProfile {
        val fallback = UserProfile(
            id = user.id,
            fullName = user.userMetadata
                ?.get("full_name")
                ?.jsonPrimitive
                ?.content
                ?: user.email.orEmpty(),
            email = user.email.orEmpty(),
            role = Role.CHURCH_MEMBER,
        )

        return try {
            supabase.from("profiles")
                .select(SupabaseUserRepository.PROFILE_COLUMNS) {
                    filter {
                        eq("id", user.id)
                    }
                }
                .decodeSingleOrNull<UserProfile>()
                ?: fallback
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * Runs a Supabase call and converts failures into
     * messages the user can understand.
     */
    private inline fun <R> attempt(
        onError: (String) -> R,
        block: () -> R,
    ): R =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onError(friendlyMessage(e))
        }

    private companion object {

        fun friendlyMessage(
            e: Exception,
        ): String =
            when (e) {
                is AuthRestException ->
                    when (e.errorCode) {
                        AuthErrorCode.InvalidCredentials ->
                            "Incorrect email or password."

                        AuthErrorCode.UserAlreadyExists,
                        AuthErrorCode.EmailExists ->
                            "An account with this email already exists."

                        AuthErrorCode.EmailNotConfirmed ->
                            "Please confirm your email before signing in."

                        AuthErrorCode.WeakPassword ->
                            "This password is too weak. Choose a stronger one."

                        AuthErrorCode.OverRequestRateLimit,
                        AuthErrorCode.OverEmailSendRateLimit ->
                            "Too many attempts. Please wait a few minutes and try again."

                        else -> e.errorDescription
                    }

                is HttpRequestException ->
                    "No internet connection. Check your connection and try again."

                else ->
                    "Something went wrong. Please try again."
            }
    }
}