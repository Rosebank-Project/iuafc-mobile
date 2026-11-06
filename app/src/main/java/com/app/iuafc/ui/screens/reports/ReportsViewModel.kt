package com.app.iuafc.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.AttendanceRepository
import com.app.iuafc.data.repository.EventRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.MockAttendanceRepository
import com.app.iuafc.data.repository.MockEventRepository
import com.app.iuafc.util.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The raw data the reports are calculated from (totals and charts are worked out in ReportsScreen). */
data class ReportsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val members: List<Member> = emptyList(),
    val ministries: List<Ministry> = emptyList(),
    val donations: List<Donation> = emptyList(),
    val events: List<Event> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
)

/**
 * Loads everything the Reports screen needs: members, ministries, donations, events and attendance.
 * Administrators and Pastors can open it; Row Level Security lets them read every table used here.
 *
 * The event and attendance repositories default to the mock versions so older tests still compile.
 *
 * Reference: Android Developers. 2026. ViewModel overview. [Online]. Available at:
 * https://developer.android.com/topic/libraries/architecture/viewmodel [Accessed 6 November 2026].
 */
class ReportsViewModel(
    private val featureRepository: ChurchFeaturesRepository,
    private val memberRepository: MemberRepository,
    private val eventRepository: EventRepository = MockEventRepository(),
    private val attendanceRepository: AttendanceRepository = MockAttendanceRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    /**
     * Loads (or reloads) all the data. The loading spinner only shows the first time, so a refresh
     * keeps the current report on screen.
     */
    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.members.isEmpty(), errorMessage = null) }
        try {
            val members = memberRepository.getMembers()
            val ministries = featureRepository.getMinistries()
            val donations = featureRepository.getDonations()
            val events = eventRepository.getEvents()
            val attendance = attendanceRepository.getAttendance()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    members = members,
                    ministries = ministries,
                    donations = donations,
                    events = events,
                    attendance = attendance,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e(TAG, "load failed", e)
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load reports. Please try again.") }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ReportsViewModel(
                    appContainer.churchFeaturesRepository,
                    appContainer.memberRepository,
                    appContainer.eventRepository,
                    appContainer.attendanceRepository,
                )
            }
        }
    }
}

private const val TAG = "Reports"
