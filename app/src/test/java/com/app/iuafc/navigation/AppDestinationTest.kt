package com.app.iuafc.navigation

import com.app.iuafc.data.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Checks that the role rules match the website sidebar. */
class AppDestinationTest {

    @Test
    fun everyRole_hasTheSameBottomBar() {
        val expected = listOf(
            AppDestination.DASHBOARD,
            AppDestination.EVENTS,
            AppDestination.ANNOUNCEMENTS,
            AppDestination.MENU,
        )
        Role.entries.forEach { role -> assertEquals(expected, AppDestination.bottomBarFor(role)) }
    }

    @Test
    fun onlyAdministrator_canManageUsers() {
        assertTrue(AppDestination.USERS.isAllowedFor(Role.ADMINISTRATOR))
        Role.entries.filter { it != Role.ADMINISTRATOR }.forEach { role ->
            assertFalse(AppDestination.USERS.isAllowedFor(role))
        }
    }

    @Test
    fun churchMember_seesOwnAttendanceAndDonationsOnly() {
        assertEquals(
            listOf(AppDestination.NOTIFICATIONS, AppDestination.MY_ATTENDANCE, AppDestination.DONATIONS),
            AppDestination.menuItemsFor(Role.CHURCH_MEMBER),
        )
    }

    @Test
    fun financeOfficer_cannotSeeMembersOrReports() {
        val menu = AppDestination.menuItemsFor(Role.FINANCE_OFFICER)
        assertTrue(AppDestination.DONATIONS in menu)
        assertFalse(AppDestination.MEMBERS in menu)
        assertFalse(AppDestination.REPORTS in menu)
    }

    @Test
    fun settingsAndHelp_areNotInFeatureList_butAllowedForEveryone() {
        Role.entries.forEach { role ->
            val menu = AppDestination.menuItemsFor(role)
            assertFalse(AppDestination.SETTINGS in menu)
            assertFalse(AppDestination.HELP in menu)
            assertTrue(AppDestination.SETTINGS.isAllowedFor(role))
            assertTrue(AppDestination.HELP.isAllowedFor(role))
        }
    }
}
