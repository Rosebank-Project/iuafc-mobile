package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Announcement
import java.util.UUID

interface AnnouncementRepository {

    suspend fun getAnnouncements(): List<Announcement>

    suspend fun createAnnouncement(
        title: String,
        message: String,
        createdBy: String,
    ): Announcement

    suspend fun updateAnnouncement(
        announcement: Announcement,
    )

    suspend fun deleteAnnouncement(
        announcementId: String,
    )
}

class MockAnnouncementRepository : AnnouncementRepository {

    /*
     * Mutable copy allows the app to demonstrate CRUD functionality
     * while the Supabase announcements table is still being completed.
     */
    private val announcements = MockData.announcements.toMutableList()

    override suspend fun getAnnouncements(): List<Announcement> {
        return announcements
            .sortedByDescending { it.publishDate }
    }

    override suspend fun createAnnouncement(
        title: String,
        message: String,
        createdBy: String,
    ): Announcement {

        val announcement = Announcement(
            id = "ann-${UUID.randomUUID()}",
            title = title,
            message = message,
            publishDate = java.time.LocalDate.now().toString(),
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

        if (index != -1) {
            announcements[index] = announcement
        }
    }

    override suspend fun deleteAnnouncement(
        announcementId: String,
    ) {
        announcements.removeAll {
            it.id == announcementId
        }
    }
}