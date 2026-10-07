package com.app.iuafc.ui.screens.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.repository.AnnouncementRepository
import com.app.iuafc.data.repository.MockAnnouncementRepository
import com.app.iuafc.ui.common.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnnouncementViewModel(
    private val repository: AnnouncementRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading)

    val uiState: StateFlow<UiState<List<Announcement>>> =
        _uiState.asStateFlow()

    init {
        loadAnnouncements()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            try {
                val announcements = repository.getAnnouncements()
                _uiState.value = UiState.Success(announcements)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to load announcements.",
                )
            }
        }
    }

    fun createAnnouncement(
        title: String,
        message: String,
        createdBy: String,
    ) {
        viewModelScope.launch {
            try {
                repository.createAnnouncement(
                    title = title,
                    message = message,
                    createdBy = createdBy,
                )

                loadAnnouncements()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to create announcement.",
                )
            }
        }
    }

    fun updateAnnouncement(
        announcement: Announcement,
    ) {
        viewModelScope.launch {
            try {
                repository.updateAnnouncement(announcement)
                loadAnnouncements()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to update announcement.",
                )
            }
        }
    }

    fun deleteAnnouncement(
        announcementId: String,
    ) {
        viewModelScope.launch {
            try {
                repository.deleteAnnouncement(announcementId)
                loadAnnouncements()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to delete announcement.",
                )
            }
        }
    }

    companion object {

        fun factory(): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            AnnouncementViewModel::class.java,
                        )
                    ) {
                        return AnnouncementViewModel(
                            repository = MockAnnouncementRepository(),
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}",
                    )
                }
            }
    }
}