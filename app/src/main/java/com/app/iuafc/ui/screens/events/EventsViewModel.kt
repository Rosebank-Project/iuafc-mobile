package com.app.iuafc.ui.screens.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val events: List<Event> = emptyList(),
    val registeredEventIds: List<String> = emptyList()
)

class EventsViewModel(
    private val repository: EventRepository,
    private val user: UserProfile
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    val canManage: Boolean get() = user.role == Role.ADMINISTRATOR || user.role == Role.PASTOR
    val canRegister: Boolean get() = user.role == Role.CHURCH_MEMBER && user.memberId != null

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val events = repository.getEvents()
                val registeredIds = if (user.memberId != null) {
                    repository.getRegisteredEventIds(user.memberId!!)
                } else emptyList()

                _uiState.update { it.copy(isLoading = false, events = events, registeredEventIds = registeredIds) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load events.") }
            }
        }
    }

    fun toggleRegistration(eventId: String) {
        if (!canRegister || user.memberId == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val isRegistered = _uiState.value.registeredEventIds.contains(eventId)
                if (isRegistered) {
                    repository.unregisterFromEvent(eventId, user.memberId)
                } else {
                    repository.registerForEvent(eventId, user.memberId)
                }
                loadEvents() // Refresh the list
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to update registration.") }
            }
        }
    }

    fun deleteEvent(eventId: String) {
        if (!canManage) return
        viewModelScope.launch {
            try {
                repository.deleteEvent(eventId)
                loadEvents()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to delete event.") }
            }
        }
    }

    companion object {
        fun factory(user: UserProfile) = viewModelFactory {
            initializer {
                EventsViewModel(
                    repository = appContainer.eventRepository,
                    user = user
                )
            }
        }
    }
}