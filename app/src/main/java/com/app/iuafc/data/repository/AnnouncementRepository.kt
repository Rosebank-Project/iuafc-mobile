package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Announcement
import java.util.UUID

/**
 * Church announcements. The Supabase version is [SupabaseAnnouncementRepository];
 * [MockAnnouncementRepository] is used in demo mode and in the unit tests.
 *
 * Only Administrators and Pastors may create, edit or delete announcements. The screen hides the
 * buttons for other roles and Row Level Security enforces it in the database.
 */
interface AnnouncementRepository {

    /** All announcements, newest first. */
    suspend fun getAnnouncements(): List<Announcement>

    /** Publishes a new announcement dated today and returns it with its new id. */
    suspend fun createAnnouncement(
        title: String,
        message: String,
        createdBy: String,
    ): Announcement

    /** Saves a new title and message for an existing announcement. */
    suspend fun updateAnnouncement(
        announcement: Announcement,
    )

    /** Removes an announcement. */
    suspend fun deleteAnnouncement(
        announcementId: String,
    )
}

/** Announcements kept in memory, starting from [MockData.announcements]. Changes are lost when the app closes. */
class MockAnnouncementRepository : AnnouncementRepository {

    // Mutable copy so create, edit and delete work in demo mode.
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