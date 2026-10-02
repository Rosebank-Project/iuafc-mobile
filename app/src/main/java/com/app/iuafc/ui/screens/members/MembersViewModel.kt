package com.app.iuafc.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.repository.MemberRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MemberSort(val label: String) {
    NAME("Name"),
    NEWEST("Newest"),
    MINISTRY("Ministry"),
}

data class MembersUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val members: List<Member> = emptyList(),
    val ministries: List<Ministry> = emptyList(),
    val query: String = "",
    val ministryFilter: String? = null,
    val sort: MemberSort = MemberSort.NAME,
) {
    val visibleMembers: List<Member>
        get() = filterMembers(members, ministries, query, ministryFilter, sort)

    fun ministryName(ministryId: String?): String? = ministries.firstOrNull { it.id == ministryId }?.name
}

/** Search by name, email or phone (like the website), filter by ministry and sort. */
fun filterMembers(
    members: List<Member>,
    ministries: List<Ministry>,
    query: String,
    ministryFilter: String?,
    sort: MemberSort,
): List<Member> {
    val q = query.trim().lowercase()
    val ministryNames = ministries.associate { it.id to it.name }
    return members
        .filter { ministryFilter == null || it.ministryId == ministryFilter }
        .filter {
            q.isEmpty() ||
                it.fullName.lowercase().contains(q) ||
                it.email.lowercase().contains(q) ||
                it.contactNumber.replace(" ", "").contains(q.replace(" ", ""))
        }
        .sortedWith(
            when (sort) {
                MemberSort.NAME -> compareBy { it.fullName.lowercase() }
                MemberSort.NEWEST -> compareByDescending { it.joinDate }
                MemberSort.MINISTRY -> compareBy<Member> { ministryNames[it.ministryId] ?: "~" }.thenBy { it.fullName }
            },
        )
}

class MembersViewModel(private val repository: MemberRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

    /** Called every time the screen is shown, so changes made in the form appear. */
    fun load() {
        viewModelScope.launch {
            // Only show the full-screen spinner the first time.
            _uiState.update { it.copy(isLoading = it.members.isEmpty(), errorMessage = null) }
            try {
                val members = repository.getMembers()
                val ministries = repository.getMinistries()
                _uiState.update { it.copy(isLoading = false, members = members, ministries = ministries) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load members. Please try again.") }
            }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onMinistryFilterChange(ministryId: String?) = _uiState.update { it.copy(ministryFilter = ministryId) }

    fun onSortChange(sort: MemberSort) = _uiState.update { it.copy(sort = sort) }

    companion object {
        val Factory = viewModelFactory {
            initializer { MembersViewModel(appContainer.memberRepository) }
        }
    }
}
