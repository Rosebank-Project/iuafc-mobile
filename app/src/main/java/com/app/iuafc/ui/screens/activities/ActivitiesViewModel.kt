package com.app.iuafc.ui.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.MinistryActivity
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.SaveResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

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
            if (parsedDate.isBefore(LocalDate.now())) {
                _uiState.update { it.copy(errorMessage = "Proposed date cannot be in the past.") }
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

private fun parseDate(value: String): LocalDate? =
    try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
