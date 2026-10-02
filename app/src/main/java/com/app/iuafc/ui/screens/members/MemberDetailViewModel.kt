package com.app.iuafc.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.navigation.MemberDetailRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemberDetailUiState(
    val isLoading: Boolean = true,
    val member: Member? = null,
    val ministryName: String? = null,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    /** True after the member was removed, so the screen can go back. */
    val deleted: Boolean = false,
)

class MemberDetailViewModel(
    private val memberId: String,
    private val repository: MemberRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemberDetailUiState())
    val uiState: StateFlow<MemberDetailUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.member == null, errorMessage = null) }
            try {
                val member = repository.getMember(memberId)
                val ministry = repository.getMinistries().firstOrNull { it.id == member?.ministryId }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        member = member,
                        ministryName = ministry?.name,
                        errorMessage = if (member == null) "This member no longer exists." else null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load the member. Please try again.") }
            }
        }
    }

    fun delete() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            when (val result = repository.deleteMember(memberId)) {
                SaveResult.Success -> _uiState.update { it.copy(isDeleting = false, deleted = true) }
                is SaveResult.Error -> _uiState.update { it.copy(isDeleting = false, errorMessage = result.message) }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val route = createSavedStateHandle().toRoute<MemberDetailRoute>()
                MemberDetailViewModel(route.memberId, appContainer.memberRepository)
            }
        }
    }
}
