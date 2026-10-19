package com.app.iuafc.data.repository

import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.AppNotification
import com.app.iuafc.data.model.NotificationType

interface NotificationRepository {

    suspend fun getNotifications(): List<AppNotification>

    suspend fun markAsRead(notificationId: String)

    suspend fun markAllAsRead()
}

/**
 * Notifications built from announcements, events and reviewed activities each time they are
 * loaded. Which ones were read is kept in memory while the app is open.
 */
class MockNotificationRepository(
    private val announcementRepository: AnnouncementRepository =
        MockAnnouncementRepository(),
    private val eventRepository: EventRepository = MockEventRepository(),
    private val featuresRepository: ChurchFeaturesRepository = MockChurchFeaturesRepository(),
) : NotificationRepository {

    private val readIds = mutableSetOf<String>()
    private var lastIds: List<String> = emptyList()

    override suspend fun getNotifications(): List<AppNotification> {
        val announcements = announcementRepository.getAnnouncements().map { announcement ->
            AppNotification(
                id = "announcement-${announcement.id}",
                type = NotificationType.Announcement,
                title = announcement.title,
                message = announcement.message,
                timestamp = "${announcement.publishDate}T00:00:00",
            )
        }

        val events = eventRepository.getEvents().map { event ->
            AppNotification(
                id = "event-${event.id}",
                type = NotificationType.Event,
                title = event.name,
                message = "${event.date} at ${event.time} • ${event.venue}",
                timestamp = "${event.date}T${event.time}:00",
            )
        }

        val activities = featuresRepository.getActivities()
            .filter { it.status == ActivityStatus.Approved || it.status == ActivityStatus.Rejected }
            .mapNotNull { activity ->
                val reviewedAt = activity.reviewedAt ?: return@mapNotNull null
                val statusText = if (activity.status == ActivityStatus.Approved) "approved" else "rejected"
                AppNotification(
                    id = "activity-${activity.id}",
                    type = NotificationType.ActivityApproval,
                    title = "Activity $statusText",
                    message = activity.title + (activity.reviewNote?.let { " — $it" } ?: ""),
                    timestamp = reviewedAt,
                )
            }

        val all = (announcements + events + activities)
            .map { it.copy(isRead = it.id in readIds) }
            .sortedByDescending { it.timestamp }
        lastIds = all.map { it.id }
        return all
    }

    override suspend fun markAsRead(notificationId: String) {
        readIds += notificationId
    }

    override suspend fun markAllAsRead() {
        readIds += lastIds
    }
}
