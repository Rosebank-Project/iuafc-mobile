package com.app.iuafc.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.AppNotification
import com.app.iuafc.data.repository.NotificationRepository
import com.app.iuafc.ui.common.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UiState<List<AppNotification>>>(
            UiState.Loading,
        )

    val uiState: StateFlow<UiState<List<AppNotification>>> =
        _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            try {
                val notifications = repository.getNotifications()
                _uiState.value = UiState.Success(notifications)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to load notifications.",
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                repository.markAsRead(notificationId)
                loadNotifications()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to update notification.",
                )
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repository.markAllAsRead()
                loadNotifications()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to update notifications.",
                )
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                NotificationViewModel(
                    repository = appContainer.notificationRepository,
                )
            }
        }
    }
}