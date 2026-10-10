package com.app.iuafc.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.DashboardData
import com.app.iuafc.data.repository.DashboardRepository
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

    /**
     * Loads the dashboard data. Can also be called to retry after an error.
     */
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
        fun factory(user: UserProfile) = viewModelFactory {
            initializer {
                DashboardViewModel(
                    repository = appContainer.dashboardRepository,
                    user = user,
                )
            }
        }
    }
}