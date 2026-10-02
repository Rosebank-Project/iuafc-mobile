package com.app.iuafc.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.Role.ADMINISTRATOR
import com.app.iuafc.data.model.Role.CHURCH_MEMBER
import com.app.iuafc.data.model.Role.FINANCE_OFFICER
import com.app.iuafc.data.model.Role.MINISTRY_LEADER
import com.app.iuafc.data.model.Role.PASTOR
import kotlinx.serialization.Serializable

// Type-safe routes for Navigation Compose. Add a new one here when you add a screen.
@Serializable data object DashboardRoute
@Serializable data object NotificationsRoute
@Serializable data object UsersRoute
@Serializable data object MembersRoute
@Serializable data class MemberDetailRoute(val memberId: String)
/** [memberId] null = add a new member. */
@Serializable data class MemberFormRoute(val memberId: String? = null)
@Serializable data object MinistriesRoute
@Serializable data object ActivitiesRoute
@Serializable data object EventsRoute
@Serializable data object AttendanceRoute
@Serializable data object MyAttendanceRoute
@Serializable data object DonationsRoute
@Serializable data object AnnouncementsRoute
@Serializable data object ReportsRoute
@Serializable data object MenuRoute
@Serializable data object SettingsRoute
@Serializable data object HelpRoute

private val ALL_ROLES = Role.entries.toSet()

/**
 * Every screen of the app and which roles can open it.
 * The role rules are the same as the website sidebar (`components/app-sidebar.tsx`).
 */
enum class AppDestination(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val roles: Set<Role>,
    val inBottomBar: Boolean = false,
) {
    DASHBOARD("Home", Icons.Outlined.Home, DashboardRoute, ALL_ROLES, inBottomBar = true),
    EVENTS("Events", Icons.Outlined.CalendarMonth, EventsRoute, ALL_ROLES, inBottomBar = true),
    ANNOUNCEMENTS("Announcements", Icons.Outlined.Campaign, AnnouncementsRoute, ALL_ROLES, inBottomBar = true),
    MENU("Menu", Icons.Outlined.Menu, MenuRoute, ALL_ROLES, inBottomBar = true),

    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications, NotificationsRoute, ALL_ROLES),
    USERS("Users", Icons.Outlined.ManageAccounts, UsersRoute, setOf(ADMINISTRATOR)),
    MEMBERS("Members", Icons.Outlined.Groups, MembersRoute, setOf(ADMINISTRATOR, PASTOR)),
    MINISTRIES("Ministries", Icons.Outlined.VolunteerActivism, MinistriesRoute, setOf(ADMINISTRATOR, PASTOR, MINISTRY_LEADER)),
    ACTIVITIES("Ministry Activities", Icons.Outlined.Checklist, ActivitiesRoute, setOf(ADMINISTRATOR, PASTOR, MINISTRY_LEADER)),
    ATTENDANCE("Attendance", Icons.Outlined.FactCheck, AttendanceRoute, setOf(ADMINISTRATOR, PASTOR, MINISTRY_LEADER)),
    MY_ATTENDANCE("My Attendance", Icons.Outlined.EventAvailable, MyAttendanceRoute, setOf(CHURCH_MEMBER)),
    DONATIONS("Donations", Icons.Outlined.Payments, DonationsRoute, setOf(ADMINISTRATOR, PASTOR, FINANCE_OFFICER, CHURCH_MEMBER)),
    REPORTS("Reports", Icons.Outlined.Assessment, ReportsRoute, setOf(ADMINISTRATOR, PASTOR)),

    SETTINGS("Settings", Icons.Outlined.Settings, SettingsRoute, ALL_ROLES),
    HELP("Help", Icons.Outlined.HelpOutline, HelpRoute, ALL_ROLES);

    fun isAllowedFor(role: Role): Boolean = role in roles

    companion object {
        fun bottomBarFor(role: Role): List<AppDestination> =
            entries.filter { it.inBottomBar && it.isAllowedFor(role) }

        /** Feature screens listed on the Menu screen (Settings and Help are shown separately). */
        fun menuItemsFor(role: Role): List<AppDestination> =
            entries.filter { !it.inBottomBar && it != SETTINGS && it != HELP && it.isAllowedFor(role) }
    }
}
