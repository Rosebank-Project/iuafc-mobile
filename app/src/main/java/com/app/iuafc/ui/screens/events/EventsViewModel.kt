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
import com.app.iuafc.util.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Events screen state. [registeredEventIds] are the events the signed-in member registered for. */
data class EventsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val events: List<Event> = emptyList(),
    val registeredEventIds: List<String> = emptyList()
)

/**
 * Events: everyone sees them, Church Members register or unregister, and Administrators and
 * Pastors create, edit and delete them. After every change the list is loaded again, so the
 * screen always shows what is in the database.
 *
 * Reference: Android Developers. 2026. ViewModel overview. [Online]. Available at:
 * https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 November 2026].
 */
class EventsViewModel(
    private val repository: EventRepository,
    private val user: UserProfile
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    /** Create, edit and delete buttons are only shown to Administrators and Pastors. */
    val canManage: Boolean get() = user.role == Role.ADMINISTRATOR || user.role == Role.PASTOR
    /** Only a Church Member linked to a member record can register. */
    val canRegister: Boolean get() = user.role == Role.CHURCH_MEMBER && user.memberId != null

    init {
        loadEvents()
    }

    /** Loads the events and, for a member, the ids of the events they registered for. */
    fun loadEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val events = repository.getEvents()
                val registeredIds = if (user.memberId != null) {
                    repository.getRegisteredEventIds(user.memberId!!)
                } else emptyList()

                _uiState.update { it.copy(isLoading = false, events = events, registeredEventIds = registeredIds) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "loadEvents failed", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load events.") }
            }
        }
    }

    /** Registers the member for [eventId], or unregisters them if they were already registered. */
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
                loadEvents()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "toggleRegistration failed", e)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to update registration.") }
            }
        }
    }

    /** Deletes an event (the screen asks for confirmation first). */
    fun deleteEvent(eventId: String) {
        if (!canManage) return
        viewModelScope.launch {
            try {
                repository.deleteEvent(eventId)
                loadEvents()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "deleteEvent failed", e)
                _uiState.update { it.copy(errorMessage = "Failed to delete event.") }
            }
        }
    }

    /** Saves a new event. The form has already checked the fields and blocked past dates. */
    fun createEvent(event: Event) {
        if (!canManage) return
        viewModelScope.launch {
            try {
                repository.createEvent(event)
                loadEvents()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "createEvent failed", e)
                _uiState.update { it.copy(errorMessage = "Failed to create event.") }
            }
        }
    }

    /** Saves the changes to an existing event. */
    fun updateEvent(event: Event) {
        if (!canManage) return
        viewModelScope.launch {
            try {
                repository.updateEvent(event)
                loadEvents()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppLog.e(TAG, "updateEvent failed", e)
                _uiState.update { it.copy(errorMessage = "Failed to update event.") }
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

private const val TAG = "Events"
