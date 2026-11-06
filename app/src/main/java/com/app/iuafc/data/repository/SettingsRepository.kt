package com.app.iuafc.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/*
 * Settings stored on the phone with Jetpack DataStore (Preferences), which replaces
 * SharedPreferences and reads and writes on a background thread.
 *
 * Reference: Android Developers. 2026. DataStore. [Online]. Available at:
 * https://developer.android.com/topic/libraries/architecture/datastore [Accessed 6 November 2026].
 */

// One DataStore file for the whole app ("iuafc_settings.preferences_pb").
private val Context.settingsDataStore by preferencesDataStore(
    name = "iuafc_settings",
)

/** Profile details the user edits in Settings. The name is also saved to Supabase. */
data class LocalProfile(
    val fullName: String,
    val phone: String,
    val address: String,
)

/** Theme chosen in Settings. [SYSTEM] follows the phone's light or dark mode. */
enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK,
}

/** The user's preferences on this device: notifications, theme and profile details. */
interface SettingsRepository {

    /** Whether notifications are turned on (the switch in Settings). */
    val notificationsEnabled: StateFlow<Boolean>

    /** The chosen theme. Observed by MainActivity, so a change applies straight away. */
    val themePreference: StateFlow<ThemePreference>

    suspend fun setNotificationsEnabled(enabled: Boolean)

    /** The profile saved for [userId]; empty fields if nothing was saved yet. */
    suspend fun getProfile(userId: String): LocalProfile

    /** Saves the profile of [userId]. Each account on the phone has its own keys. */
    suspend fun updateProfile(
        userId: String,
        fullName: String,
        phone: String,
        address: String,
    )

    suspend fun getThemePreference(): ThemePreference

    suspend fun setThemePreference(theme: ThemePreference)
}

/**
 * DataStore implementation. It is called "Mock" because the settings never leave the phone,
 * but it is the one used by the real app as well as demo mode.
 */
class MockSettingsRepository(
    private val context: Context,
    appScope: CoroutineScope,
) : SettingsRepository {

    override val notificationsEnabled: StateFlow<Boolean> =
        context.settingsDataStore.data
            .map { preferences ->
                preferences[NOTIFICATIONS_ENABLED] ?: true
            }
            .stateIn(
                scope = appScope,
                started = SharingStarted.Eagerly,
                initialValue = true,
            )

    override val themePreference: StateFlow<ThemePreference> =
        context.settingsDataStore.data
            .map { preferences ->
                when (preferences[THEME_PREFERENCE]) {
                    "LIGHT" -> ThemePreference.LIGHT
                    "DARK" -> ThemePreference.DARK
                    else -> ThemePreference.SYSTEM
                }
            }
            .stateIn(
                scope = appScope,
                started = SharingStarted.Eagerly,
                initialValue = ThemePreference.SYSTEM,
            )

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun getProfile(userId: String): LocalProfile {
        val preferences = context.settingsDataStore.data.first()

        return LocalProfile(
            fullName = preferences[profileNameKey(userId)] ?: "",
            phone = preferences[profilePhoneKey(userId)] ?: "",
            address = preferences[profileAddressKey(userId)] ?: "",
        )
    }

    override suspend fun updateProfile(
        userId: String,
        fullName: String,
        phone: String,
        address: String,
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[profileNameKey(userId)] = fullName
            preferences[profilePhoneKey(userId)] = phone
            preferences[profileAddressKey(userId)] = address
        }
    }

    override suspend fun getThemePreference(): ThemePreference {
        val preferences = context.settingsDataStore.data.first()

        return when (preferences[THEME_PREFERENCE]) {
            "LIGHT" -> ThemePreference.LIGHT
            "DARK" -> ThemePreference.DARK
            else -> ThemePreference.SYSTEM
        }
    }

    override suspend fun setThemePreference(theme: ThemePreference) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_PREFERENCE] = theme.name
        }
    }

    // DataStore keys. Profile keys include the user id so two accounts don't share a profile.
    private companion object {

        val NOTIFICATIONS_ENABLED =
            booleanPreferencesKey("notifications_enabled")

        val THEME_PREFERENCE =
            stringPreferencesKey("theme_preference")

        fun profileNameKey(userId: String) =
            stringPreferencesKey("profile_${userId}_name")

        fun profilePhoneKey(userId: String) =
            stringPreferencesKey("profile_${userId}_phone")

        fun profileAddressKey(userId: String) =
            stringPreferencesKey("profile_${userId}_address")
    }
}