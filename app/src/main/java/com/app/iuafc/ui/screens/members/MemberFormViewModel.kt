package com.app.iuafc.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Gender
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.navigation.MemberFormRoute
import com.app.iuafc.util.Validators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MemberFormUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val fullName: String = "",
    val gender: Gender? = null,
    val dateOfBirth: String = "",
    val contactNumber: String = "",
    val email: String = "",
    val address: String = "",
    val ministryId: String? = null,
    val joinDate: String = "",
    val ministries: List<Ministry> = emptyList(),
    val errors: Map<MemberField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    /** True after saving, so the screen can go back. */
    val saved: Boolean = false,
)

enum class MemberField { FULL_NAME, GENDER, DATE_OF_BIRTH, CONTACT_NUMBER, EMAIL, ADDRESS, JOIN_DATE }

/** Add a member when [memberId] is null, edit it otherwise. Only Administrators open this screen. */
class MemberFormViewModel(
    private val memberId: String?,
    private val repository: MemberRepository,
    today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MemberFormUiState(isEditing = memberId != null, isLoading = memberId != null, joinDate = today.toString()),
    )
    val uiState: StateFlow<MemberFormUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val ministries = repository.getMinistries()
                val member = memberId?.let { repository.getMember(it) }
                _uiState.update { state ->
                    val loaded = member?.let {
                        state.copy(
                            fullName = it.fullName, gender = it.gender, dateOfBirth = it.dateOfBirth,
                            contactNumber = it.contactNumber, email = it.email, address = it.address,
                            ministryId = it.ministryId, joinDate = it.joinDate,
                        )
                    } ?: state
                    loaded.copy(
                        isLoading = false,
                        ministries = ministries,
                        errorMessage = if (memberId != null && member == null) "This member no longer exists." else null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load the form. Please try again.") }
            }
        }
    }

    fun onFullNameChange(value: String) = edit(MemberField.FULL_NAME) { copy(fullName = value) }
    fun onGenderChange(value: Gender) = edit(MemberField.GENDER) { copy(gender = value) }
    fun onDateOfBirthChange(value: String) = edit(MemberField.DATE_OF_BIRTH) { copy(dateOfBirth = value) }
    fun onContactNumberChange(value: String) = edit(MemberField.CONTACT_NUMBER) { copy(contactNumber = value) }
    fun onEmailChange(value: String) = edit(MemberField.EMAIL) { copy(email = value) }
    fun onAddressChange(value: String) = edit(MemberField.ADDRESS) { copy(address = value) }
    fun onJoinDateChange(value: String) = edit(MemberField.JOIN_DATE) { copy(joinDate = value) }
    fun onMinistryChange(value: String?) = _uiState.update { it.copy(ministryId = value) }

    fun save() {
        val state = _uiState.value
        if (state.isSaving || state.isLoading) return

        val errors = validate(state)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        val member = Member(
            id = memberId.orEmpty(),
            fullName = state.fullName.trim(),
            gender = state.gender!!,
            dateOfBirth = state.dateOfBirth,
            contactNumber = state.contactNumber.trim(),
            email = state.email.trim(),
            address = state.address.trim(),
            ministryId = state.ministryId,
            joinDate = state.joinDate,
        )
        viewModelScope.launch {
            when (val result = repository.saveMember(member)) {
                SaveResult.Success -> _uiState.update { it.copy(isSaving = false, saved = true) }
                is SaveResult.Error -> _uiState.update { it.copy(isSaving = false, errorMessage = result.message) }
            }
        }
    }

    private fun validate(state: MemberFormUiState): Map<MemberField, String> = buildMap {
        Validators.required(state.fullName, "Full name")?.let { put(MemberField.FULL_NAME, it) }
        if (state.gender == null) put(MemberField.GENDER, "Select a gender")
        Validators.pastDate(state.dateOfBirth, "Date of birth")?.let { put(MemberField.DATE_OF_BIRTH, it) }
        Validators.phone(state.contactNumber)?.let { put(MemberField.CONTACT_NUMBER, it) }
        Validators.email(state.email)?.let { put(MemberField.EMAIL, it) }
        Validators.required(state.address, "Address")?.let { put(MemberField.ADDRESS, it) }
        Validators.pastDate(state.joinDate, "Join date")?.let { put(MemberField.JOIN_DATE, it) }
    }

    private fun edit(field: MemberField, change: MemberFormUiState.() -> MemberFormUiState) =
        _uiState.update { it.change().copy(errors = it.errors - field, errorMessage = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val route = createSavedStateHandle().toRoute<MemberFormRoute>()
                MemberFormViewModel(route.memberId, appContainer.memberRepository)
            }
        }
    }
}
