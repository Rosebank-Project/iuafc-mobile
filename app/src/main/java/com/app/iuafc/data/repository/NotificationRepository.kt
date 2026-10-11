package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.AppNotification
import com.app.iuafc.data.model.NotificationType

interface NotificationRepository {

    suspend fun getNotifications(): List<AppNotification>

    suspend fun markAsRead(notificationId: String)

    suspend fun markAllAsRead()
}

class MockNotificationRepository(
    private val announcementRepository: AnnouncementRepository =
        MockAnnouncementRepository(),
) : NotificationRepository {

    private val notifications: MutableList<AppNotification> =
        createStaticNotifications().toMutableList()

    override suspend fun getNotifications(): List<AppNotification> {
        val announcements = announcementRepository.getAnnouncements()

        // Preserve read/unread status when refreshing notifications.
        val existingReadStatus = notifications.associate {
            it.id to it.isRead
        }

        // Create a notification for every current announcement.
        val announcementNotifications = announcements.map { announcement ->
            val notificationId = "announcement-${announcement.id}"

            AppNotification(
                id = notificationId,
                type = NotificationType.Announcement,
                title = announcement.title,
                message = announcement.message,
                timestamp = "${announcement.publishDate}T00:00:00",
                isRead = existingReadStatus[notificationId] ?: false,
            )
        }

        // Replace announcement notifications with the latest list.
        notifications.removeAll {
            it.type == NotificationType.Announcement
        }

        notifications.addAll(announcementNotifications)

        return notifications.sortedByDescending { it.timestamp }
    }

    override suspend fun markAsRead(notificationId: String) {
        val index = notifications.indexOfFirst {
            it.id == notificationId
        }

        if (index != -1) {
            notifications[index] =
                notifications[index].copy(isRead = true)
        }
    }

    override suspend fun markAllAsRead() {
        for (index in notifications.indices) {
            notifications[index] =
                notifications[index].copy(isRead = true)
        }
    }

    private fun createStaticNotifications(): List<AppNotification> {
        val eventNotifications = MockData.events.map { event ->
            AppNotification(
                id = "event-${event.id}",
                type = NotificationType.Event,
                title = event.name,
                message = "${event.date} at ${event.time} • ${event.venue}",
                timestamp = "${event.date}T${event.time}:00",
            )
        }

        val activityNotifications = MockData.activities
            .filter { activity ->
                activity.status == ActivityStatus.Approved ||
                        activity.status == ActivityStatus.Rejected
            }
            .mapNotNull { activity ->
                val reviewedAt = activity.reviewedAt
                    ?: return@mapNotNull null

                val statusText =
                    if (activity.status == ActivityStatus.Approved) {
                        "approved"
                    } else {
                        "rejected"
                    }

                AppNotification(
                    id = "activity-${activity.id}",
                    type = NotificationType.ActivityApproval,
                    title = "Activity $statusText",
                    message = activity.title +
                            (activity.reviewNote?.let { " — $it" } ?: ""),
                    timestamp = reviewedAt,
                )
            }

        return eventNotifications + activityNotifications
    }
}
