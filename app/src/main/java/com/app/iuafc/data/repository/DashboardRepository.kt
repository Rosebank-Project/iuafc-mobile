
package com.app.iuafc.data.repository

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

/** Builds the dashboard of each role from the other repositories. */
class MockDashboardRepository(
    private val announcementRepository: AnnouncementRepository =
        MockAnnouncementRepository(),
    private val memberRepository: MemberRepository = MockMemberRepository(delayMillis = 0),
    private val featuresRepository: ChurchFeaturesRepository = MockChurchFeaturesRepository(delayMillis = 0),
    private val eventRepository: EventRepository = MockEventRepository(),
    private val attendanceRepository: AttendanceRepository = MockAttendanceRepository(),
) : DashboardRepository {

    override suspend fun getDashboard(
        user: UserProfile,
    ): DashboardData {

        val today = LocalDate.now().toString()
        val members = memberRepository.getMembers()
        val ministries = featuresRepository.getMinistries()
        val donations = featuresRepository.getDonations()
        val events = eventRepository.getEvents()

        // Count every upcoming event.
        val allUpcomingEvents = events
            .filter { it.date >= today }
            .sortedBy { it.date }

        // Display only the first three events.
        val upcomingEvents = allUpcomingEvents.take(3)

        // Read the current announcements from the shared repository.
        val allAnnouncements =
            announcementRepository.getAnnouncements()

        val latestAnnouncements = allAnnouncements.take(3)

        val totalDonations = donations
            .sumOf { it.amount }

        val formattedDonations =
            "ZAR ${"%,.0f".format(totalDonations)}"

        val summaries = when (user.role) {

            Role.ADMINISTRATOR -> listOf(
                DashboardSummary(
                    label = "Total Members",
                    value = members.size.toString(),
                ),
                DashboardSummary(
                    label = "Upcoming Events",
                    value = allUpcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    label = "Announcements",
                    value = allAnnouncements.size.toString(),
                ),
                DashboardSummary(
                    label = "Total Donations",
                    value = formattedDonations,
                ),
            )

            Role.PASTOR -> listOf(
                DashboardSummary(
                    label = "Total Members",
                    value = members.size.toString(),
                ),
                DashboardSummary(
                    label = "Ministries",
                    value = ministries.size.toString(),
                ),
                DashboardSummary(
                    label = "Upcoming Events",
                    value = allUpcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    label = "Announcements",
                    value = allAnnouncements.size.toString(),
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
                    value = members.size.toString(),
                ),
                DashboardSummary(
                    label = "Upcoming Events",
                    value = allUpcomingEvents.size.toString(),
                ),
                DashboardSummary(
                    label = "Announcements",
                    value = allAnnouncements.size.toString(),
                ),
            )

            Role.MINISTRY_LEADER -> {
                val activities = featuresRepository.getActivities().count {
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
                        value = allAnnouncements.size.toString(),
                    ),
                    DashboardSummary(
                        label = "Ministries",
                        value = ministries.size.toString(),
                    ),
                )
            }

            Role.CHURCH_MEMBER -> {
                val attendance = attendanceRepository.getAttendance().count {
                    user.memberId != null &&
                            it.memberId == user.memberId
                }

                val donations = donations
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
                        value = allAnnouncements.size.toString(),
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