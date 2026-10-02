package com.app.iuafc.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.screens.activities.ActivitiesScreen
import com.app.iuafc.ui.screens.announcements.AnnouncementsScreen
import com.app.iuafc.ui.screens.attendance.AttendanceScreen
import com.app.iuafc.ui.screens.dashboard.DashboardScreen
import com.app.iuafc.ui.screens.donations.DonationsScreen
import com.app.iuafc.ui.screens.events.EventsScreen
import com.app.iuafc.ui.screens.help.HelpScreen
import com.app.iuafc.ui.screens.members.MembersScreen
import com.app.iuafc.ui.screens.menu.MenuScreen
import com.app.iuafc.ui.screens.ministries.MinistriesScreen
import com.app.iuafc.ui.screens.myattendance.MyAttendanceScreen
import com.app.iuafc.ui.screens.notifications.NotificationsScreen
import com.app.iuafc.ui.screens.reports.ReportsScreen
import com.app.iuafc.ui.screens.settings.SettingsScreen
import com.app.iuafc.ui.screens.users.UsersScreen
import kotlin.reflect.KClass

/** The signed-in part of the app: bottom navigation bar + every screen. */
@Composable
fun MainScaffold(user: UserProfile, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val bottomItems = AppDestination.bottomBarFor(user.role)
    val showBottomBar = bottomItems.any { item -> currentDestination?.hasRoute(item.route::class) == true }

    Scaffold(
        // Each screen draws its own top bar, so only the bottom bar is handled here.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                            onClick = { navController.navigateToTab(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        ) {
            val back: () -> Unit = { navController.popBackStack() }

            // Bottom bar tabs
            composable<DashboardRoute> {
                DashboardScreen(onOpenNotifications = { navController.navigate(NotificationsRoute) })
            }
            composable<EventsRoute> { EventsScreen() }
            composable<AnnouncementsRoute> { AnnouncementsScreen() }
            composable<MenuRoute> {
                MenuScreen(
                    user = user,
                    onNavigate = { navController.navigate(it.route) },
                    onSignOut = onSignOut,
                )
            }

            // Screens opened from the Menu. Each one checks the user's role.
            roleScreen(user, AppDestination.NOTIFICATIONS, NotificationsRoute::class) { NotificationsScreen(back) }
            roleScreen(user, AppDestination.USERS, UsersRoute::class) { UsersScreen(back) }
            roleScreen(user, AppDestination.MEMBERS, MembersRoute::class) { MembersScreen(back) }
            roleScreen(user, AppDestination.MINISTRIES, MinistriesRoute::class) { MinistriesScreen(back) }
            roleScreen(user, AppDestination.ACTIVITIES, ActivitiesRoute::class) { ActivitiesScreen(back) }
            roleScreen(user, AppDestination.ATTENDANCE, AttendanceRoute::class) { AttendanceScreen(back) }
            roleScreen(user, AppDestination.MY_ATTENDANCE, MyAttendanceRoute::class) { MyAttendanceScreen(back) }
            roleScreen(user, AppDestination.DONATIONS, DonationsRoute::class) { DonationsScreen(back) }
            roleScreen(user, AppDestination.REPORTS, ReportsRoute::class) { ReportsScreen(back) }
            roleScreen(user, AppDestination.SETTINGS, SettingsRoute::class) { SettingsScreen(back) }
            roleScreen(user, AppDestination.HELP, HelpRoute::class) { HelpScreen(back) }
        }
    }
}

/** Registers a screen that only some roles can open; others see "Access denied". */
private fun <T : Any> NavGraphBuilder.roleScreen(
    user: UserProfile,
    destination: AppDestination,
    route: KClass<T>,
    content: @Composable () -> Unit,
) {
    composable(route) {
        if (destination.isAllowedFor(user.role)) {
            content()
        } else {
            EmptyState(
                title = "Access denied",
                message = "Your role (${user.role.displayName}) cannot open ${destination.label}.",
            )
        }
    }
}

/** Switches bottom bar tabs without stacking them on top of each other. */
private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
