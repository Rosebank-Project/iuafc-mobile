package com.app.iuafc.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.DashboardData
import com.app.iuafc.data.repository.DashboardRepository
import com.app.iuafc.data.repository.MockDashboardRepository
import com.app.iuafc.ui.common.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository,
    private val user: UserProfile,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UiState<DashboardData>>(UiState.Loading)

    val uiState: StateFlow<UiState<DashboardData>> =
        _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val data = repository.getDashboard(user)
                _uiState.value = UiState.Success(data)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to load dashboard.",
                )
            }
        }
    }

    companion object {
        fun factory(user: UserProfile): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {
                    if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                        return DashboardViewModel(
                            repository = MockDashboardRepository(),
                            user = user,
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}",
                    )
                }
            }
    }
}