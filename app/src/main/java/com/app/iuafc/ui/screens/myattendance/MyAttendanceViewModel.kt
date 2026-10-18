package com.app.iuafc.ui.screens.myattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.AttendanceRepository
import com.app.iuafc.data.repository.EventRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyAttendanceUiModel(
    val attendance: Attendance,
    val eventName: String
)

class MyAttendanceViewModel(
    private val user: UserProfile,
    private val attendanceRepo: AttendanceRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _attendanceList = MutableStateFlow<List<MyAttendanceUiModel>>(emptyList())
    val attendanceList: StateFlow<List<MyAttendanceUiModel>> = _attendanceList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyAttendance()
    }

    private fun loadMyAttendance() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (user.memberId == null) {
                    _attendanceList.value = emptyList()
                } else {
                    // Bug 4: Read from Repository, not MockData
                    val myRecords = attendanceRepo.getAttendanceForMember(user.memberId)
                    val allEvents = eventRepo.getEvents()

                    val combinedData = myRecords.map { record ->
                        val event = allEvents.find { it.id == record.eventId }?.name ?: "Unknown Event"
                        MyAttendanceUiModel(attendance = record, eventName = event)
                    }
                    _attendanceList.value = combinedData
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _attendanceList.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        fun factory(user: UserProfile) = viewModelFactory {
            initializer {
                MyAttendanceViewModel(
                    user = user,
                    attendanceRepo = appContainer.attendanceRepository,
                    eventRepo = appContainer.eventRepository
                )
            }
        }
    }
}