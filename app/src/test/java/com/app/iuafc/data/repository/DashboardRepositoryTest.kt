package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardRepositoryTest {

    private class FakeAnnouncementRepository(
        private val announcements: MutableList<Announcement>,
    ) : AnnouncementRepository {

        override suspend fun getAnnouncements(): List<Announcement> {
            return announcements.sortedByDescending { it.publishDate }
        }

        override suspend fun createAnnouncement(
            title: String,
            message: String,
            createdBy: String,
        ): Announcement {
            val announcement = Announcement(
                id = "test-${announcements.size + 1}",
                title = title,
                message = message,
                publishDate = "2026-10-11",
                createdBy = createdBy,
            )
            announcements.add(announcement)
            return announcement
        }

        override suspend fun updateAnnouncement(
            announcement: Announcement,
        ) {
            val index = announcements.indexOfFirst {
                it.id == announcement.id
            }
            if (index >= 0) {
                announcements[index] = announcement
            }
        }

        override suspend fun deleteAnnouncement(
            announcementId: String,
        ) {
            announcements.removeAll { it.id == announcementId }
        }
    }

    private fun announcement(
        id: String,
        title: String,
        date: String,
    ) = Announcement(
        id = id,
        title = title,
        message = "Test message",
        publishDate = date,
        createdBy = "user-1",
    )

    @Test
    fun getDashboard_adminShowsCorrectAnnouncementCount() {
        val announcements = mutableListOf(
            announcement("1", "First", "2026-10-01"),
            announcement("2", "Second", "2026-10-02"),
            announcement("3", "Third", "2026-10-03"),
            announcement("4", "Fourth", "2026-10-04"),
        )

        val repository = MockDashboardRepository(
            announcementRepository = FakeAnnouncementRepository(announcements),
        )

        val admin = UserProfile(
            id = "user-1",
            fullName = "Test Administrator",
            email = "admin@iuafc.org",
            role = Role.ADMINISTRATOR,
        )

        val dashboard = kotlinx.coroutines.runBlocking {
            repository.getDashboard(admin)
        }

        val announcementSummary = dashboard.summaries.first {
            it.label == "Announcements"
        }

        assertEquals("4", announcementSummary.value)
    }

    @Test
    fun getDashboard_displaysOnlyThreeLatestAnnouncements() {
        val announcements = mutableListOf(
            announcement("1", "Oldest", "2026-10-01"),
            announcement("2", "Second", "2026-10-02"),
            announcement("3", "Third", "2026-10-03"),
            announcement("4", "Newest", "2026-10-04"),
        )

        val repository = MockDashboardRepository(
            announcementRepository = FakeAnnouncementRepository(announcements),
        )

        val admin = UserProfile(
            id = "user-1",
            fullName = "Test Administrator",
            email = "admin@iuafc.org",
            role = Role.ADMINISTRATOR,
        )

        val dashboard = kotlinx.coroutines.runBlocking {
            repository.getDashboard(admin)
        }

        assertEquals(3, dashboard.latestAnnouncements.size)
        assertEquals(
            listOf("Newest", "Third", "Second"),
            dashboard.latestAnnouncements.map { it.title },
        )
    }

    @Test
    fun getDashboard_usesUpdatedAnnouncementCount() {
        val announcements = mutableListOf(
            announcement("1", "First", "2026-10-01"),
        )

        val repository = MockDashboardRepository(
            announcementRepository = FakeAnnouncementRepository(announcements),
        )

        val admin = UserProfile(
            id = "user-1",
            fullName = "Test Administrator",
            email = "admin@iuafc.org",
            role = Role.ADMINISTRATOR,
        )

        kotlinx.coroutines.runBlocking {
            repository.getDashboard(admin)

            announcements.add(
                announcement("2", "New announcement", "2026-10-11"),
            )

            val updatedDashboard = repository.getDashboard(admin)

            val count = updatedDashboard.summaries.first {
                it.label == "Announcements"
            }.value

            assertEquals("2", count)
            assertTrue(
                updatedDashboard.latestAnnouncements.any {
                    it.title == "New announcement"
                },
            )
        }
    }
}
