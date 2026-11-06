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
import com.app.iuafc.util.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One row of My Attendance: the record and the name of its event. */
data class MyAttendanceUiModel(
    val attendance: Attendance,
    val eventName: String
)

/**
 * A Church Member's own attendance history. The records only hold the event id, so they are
 * joined with the events to show the event name. An account that is not linked to a member
 * record (see Users) has no attendance.
 *
 * Reference: Android Developers. 2026. ViewModel overview. [Online]. Available at:
 * https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 November 2026].
 */
class MyAttendanceViewModel(
    private val user: UserProfile,
    private val attendanceRepo: AttendanceRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _attendanceList = MutableStateFlow<List<MyAttendanceUiModel>>(emptyList())
    val attendanceList: StateFlow<List<MyAttendanceUiModel>> = _attendanceList.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadMyAttendance()
    }

    /** Loads the member's records and the events, and combines them. Also used by "Try again". */
    fun loadMyAttendance() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                if (user.memberId == null) {
                    _attendanceList.value = emptyList()
                } else {
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
                AppLog.e(TAG, "loadMyAttendance failed", e)
                _errorMessage.value = "Could not load your attendance. Please try again."
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

private const val TAG = "MyAttendance"
