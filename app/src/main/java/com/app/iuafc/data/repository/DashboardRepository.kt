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

class MockDashboardRepository : DashboardRepository {

    override suspend fun getDashboard(user: UserProfile): DashboardData {

        val today = LocalDate.now().toString()

        val upcomingEvents = MockData.events
            .filter { it.date >= today }
            .sortedBy { it.date }
            .take(3)

        val latestAnnouncements = MockData.announcements
            .sortedByDescending { it.publishDate }
            .take(3)

        val summaries = when (user.role) {

            Role.ADMINISTRATOR -> listOf(
                DashboardSummary(
                    "Members",
                    MockData.members.size.toString(),
                ),
                DashboardSummary(
                    "Upcoming Events",
                    upcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    "Announcements",
                    MockData.announcements.size.toString(),
                ),
                DashboardSummary(
                    "Donations",
                    "R ${"%,.2f".format(
                        MockData.donations.sumOf { it.amount }
                    )}",
                ),
            )

            Role.PASTOR -> listOf(
                DashboardSummary(
                    "Members",
                    MockData.members.size.toString(),
                ),
                DashboardSummary(
                    "Upcoming Events",
                    upcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    "Announcements",
                    MockData.announcements.size.toString(),
                ),
                DashboardSummary(
                    "Ministries",
                    MockData.ministries.size.toString(),
                ),
            )

            Role.FINANCE_OFFICER -> listOf(
                DashboardSummary(
                    "Donations",
                    "R ${"%,.2f".format(
                        MockData.donations.sumOf { it.amount }
                    )}",
                ),
                DashboardSummary(
                    "Members",
                    MockData.members.size.toString(),
                ),
                DashboardSummary(
                    "Upcoming Events",
                    upcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    "Announcements",
                    MockData.announcements.size.toString(),
                ),
            )

            Role.MINISTRY_LEADER -> {

                val activities = MockData.activities.count {
                    it.ministryId == user.ministryId
                }

                listOf(
                    DashboardSummary(
                        "My Activities",
                        activities.toString(),
                    ),
                    DashboardSummary(
                        "Upcoming Events",
                        upcomingEvents.size.toString(),
                    ),
                    DashboardSummary(
                        "Announcements",
                        MockData.announcements.size.toString(),
                    ),
                    DashboardSummary(
                        "Ministries",
                        MockData.ministries.size.toString(),
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
                        "Upcoming Events",
                        upcomingEvents.size.toString(),
                    ),
                    DashboardSummary(
                        "Announcements",
                        MockData.announcements.size.toString(),
                    ),
                    DashboardSummary(
                        "My Attendance",
                        attendance.toString(),
                    ),
                    DashboardSummary(
                        "My Giving",
                        "R ${"%,.2f".format(donations)}",
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