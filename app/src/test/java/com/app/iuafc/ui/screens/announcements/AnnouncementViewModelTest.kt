package com.app.iuafc.ui.screens.announcements

import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.repository.MockAnnouncementRepository
import com.app.iuafc.testutil.MainDispatcherRule
import com.app.iuafc.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class AnnouncementViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun createViewModel(): AnnouncementViewModel {
        return AnnouncementViewModel(
            repository = MockAnnouncementRepository(),
        )
    }

    @Test
    fun loadAnnouncements_returnsExistingAnnouncements() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value

        assertFalse(state is UiState.Loading)
        assertTrueAnnouncementsLoaded(state)
    }

    @Test
    fun createAnnouncement_addsAnnouncementToList() {
        val viewModel = createViewModel()

        viewModel.createAnnouncement(
            title = "Unit Test Announcement",
            message = "Created during a unit test",
            createdBy = "user-1",
        )

        val state = viewModel.uiState.value
        assertTrueAnnouncementsLoaded(state)

        val announcements = (state as UiState.Success).data

        assertNotNull(
            announcements.find {
                it.title == "Unit Test Announcement"
            },
        )
    }

    @Test
    fun updateAnnouncement_changesAnnouncementDetails() {
        val viewModel = createViewModel()

        viewModel.createAnnouncement(
            title = "Original title",
            message = "Original message",
            createdBy = "user-1",
        )

        val createdState = viewModel.uiState.value
        assertTrueAnnouncementsLoaded(createdState)

        val created = (createdState as UiState.Success).data.first {
            it.title == "Original title"
        }

        viewModel.updateAnnouncement(
            created.copy(
                title = "Updated title",
                message = "Updated message",
            ),
        )

        val updatedState = viewModel.uiState.value
        assertTrueAnnouncementsLoaded(updatedState)

        val updated = (updatedState as UiState.Success).data.first {
            it.id == created.id
        }

        assertEquals("Updated title", updated.title)
        assertEquals("Updated message", updated.message)
    }

    @Test
    fun deleteAnnouncement_removesAnnouncementFromList() {
        val viewModel = createViewModel()

        viewModel.createAnnouncement(
            title = "Announcement to delete",
            message = "This announcement will be deleted",
            createdBy = "user-1",
        )

        val createdState = viewModel.uiState.value
        assertTrueAnnouncementsLoaded(createdState)

        val created = (createdState as UiState.Success).data.first {
            it.title == "Announcement to delete"
        }

        viewModel.deleteAnnouncement(created.id)

        val deletedState = viewModel.uiState.value
        assertTrueAnnouncementsLoaded(deletedState)

        val remaining = (deletedState as UiState.Success).data

        assertNull(
            remaining.find {
                it.id == created.id
            },
        )
    }

    private fun assertTrueAnnouncementsLoaded(
        state: UiState<List<Announcement>>,
    ) {
        org.junit.Assert.assertTrue(
            "Expected Success state, got $state",
            state is UiState.Success,
        )
    }
}