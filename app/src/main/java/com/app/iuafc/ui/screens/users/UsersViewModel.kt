package com.app.iuafc.ui.screens.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.data.repository.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The role being edited in the "Change role" dialog. */
data class RoleEdit(
    val user: UserProfile,
    val role: Role = user.role,
    val memberId: String? = user.memberId,
    val ministryId: String? = user.ministryId,
    val error: String? = null,
    val isSaving: Boolean = false,
)

data class UsersUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val users: List<UserProfile> = emptyList(),
    val members: List<Member> = emptyList(),
    val ministries: List<Ministry> = emptyList(),
    val query: String = "",
    val roleFilter: Role? = null,
    val editing: RoleEdit? = null,
    /** Short confirmation shown after a change (e.g. in a snackbar). */
    val message: String? = null,
) {
    val visibleUsers: List<UserProfile>
        get() {
            val q = query.trim().lowercase()
            return users
                .filter { roleFilter == null || it.role == roleFilter }
                .filter {
                    q.isEmpty() || it.fullName.lowercase().contains(q) ||
                        it.email.lowercase().contains(q) || it.role.displayName.lowercase().contains(q)
                }
                .sortedBy { it.fullName.lowercase() }
        }

    val roleCounts: Map<Role, Int>
        get() = Role.entries.associateWith { role -> users.count { it.role == role } }
}

/** Administrators manage the role of every account (accounts are created by registering). */
class UsersViewModel(
    private val users: UserRepository,
    private val members: MemberRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.users.isEmpty(), errorMessage = null) }
            try {
                val userList = users.getUsers()
                val memberList = members.getMembers()
                val ministryList = members.getMinistries()
                _uiState.update {
                    it.copy(isLoading = false, users = userList, members = memberList, ministries = ministryList)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load users. Please try again.") }
            }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }
    fun onRoleFilterChange(role: Role?) = _uiState.update { it.copy(roleFilter = role) }
    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    fun startEditing(user: UserProfile) = _uiState.update { it.copy(editing = RoleEdit(user)) }
    fun cancelEditing() = _uiState.update { it.copy(editing = null) }

    fun onEditRoleChange(role: Role) = updateEdit { copy(role = role, error = null) }
    fun onEditMemberChange(memberId: String?) = updateEdit { copy(memberId = memberId, error = null) }
    fun onEditMinistryChange(ministryId: String?) = updateEdit { copy(ministryId = ministryId, error = null) }

    fun saveRole() {
        val edit = _uiState.value.editing ?: return
        if (edit.isSaving) return

        if (edit.role == Role.MINISTRY_LEADER && edit.ministryId == null) {
            updateEdit { copy(error = "Choose the ministry this leader manages.") }
            return
        }

        updateEdit { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val result = users.updateRole(edit.user.id, edit.role, edit.memberId, edit.ministryId)) {
                SaveResult.Success -> {
                    val updated = users.getUsers()
                    _uiState.update {
                        it.copy(
                            users = updated,
                            editing = null,
                            message = "${edit.user.fullName} is now ${edit.role.displayName}",
                        )
                    }
                }
                is SaveResult.Error -> updateEdit { copy(isSaving = false, error = result.message) }
            }
        }
    }

    private fun updateEdit(change: RoleEdit.() -> RoleEdit) =
        _uiState.update { state -> state.copy(editing = state.editing?.change()) }

    companion object {
        val Factory = viewModelFactory {
            initializer { UsersViewModel(appContainer.userRepository, appContainer.memberRepository) }
        }
    }
}
