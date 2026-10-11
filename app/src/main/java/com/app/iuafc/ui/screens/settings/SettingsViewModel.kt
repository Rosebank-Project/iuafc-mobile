package com.app.iuafc.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.LocalProfile
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SettingsRepository
import com.app.iuafc.data.repository.ThemePreference
import com.app.iuafc.data.repository.UserRepository
import com.app.iuafc.util.Validators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val notificationsEnabled = settingsRepository.notificationsEnabled

    private val _profile = MutableStateFlow<LocalProfile?>(null)
    val profile: StateFlow<LocalProfile?> = _profile.asStateFlow()

    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> =
        _isProfileLoading.asStateFlow()

    private val _profileMessage = MutableStateFlow<String?>(null)
    val profileMessage: StateFlow<String?> =
        _profileMessage.asStateFlow()

    private val _fullNameError = MutableStateFlow<String?>(null)
    val fullNameError: StateFlow<String?> =
        _fullNameError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> =
        _phoneError.asStateFlow()

    private val _profileSaveSucceeded = MutableStateFlow(false)
    val profileSaveSucceeded: StateFlow<Boolean> =
        _profileSaveSucceeded.asStateFlow()

    private val _passwordResetMessage = MutableStateFlow<String?>(null)
    val passwordResetMessage: StateFlow<String?> =
        _passwordResetMessage.asStateFlow()

    private val _themePreference =
        MutableStateFlow(ThemePreference.SYSTEM)
    val themePreference: StateFlow<ThemePreference> =
        _themePreference.asStateFlow()

    private val _isThemeLoading = MutableStateFlow(false)
    val isThemeLoading: StateFlow<Boolean> =
        _isThemeLoading.asStateFlow()

    fun loadProfile(user: UserProfile) {
        viewModelScope.launch {
            _isProfileLoading.value = true

            try {
                val savedProfile =
                    settingsRepository.getProfile(user.id)

                _profile.value = LocalProfile(
                    fullName = savedProfile.fullName
                        .ifBlank { user.fullName },
                    phone = savedProfile.phone,
                    address = savedProfile.address,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profileMessage.value =
                    "We couldn't load your profile. Please try again."
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
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profileMessage.value =
                    "We couldn't load your theme preference."
            } finally {
                _isThemeLoading.value = false
            }
        }
    }

    fun updateThemePreference(preference: ThemePreference) {
        if (_isThemeLoading.value) return

        viewModelScope.launch {
            _isThemeLoading.value = true

            try {
                settingsRepository.setThemePreference(preference)
                _themePreference.value = preference
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profileMessage.value =
                    "We couldn't change the theme. Please try again."
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
        if (_isProfileLoading.value) return

        _profileMessage.value = null
        _fullNameError.value = null
        _phoneError.value = null
        _profileSaveSucceeded.value = false

        val cleanedName = fullName.trim()
        val cleanedPhone = phone.trim()
        val cleanedAddress = address.trim()

        if (cleanedName.isBlank()) {
            _fullNameError.value = "Full name is required."
            return
        }

        if (cleanedName.length < 2) {
            _fullNameError.value =
                "Full name must contain at least 2 characters."
            return
        }

        if (cleanedPhone.isNotBlank()) {
            val validationError = Validators.phone(cleanedPhone)

            if (validationError != null) {
                _phoneError.value = validationError
                return
            }
        }

        viewModelScope.launch {
            _isProfileLoading.value = true

            try {
                // Save the name to the remote profile when not in demo mode.
                if (!sessionRepository.isDemo) {
                    when (
                        val result = userRepository.updateFullName(
                            userId = user.id,
                            fullName = cleanedName,
                        )
                    ) {
                        is SaveResult.Success -> Unit

                        is SaveResult.Error -> {
                            _profileMessage.value =
                                result.message.ifBlank {
                                    "We couldn't save your name. Please try again."
                                }
                            return@launch
                        }
                    }
                }

                // Save local profile information.
                settingsRepository.updateProfile(
                    userId = user.id,
                    fullName = cleanedName,
                    phone = cleanedPhone,
                    address = cleanedAddress,
                )

                // Update the active session after the profile save succeeds.
                sessionRepository.updateCurrentUserFullName(
                    userId = user.id,
                    fullName = cleanedName,
                )

                _profile.value = LocalProfile(
                    fullName = cleanedName,
                    phone = cleanedPhone,
                    address = cleanedAddress,
                )

                _profileMessage.value = "Profile updated successfully."
                _profileSaveSucceeded.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profileMessage.value =
                    "We couldn't save your changes. Check your connection and try again."
            } finally {
                _isProfileLoading.value = false
            }
        }
    }

    fun clearProfileMessage() {
        _profileMessage.value = null
        _fullNameError.value = null
        _phoneError.value = null
        _profileSaveSucceeded.value = false
    }

    fun clearFullNameError() {
        _fullNameError.value = null
        _profileMessage.value = null
        _profileSaveSucceeded.value = false
    }

    fun clearPhoneError() {
        _phoneError.value = null
        _profileMessage.value = null
        _profileSaveSucceeded.value = false
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setNotificationsEnabled(enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profileMessage.value =
                    "We couldn't update notifications. Please try again."
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val cleanedEmail = email.trim()

        if (cleanedEmail.isBlank()) {
            _passwordResetMessage.value =
                "Your email address is missing."
            return
        }

        viewModelScope.launch {
            _passwordResetMessage.value = null

            try {
                when (
                    val result =
                        sessionRepository.sendPasswordReset(cleanedEmail)
                ) {
                    is AuthResult.Success -> {
                        _passwordResetMessage.value =
                            "Password reset instructions have been sent."
                    }

                    is AuthResult.Error -> {
                        _passwordResetMessage.value =
                            result.message.ifBlank {
                                "We couldn't send the reset email. Please try again."
                            }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _passwordResetMessage.value =
                    "We couldn't send the reset email. Please try again."
            }
        }
    }

    fun clearPasswordResetMessage() {
        _passwordResetMessage.value = null
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                sessionRepository.signOut()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _profileMessage.value =
                    "We couldn't sign you out. Please try again."
            }
        }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            sessionRepository: SessionRepository,
            userRepository: UserRepository,
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
                            settingsRepository = settingsRepository,
                            sessionRepository = sessionRepository,
                            userRepository = userRepository,
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}",
                    )
                }
            }
    }
}