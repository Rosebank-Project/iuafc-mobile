package com.app.iuafc.ui.screens.ministries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.data.repository.UserRepository
import com.app.iuafc.util.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Ministries screen state. [saveVersion] goes up after every successful save, so the screen knows
 * when to close its dialog and clear the form.
 */
data class MinistriesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveVersion: Int = 0,
    val errorMessage: String? = null,
    val ministries: List<Ministry> = emptyList(),
    val users: List<UserProfile> = emptyList(),
)

/**
 * Ministries and their leaders. Everyone who can open the screen sees them; only Administrators
 * add or edit them. Leader names come from `profile_directory()`, which returns names and roles
 * without emails.
 *
 * Reference: Android Developers. 2026. ViewModel overview. [Online]. Available at:
 * https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 November 2026].
 */
class MinistriesViewModel(
    private val repository: ChurchFeaturesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MinistriesUiState())
    val uiState: StateFlow<MinistriesUiState> = _uiState.asStateFlow()

    /** Hides the error message after it was shown. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Loads the ministries and the user directory used to show and pick leaders. */
    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.ministries.isEmpty(), errorMessage = null) }
        try {
            val ministries = repository.getMinistries()
            val users = userRepository.getDirectory()
            _uiState.update { it.copy(isLoading = false, ministries = ministries, users = users) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e(TAG, "load failed", e)
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load ministries. Please try again.") }
        }
    }

    /** Adds or updates a ministry. The name is required and must be unique (checked by the database). */
    fun save(ministry: Ministry) = viewModelScope.launch {
        if (ministry.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a ministry name.") }
            return@launch
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            when (val result = repository.saveMinistry(ministry)) {
                SaveResult.Success -> _uiState.update {
                    it.copy(isSaving = false, saveVersion = it.saveVersion + 1, ministries = repository.getMinistries())
                }
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e(TAG, "save failed", e)
            _uiState.update { it.copy(isSaving = false, errorMessage = "Could not save the ministry. Please try again.") }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MinistriesViewModel(appContainer.churchFeaturesRepository, appContainer.userRepository) }
        }
    }
}

private const val TAG = "Ministries"
