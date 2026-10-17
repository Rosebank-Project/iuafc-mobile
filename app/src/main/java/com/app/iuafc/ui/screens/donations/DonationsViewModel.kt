package com.app.iuafc.ui.screens.donations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.PaymentMethod
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.util.Validators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

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

    fun record(
        memberId: String,
        amountText: String,
        type: DonationType,
        method: PaymentMethod,
        date: String,
    ) = viewModelScope.launch {
        val dateError = Validators.pastDate(date, "Donation date")
        val amount = amountText.trim().replace(',', '.').toDoubleOrNull()
        val parsedDate = parseDate(date)
        if (!canRecord || _uiState.value.members.none { it.id == memberId } ||
            amount == null || !amount.isFinite() || amount <= 0 || parsedDate == null || dateError != null
        ) {
            _uiState.update {
                it.copy(errorMessage = dateError ?: "Choose a member, enter an amount greater than zero and select a valid date.")
            }
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

private fun parseDate(value: String): LocalDate? =
    try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }
