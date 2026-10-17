package com.app.iuafc.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.MemberRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val members: List<Member> = emptyList(),
    val ministries: List<Ministry> = emptyList(),
    val donations: List<Donation> = emptyList(),
    val events: List<Event> = emptyList(),
    val attendance: List<Attendance> = emptyList(),
)

class ReportsViewModel(
    private val featureRepository: ChurchFeaturesRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.members.isEmpty(), errorMessage = null) }
        try {
            val members = memberRepository.getMembers()
            val ministries = featureRepository.getMinistries()
            val donations = featureRepository.getDonations()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    members = members,
                    ministries = ministries,
                    donations = donations,
                    // Reading MockData for events and attendance is expected until
                    // Rolph's event and attendance repositories exist (#4).
                    events = MockData.events,
                    attendance = MockData.attendance,
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load reports. Please try again.") }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ReportsViewModel(appContainer.churchFeaturesRepository, appContainer.memberRepository)
            }
        }
    }
}
