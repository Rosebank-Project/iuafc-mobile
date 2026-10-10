package com.app.iuafc.ui.screens.features

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Gender
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.MinistryActivity
import com.app.iuafc.data.model.PaymentMethod
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.SaveResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class MinistriesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveVersion: Int = 0,
    val errorMessage: String? = null,
    val ministries: List<Ministry> = emptyList(),
)

class MinistriesViewModel(private val repository: ChurchFeaturesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MinistriesUiState())
    val uiState: StateFlow<MinistriesUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.ministries.isEmpty(), errorMessage = null) }
        try {
            _uiState.update { it.copy(isLoading = false, ministries = repository.getMinistries()) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load ministries. Please try again.") }
        }
    }

    fun save(ministry: Ministry) = viewModelScope.launch {
        if (ministry.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a ministry name.") }
            return@launch
        }
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            when (val result = repository.saveMinistry(ministry)) {
                SaveResult.Success -> _uiState.update {
                    it.copy(isSaving = false, saveVersion = it.saveVersion + 1, ministries = repository.getMinistries())
                }
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isSaving = false, errorMessage = "Could not save the ministry. Please try again.") }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { MinistriesViewModel(appContainer.churchFeaturesRepository) }
        }
    }
}

data class ActivitiesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveVersion: Int = 0,
    val errorMessage: String? = null,
    val activities: List<MinistryActivity> = emptyList(),
    val ministries: List<Ministry> = emptyList(),
)

class ActivitiesViewModel(
    private val user: UserProfile,
    private val repository: ChurchFeaturesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()
    val canReview: Boolean get() = user.role == Role.ADMINISTRATOR || user.role == Role.PASTOR
    val canSubmit: Boolean get() = user.role == Role.MINISTRY_LEADER

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.activities.isEmpty(), errorMessage = null) }
        try {
            val activities = repository.getActivities()
            val ministries = repository.getMinistries()
            _uiState.update { it.copy(isLoading = false, activities = activities, ministries = ministries) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load activities. Please try again.") }
        }
    }

    fun visibleActivities(): List<MinistryActivity> {
        val activities = _uiState.value.activities
        return if (user.role == Role.MINISTRY_LEADER) {
            activities.filter { it.ministryId == user.ministryId }
        } else {
            activities
        }.sortedWith(compareBy<MinistryActivity> { it.proposedDate }.thenBy { it.title })
    }

    fun submit(ministryId: String, title: String, description: String, date: String, venue: String) =
        viewModelScope.launch {
            if (!canSubmit) {
                _uiState.update { it.copy(errorMessage = "Only Ministry Leaders can submit activities.") }
                return@launch
            }
            val parsedDate = parseDate(date)
            if (ministryId.isBlank() || title.isBlank() || description.isBlank() || venue.isBlank() || parsedDate == null) {
                _uiState.update { it.copy(errorMessage = "Complete every field with a valid date.") }
                return@launch
            }
            if (user.ministryId != null && user.ministryId != ministryId) {
                _uiState.update { it.copy(errorMessage = "You can only submit activities for your ministry.") }
                return@launch
            }
            if (user.role == Role.MINISTRY_LEADER && user.ministryId == null) {
                _uiState.update { it.copy(errorMessage = "Your account is not assigned to a ministry. Contact an Administrator.") }
                return@launch
            }
            val activity = MinistryActivity(
                id = "",
                ministryId = ministryId,
                title = title.trim(),
                description = description.trim(),
                proposedDate = parsedDate.toString(),
                venue = venue.trim(),
                submittedBy = user.id,
                submittedAt = java.time.Instant.now().toString(),
            )
            runSave { repository.saveActivity(activity) }
        }

    fun review(activity: MinistryActivity, status: ActivityStatus, note: String) = viewModelScope.launch {
        if (!canReview || activity.status != ActivityStatus.Pending ||
            status == ActivityStatus.Pending
        ) {
            _uiState.update { it.copy(errorMessage = "This activity cannot be reviewed.") }
            return@launch
        }
        val reviewed = activity.copy(
            status = status,
            reviewedBy = user.id,
            reviewedAt = java.time.Instant.now().toString(),
            reviewNote = note.trim().ifEmpty { null },
        )
        runSave { repository.reviewActivity(reviewed) }
    }

    private suspend fun runSave(operation: suspend () -> SaveResult) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            when (val result = operation()) {
                SaveResult.Success -> {
                    val activities = repository.getActivities()
                    _uiState.update { it.copy(isSaving = false, saveVersion = it.saveVersion + 1, activities = activities) }
                }
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isSaving = false, errorMessage = "Could not save the activity. Please try again.") }
        }
    }
}

data class DonationsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveVersion: Int = 0,
    val errorMessage: String? = null,
    val donations: List<Donation> = emptyList(),
    val members: List<Member> = emptyList(),
)

class DonationsViewModel(
    private val user: UserProfile,
    private val repository: ChurchFeaturesRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DonationsUiState())
    val uiState: StateFlow<DonationsUiState> = _uiState.asStateFlow()
    val canRecord: Boolean get() = user.role == Role.ADMINISTRATOR || user.role == Role.FINANCE_OFFICER

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = it.donations.isEmpty(), errorMessage = null) }
        try {
            val allDonations = repository.getDonations()
            val members = memberRepository.getMembers()
            val visible = if (user.role == Role.CHURCH_MEMBER) {
                allDonations.filter { it.memberId == user.memberId }
            } else {
                allDonations
            }
            _uiState.update { it.copy(isLoading = false, donations = visible, members = members) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load donations. Please try again.") }
        }
    }

    fun record(memberId: String, amountText: String, type: DonationType,
               method: PaymentMethod, date: String) = viewModelScope.launch {
        val amount = amountText.trim().replace(',', '.').toDoubleOrNull()
        val parsedDate = parseDate(date)
        if (!canRecord || _uiState.value.members.none { it.id == memberId } ||
            amount == null || !amount.isFinite() || amount <= 0 || parsedDate == null
        ) {
            _uiState.update { it.copy(errorMessage = "Choose a member, enter an amount greater than zero and select a valid date.") }
            return@launch
        }
        val donation = Donation(
            id = "",
            memberId = memberId,
            amount = amount,
            donationType = type,
            paymentMethod = method,
            donationDate = parsedDate.toString(),
            recordedBy = user.id,
            receiptNumber = "",
        )
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            when (val result = repository.saveDonation(donation)) {
                SaveResult.Success -> _uiState.update {
                    it.copy(isSaving = false, saveVersion = it.saveVersion + 1, donations = repository.getDonations())
                }
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _uiState.update { it.copy(isSaving = false, errorMessage = "Could not record the donation. Please try again.") }
        }
    }
}

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

private fun parseDate(value: String): LocalDate? =
    try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
