package com.app.iuafc.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Attendance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// We create a UI model so we can show the Member's Name and Event's Name, not just their IDs
data class AttendanceUiModel(
    val attendance: Attendance,
    val memberName: String,
    val eventName: String
)

class AttendanceViewModel : ViewModel() {

    private val _attendanceList = MutableStateFlow<List<AttendanceUiModel>>(emptyList())
    val attendanceList: StateFlow<List<AttendanceUiModel>> = _attendanceList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAttendance()
    }

    private fun loadAttendance() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(500)

            // Combine MockData.attendance with Member names and Event names
            val combinedData = MockData.attendance.map { record ->
                val member = MockData.memberName(record.memberId) ?: "Unknown Member"
                val event = MockData.events.find { it.id == record.eventId }?.name ?: "Unknown Event"

                AttendanceUiModel(
                    attendance = record,
                    memberName = member,
                    eventName = event
                )
            }

            _attendanceList.value = combinedData
            _isLoading.value = false
        }
    }
}