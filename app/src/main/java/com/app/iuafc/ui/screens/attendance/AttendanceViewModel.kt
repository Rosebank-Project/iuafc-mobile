package com.app.iuafc.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.AttendanceStatus
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.AttendanceRepository
import com.app.iuafc.data.repository.EventRepository
import com.app.iuafc.data.repository.MemberRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val events: List<Event> = emptyList(),
    val selectedEventId: String? = null,
    val selectedEventDate: String? = null, // To fix Bug 3
    val members: List<Member> = emptyList(),
    val attendanceMap: Map<String, AttendanceStatus?> = emptyMap(), // Nullable for Bug 7
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class AttendanceViewModel(
    private val eventRepo: EventRepository,
    private val memberRepo: MemberRepository,
    private val attendanceRepo: AttendanceRepository,
    private val user: UserProfile
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    val canManage: Boolean get() = user.role == Role.ADMINISTRATOR || user.role == Role.PASTOR || user.role == Role.MINISTRY_LEADER

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val events = eventRepo.getEvents()
                var members = memberRepo.getMembers()

                // Bug 6: If Ministry Leader, only show their ministry's members
                if (user.role == Role.MINISTRY_LEADER && user.ministryId != null) {
                    members = members.filter { it.ministryId == user.ministryId }
                }

                _uiState.update { it.copy(isLoading = false, events = events, members = members) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load data.") }
            }
        }
    }

    fun selectEvent(eventId: String) {
        val event = _uiState.value.events.find { it.id == eventId }
        _uiState.update {
            it.copy(
                selectedEventId = eventId,
                selectedEventDate = event?.date, // Store the event's date
                saveSuccess = false,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val existing = attendanceRepo.getAttendanceForEvent(eventId)
                // Bug 7: Default to null (no status) instead of Absent
                val map = _uiState.value.members.associate { member ->
                    val status = existing.find { it.memberId == member.id }?.status
                    member.id to status
                }
                _uiState.update { it.copy(attendanceMap = map) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load attendance records.") }
            }
        }
    }

    // Bug 7: Accept nullable status
    fun toggleStatus(memberId: String, status: AttendanceStatus?) {
        val currentMap = _uiState.value.attendanceMap.toMutableMap()
        currentMap[memberId] = status
        _uiState.update { it.copy(attendanceMap = currentMap, saveSuccess = false) }
    }

    fun saveAttendance() {
        val eventId = _uiState.value.selectedEventId ?: return
        val eventDate = _uiState.value.selectedEventDate ?: return

        // Bug 7: Validation - ensure all members have a status
        if (_uiState.value.attendanceMap.values.any { it == null }) {
            _uiState.update { it.copy(errorMessage = "Please mark all members Present or Absent before saving.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val records = _uiState.value.attendanceMap.mapNotNull { (memberId, status) ->
                    if (status == null) return@mapNotNull null
                    Attendance(
                        id = "att-${System.currentTimeMillis()}-$memberId",
                        memberId = memberId,
                        eventId = eventId,
                        attendanceDate = eventDate, // Bug 3: Use event date instead of LocalDate.now()
                        status = status
                    )
                }
                attendanceRepo.saveAttendance(records)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save attendance.") }
            }
        }
    }

    companion object {
        fun factory(user: UserProfile) = viewModelFactory {
            initializer {
                AttendanceViewModel(
                    eventRepo = appContainer.eventRepository,
                    memberRepo = appContainer.memberRepository,
                    attendanceRepo = appContainer.attendanceRepository,
                    user = user
                )
            }
        }
    }
}