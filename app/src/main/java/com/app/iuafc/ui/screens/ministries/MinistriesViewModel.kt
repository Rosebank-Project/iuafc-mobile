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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MinistriesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveVersion: Int = 0,
    val errorMessage: String? = null,
    val ministries: List<Ministry> = emptyList(),
    val users: List<UserProfile> = emptyList(),
)

class MinistriesViewModel(
    private val repository: ChurchFeaturesRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MinistriesUiState())
    val uiState: StateFlow<MinistriesUiState> = _uiState.asStateFlow()

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.ministries.isEmpty(), errorMessage = null) }
        try {
            val ministries = repository.getMinistries()
            val users = userRepository.getUsers()
            _uiState.update { it.copy(isLoading = false, ministries = ministries, users = users) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load ministries. Please try again.") }
        }
    }

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
        } catch (_: Exception) {
            _uiState.update { it.copy(isSaving = false, errorMessage = "Could not save the ministry. Please try again.") }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MinistriesViewModel(appContainer.churchFeaturesRepository, appContainer.userRepository) }
        }
    }
}
