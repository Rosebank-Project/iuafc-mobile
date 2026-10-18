
package com.app.iuafc.ui.screens.myattendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyAttendanceUiModel(
    val attendance: Attendance,
    val eventName: String
)

class MyAttendanceViewModel(private val user: UserProfile) : ViewModel() {

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
            delay(500)

            // Filter MockData so we only get the records for THIS user's memberId
            val myRecords = MockData.attendance.filter {
                user.memberId != null && it.memberId == user.memberId
            }

            val combinedData = myRecords.map { record ->
                val event = MockData.events.find { it.id == record.eventId }?.name ?: "Unknown Event"
                MyAttendanceUiModel(
                    attendance = record,
                    eventName = event
                )
            }

            _attendanceList.value = combinedData
            _isLoading.value = false
        }
    }

    companion object {
        fun factory(user: UserProfile) = viewModelFactory {
            initializer {
                MyAttendanceViewModel(user)
            }
        }
    }
}