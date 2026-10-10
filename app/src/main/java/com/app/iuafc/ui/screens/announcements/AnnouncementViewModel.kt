
package com.app.iuafc.ui.screens.announcements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.repository.AnnouncementRepository
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
                _uiState.value = UiState.Success(
                    repository.getAnnouncements(),
                )
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
                refreshAnnouncements()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to create announcement.",
                )
            }
        }
    }

    fun updateAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            try {
                repository.updateAnnouncement(announcement)
                refreshAnnouncements()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to update announcement.",
                )
            }
        }
    }

    fun deleteAnnouncement(announcementId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAnnouncement(announcementId)
                refreshAnnouncements()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "Unable to delete announcement.",
                )
            }
        }
    }

    private suspend fun refreshAnnouncements() {
        _uiState.value = UiState.Success(
            repository.getAnnouncements(),
        )
    }

    companion object {

        /**
         * Creates the ViewModel with the application's shared repository.
         */
        val Factory = viewModelFactory {
            initializer {
                AnnouncementViewModel(
                    repository = appContainer.announcementRepository,
                )
            }
        }

        /**
         * Retained for screens that use the factory() function.
         */
        fun factory() = Factory
    }
}