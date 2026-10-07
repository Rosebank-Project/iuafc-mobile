package com.app.iuafc.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.LocalProfile
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SettingsRepository
import com.app.iuafc.data.repository.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> =
        settingsRepository.notificationsEnabled

    private val _profile =
        MutableStateFlow<LocalProfile?>(null)

    val profile: StateFlow<LocalProfile?> =
        _profile.asStateFlow()

    private val _isProfileLoading =
        MutableStateFlow(false)

    val isProfileLoading: StateFlow<Boolean> =
        _isProfileLoading.asStateFlow()

    private val _profileMessage =
        MutableStateFlow<String?>(null)

    val profileMessage: StateFlow<String?> =
        _profileMessage.asStateFlow()

    private val _passwordResetMessage =
        MutableStateFlow<String?>(null)

    val passwordResetMessage: StateFlow<String?> =
        _passwordResetMessage.asStateFlow()

    private val _themePreference =
        MutableStateFlow(ThemePreference.SYSTEM)

    val themePreference: StateFlow<ThemePreference> =
        _themePreference.asStateFlow()

    private val _isThemeLoading =
        MutableStateFlow(false)

    val isThemeLoading: StateFlow<Boolean> =
        _isThemeLoading.asStateFlow()

    fun loadProfile(user: UserProfile) {
        viewModelScope.launch {
            _isProfileLoading.value = true
            _profileMessage.value = null

            try {
                val savedProfile =
                    settingsRepository.getProfile(user.id)

                _profile.value =
                    LocalProfile(
                        fullName = savedProfile.fullName
                            .ifBlank { user.fullName },

                        phone = savedProfile.phone,

                        address = savedProfile.address,
                    )
            } catch (exception: Exception) {
                _profileMessage.value =
                    exception.message
                        ?: "Unable to load profile."
            } finally {
                _isProfileLoading.value = false
            }
        }
    }

    fun loadThemePreference() {
        viewModelScope.launch {
            _isThemeLoading.value = true

            try {
                _themePreference.value =
                    settingsRepository.getThemePreference()
            } catch (exception: Exception) {
                _themePreference.value =
                    ThemePreference.SYSTEM
            } finally {
                _isThemeLoading.value = false
            }
        }
    }

    fun updateThemePreference(
        theme: ThemePreference,
    ) {
        viewModelScope.launch {
            _isThemeLoading.value = true

            try {
                settingsRepository.setThemePreference(
                    theme,
                )

                _themePreference.value = theme
            } catch (exception: Exception) {
                // Keep the current theme preference if saving fails.
            } finally {
                _isThemeLoading.value = false
            }
        }
    }

    fun updateProfile(
        user: UserProfile,
        fullName: String,
        phone: String,
        address: String,
    ) {
        val cleanedName = fullName.trim()
        val cleanedPhone = phone.trim()
        val cleanedAddress = address.trim()

        if (cleanedName.isBlank()) {
            _profileMessage.value =
                "Full name cannot be empty."
            return
        }

        if (cleanedName.length < 2) {
            _profileMessage.value =
                "Full name must contain at least 2 characters."
            return
        }

        if (
            cleanedPhone.isNotBlank() &&
            cleanedPhone.length < 7
        ) {
            _profileMessage.value =
                "Please enter a valid phone number."
            return
        }

        viewModelScope.launch {
            _isProfileLoading.value = true
            _profileMessage.value = null

            try {
                settingsRepository.updateProfile(
                    userId = user.id,
                    fullName = cleanedName,
                    phone = cleanedPhone,
                    address = cleanedAddress,
                )

                _profile.value =
                    LocalProfile(
                        fullName = cleanedName,
                        phone = cleanedPhone,
                        address = cleanedAddress,
                    )

                _profileMessage.value =
                    "Profile updated successfully."
            } catch (exception: Exception) {
                _profileMessage.value =
                    exception.message
                        ?: "Unable to update profile."
            } finally {
                _isProfileLoading.value = false
            }
        }
    }

    fun setNotificationsEnabled(
        enabled: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(
                enabled,
            )
        }
    }

    fun sendPasswordReset(
        email: String,
    ) {
        viewModelScope.launch {
            _passwordResetMessage.value = null

            when (
                val result =
                    sessionRepository.sendPasswordReset(
                        email,
                    )
            ) {
                AuthResult.Success -> {
                    _passwordResetMessage.value =
                        "Password reset email sent to ${email.trim()}."
                }

                is AuthResult.Error -> {
                    _passwordResetMessage.value =
                        result.message
                }
            }
        }
    }

    fun clearPasswordResetMessage() {
        _passwordResetMessage.value = null
    }

    fun clearProfileMessage() {
        _profileMessage.value = null
    }

    fun signOut() {
        viewModelScope.launch {
            sessionRepository.signOut()
        }
    }

    companion object {

        fun factory(
            settingsRepository: SettingsRepository,
            sessionRepository: SessionRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            SettingsViewModel::class.java,
                        )
                    ) {
                        return SettingsViewModel(
                            settingsRepository =
                                settingsRepository,

                            sessionRepository =
                                sessionRepository,
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}",
                    )
                }
            }
    }
}