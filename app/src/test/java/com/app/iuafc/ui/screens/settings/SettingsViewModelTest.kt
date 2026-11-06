package com.app.iuafc.ui.screens.settings

import com.app.iuafc.data.repository.AuthResult
import com.app.iuafc.data.repository.LocalProfile
import com.app.iuafc.data.repository.MockUserRepository
import com.app.iuafc.data.repository.SessionState
import com.app.iuafc.data.repository.SettingsRepository
import com.app.iuafc.data.repository.ThemePreference
import com.app.iuafc.testutil.FakeSessionRepository
import com.app.iuafc.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val session = FakeSessionRepository()
    private val viewModel = SettingsViewModel(
        settingsRepository = InMemorySettingsRepository(),
        sessionRepository = session,
        userRepository = MockUserRepository(delayMillis = 0),
    )

    @Test
    fun deleteAccount_signsOut() {
        viewModel.deleteAccount()

        assertEquals(1, session.deleteCalls)
        assertEquals(SessionState.SignedOut, session.state.value)
        assertNull(viewModel.deleteAccountError.value)
        assertFalse(viewModel.isDeletingAccount.value)
    }

    @Test
    fun deleteAccount_refused_showsTheReason() {
        val reason = "You are the only Administrator. Make someone else an Administrator before deleting your account."
        session.deleteResult = AuthResult.Error(reason)

        viewModel.deleteAccount()

        assertEquals(reason, viewModel.deleteAccountError.value)
        assertFalse(viewModel.isDeletingAccount.value)
    }

    @Test
    fun clearDeleteAccountError_hidesTheMessage() {
        session.deleteResult = AuthResult.Error("No internet connection. Check your connection and try again.")
        viewModel.deleteAccount()

        viewModel.clearDeleteAccountError()

        assertNull(viewModel.deleteAccountError.value)
    }
}

/** Settings kept in memory, so the test doesn't need an Android Context for DataStore. */
private class InMemorySettingsRepository : SettingsRepository {
    override val notificationsEnabled: StateFlow<Boolean> = MutableStateFlow(true)
    override val themePreference: StateFlow<ThemePreference> = MutableStateFlow(ThemePreference.SYSTEM)
    override suspend fun setNotificationsEnabled(enabled: Boolean) = Unit
    override suspend fun getProfile(userId: String) = LocalProfile("", "", "")
    override suspend fun updateProfile(userId: String, fullName: String, phone: String, address: String) = Unit
    override suspend fun getThemePreference() = ThemePreference.SYSTEM
    override suspend fun setThemePreference(theme: ThemePreference) = Unit
}
