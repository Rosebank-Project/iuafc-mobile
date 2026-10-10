
package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import java.time.LocalDate

data class DashboardSummary(
    val label: String,
    val value: String,
)

data class DashboardData(
    val summaries: List<DashboardSummary>,
    val upcomingEvents: List<Event>,
    val latestAnnouncements: List<Announcement>,
)

interface DashboardRepository {
    suspend fun getDashboard(user: UserProfile): DashboardData
}

class MockDashboardRepository(
    private val announcementRepository: AnnouncementRepository =
        MockAnnouncementRepository(),
) : DashboardRepository {

    override suspend fun getDashboard(
        user: UserProfile,
    ): DashboardData {

        val today = LocalDate.now().toString()

        // Keep all upcoming events for accurate summary counts.
        val allUpcomingEvents = MockData.events
            .filter { it.date >= today }
            .sortedBy { it.date }

        // Display only the first three upcoming events.
        val upcomingEvents = allUpcomingEvents.take(3)

        // Use the announcement repository shared by the app.
        val latestAnnouncements = announcementRepository
            .getAnnouncements()
            .take(3)

        val totalDonations = MockData.donations
            .sumOf { it.amount }

        val formattedDonations =
            "ZAR ${"%,.0f".format(totalDonations)}"

        val summaries = when (user.role) {

            Role.ADMINISTRATOR -> listOf(
                DashboardSummary(
                    label = "Total Members",
                    value = MockData.members.size.toString(),
                ),
                DashboardSummary(
                    label = "Upcoming Events",
                    value = allUpcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    label = "Announcements",
                    value = MockData.announcements.size.toString(),
                ),
                DashboardSummary(
                    label = "Total Donations",
                    value = formattedDonations,
                ),
            )

            Role.PASTOR -> listOf(
                DashboardSummary(
                    label = "Total Members",
                    value = MockData.members.size.toString(),
                ),
                DashboardSummary(
                    label = "Ministries",
                    value = MockData.ministries.size.toString(),
                ),
                DashboardSummary(
                    label = "Upcoming Events",
                    value = allUpcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    label = "Total Donations",
                    value = formattedDonations,
                ),
            )

            Role.FINANCE_OFFICER -> listOf(
                DashboardSummary(
                    label = "Total Donations",
                    value = formattedDonations,
                ),
                DashboardSummary(
                    label = "Total Members",
                    value = MockData.members.size.toString(),
                ),
                DashboardSummary(
                    label = "Upcoming Events",
                    value = allUpcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    label = "Announcements",
                    value = MockData.announcements.size.toString(),
                ),
            )

            Role.MINISTRY_LEADER -> {
                val activities = MockData.activities.count {
                    it.ministryId == user.ministryId
                }

                listOf(
                    DashboardSummary(
                        label = "My Activities",
                        value = activities.toString(),
                    ),
                    DashboardSummary(
                        label = "Upcoming Events",
                        value = allUpcomingEvents.size.toString(),
                    ),
                    DashboardSummary(
                        label = "Announcements",
                        value = MockData.announcements.size.toString(),
                    ),
                    DashboardSummary(
                        label = "Ministries",
                        value = MockData.ministries.size.toString(),
                    ),
                )
            }

            Role.CHURCH_MEMBER -> {
                val attendance = MockData.attendance.count {
                    user.memberId != null &&
                            it.memberId == user.memberId
                }

                val donations = MockData.donations
                    .filter {
                        user.memberId != null &&
                                it.memberId == user.memberId
                    }
                    .sumOf { it.amount }

                listOf(
                    DashboardSummary(
                        label = "Upcoming Events",
                        value = allUpcomingEvents.size.toString(),
                    ),
                    DashboardSummary(
                        label = "Announcements",
                        value = MockData.announcements.size.toString(),
                    ),
                    DashboardSummary(
                        label = "My Attendance",
                        value = attendance.toString(),
                    ),
                    DashboardSummary(
                        label = "My Giving",
                        value = "ZAR ${"%,.0f".format(donations)}",
                    ),
                )
            }
        }

        return DashboardData(
            summaries = summaries,
            upcomingEvents = upcomingEvents,
            latestAnnouncements = latestAnnouncements,
        )
    }
}