package com.app.iuafc.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.app.iuafc.BuildConfig
import com.app.iuafc.util.AppLog
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

sealed interface GoogleCredentialResult {
    data class Success(val idToken: String, val rawNonce: String) : GoogleCredentialResult
    /** The user closed the Google account picker. */
    data object Cancelled : GoogleCredentialResult
    data class Error(val message: String) : GoogleCredentialResult
}

/**
 * Shows the Google account picker (Credential Manager) and returns a Google ID token.
 *
 * Needs `GOOGLE_WEB_CLIENT_ID` in local.properties and the developer's SHA-1 registered
 * as an Android OAuth client in Google Cloud (see docs/google-sign-in.md).
 */
object GoogleCredentialProvider {

    val isConfigured: Boolean = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    /** [context] must be an Activity, because the picker is shown on top of it. */
    suspend fun requestIdToken(context: Context): GoogleCredentialResult {
        // Google receives the hash; Supabase receives the raw value and compares them,
        // so a stolen token cannot be reused.
        val rawNonce = UUID.randomUUID().toString()
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(sha256(rawNonce))
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            AppLog.d(TAG, "Showing the Google account picker")
            val credential = CredentialManager.create(context).getCredential(context, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                AppLog.d(TAG, "Google returned an ID token")
                GoogleCredentialResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken, rawNonce)
            } else {
                AppLog.w(TAG, "Unexpected credential type: ${credential.type}")
                GoogleCredentialResult.Error("Google sign-in failed. Please try again.")
            }
        } catch (_: GetCredentialCancellationException) {
            AppLog.d(TAG, "The user closed the Google account picker")
            GoogleCredentialResult.Cancelled
        } catch (e: NoCredentialException) {
            AppLog.w(TAG, "No Google account on this device", e)
            GoogleCredentialResult.Error("No Google account found. Add one in your phone's Settings > Accounts.")
        } catch (e: GetCredentialException) {
            // Usually the app's SHA-1 is not registered in Google Cloud (see docs/google-sign-in.md)
            AppLog.e(TAG, "Credential Manager error: ${e.type}", e)
            GoogleCredentialResult.Error("Google sign-in failed. Please try again.")
        } catch (e: GoogleIdTokenParsingException) {
            AppLog.e(TAG, "Could not read the Google ID token", e)
            GoogleCredentialResult.Error("Google sign-in failed. Please try again.")
        }
    }

    private const val TAG = "GoogleSignIn"

    private fun sha256(value: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
