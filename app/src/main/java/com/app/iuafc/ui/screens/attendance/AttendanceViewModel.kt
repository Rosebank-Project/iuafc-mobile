package com.app.iuafc.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.*
import com.app.iuafc.data.repository.AttendanceRepository
import com.app.iuafc.data.repository.EventRepository
import com.app.iuafc.data.repository.MemberRepository
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
    val members: List<Member> = emptyList(),
    val attendanceMap: Map<String, AttendanceStatus> = emptyMap(), // memberId -> Status
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false
)

class AttendanceViewModel(
    private val eventRepo: EventRepository,
    private val memberRepo: MemberRepository,
    private val attendanceRepo: AttendanceRepository,
    private val user: UserProfile
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendanceUiState>(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    val canManage: Boolean get() = user.role in listOf(Role.ADMINISTRATOR, Role.PASTOR, Role.MINISTRY_LEADER)

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val events = eventRepo.getEvents()
                val members = memberRepo.getMembers()
                _uiState.update {
                    it.copy(isLoading = false, events = events, members = members)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load data.") }
            }
        }
    }

    fun selectEvent(eventId: String) {
        _uiState.update { it.copy(selectedEventId = eventId, saveSuccess = false) }
        viewModelScope.launch {
            try {
                val existing = attendanceRepo.getAttendanceForEvent(eventId)
                // Default everyone to Absent if no record exists, otherwise use existing status
                val map = _uiState.value.members.associate { member ->
                    val status = existing.find { it.memberId == member.id }?.status ?: AttendanceStatus.Absent
                    member.id to status
                }
                _uiState.update { it.copy(attendanceMap = map) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load attendance records.") }
            }
        }
    }

    fun toggleStatus(memberId: String, status: AttendanceStatus) {
        val currentMap = _uiState.value.attendanceMap.toMutableMap()
        currentMap[memberId] = status
        _uiState.update { it.copy(attendanceMap = currentMap, saveSuccess = false) }
    }

    fun saveAttendance() {
        val eventId = _uiState.value.selectedEventId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val records = _uiState.value.attendanceMap.map { (memberId, status) ->
                    Attendance(
                        id = "att-${System.currentTimeMillis()}-$memberId", // Generate simple id
                        memberId = memberId,
                        eventId = eventId,
                        attendanceDate = java.time.LocalDate.now().toString(),
                        status = status
                    )
                }
                attendanceRepo.saveAttendance(records)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
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