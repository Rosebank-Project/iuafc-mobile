
package com.app.iuafc.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
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
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.screens.activities.ActivitiesScreen
import com.app.iuafc.ui.screens.announcements.AnnouncementsScreen
import com.app.iuafc.ui.screens.attendance.AttendanceScreen
import com.app.iuafc.ui.screens.dashboard.DashboardScreen
import com.app.iuafc.ui.screens.donations.DonationsScreen
import com.app.iuafc.ui.screens.events.EventsScreen
import com.app.iuafc.ui.screens.help.HelpScreen
import com.app.iuafc.ui.screens.members.MemberDetailScreen
import com.app.iuafc.ui.screens.members.MemberFormScreen
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
fun MainScaffold(
    user: UserProfile,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val bottomItems = AppDestination.bottomBarFor(user.role)

    val showBottomBar = bottomItems.any { item ->
        currentDestination?.hierarchy?.any {
            it.hasRoute(item.route::class)
        } == true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination
                                ?.hierarchy
                                ?.any { it.hasRoute(item.route::class) } == true,
                            onClick = {
                                navController.navigateToTab(item.route)
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = {
                                Text(item.label)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor =
                                    MaterialTheme.colorScheme.primary,
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
            val back: () -> Unit = {
                navController.popBackStack()
            }

            val isAdmin = user.role == Role.ADMINISTRATOR

            // Dashboard
            composable<DashboardRoute> {
                DashboardScreen(
                    user = user,
                    onOpenNotifications = {
                        navController.navigate(NotificationsRoute)
                    },
                    onOpenEvents = {
                        navController.navigateToTab(EventsRoute)
                    },
                    onOpenAnnouncements = {
                        navController.navigateToTab(AnnouncementsRoute)
                    },
                )
            }

            // Bottom navigation tabs
            composable<EventsRoute> {
                EventsScreen()
            }

            composable<AnnouncementsRoute> {
                AnnouncementsScreen(
                    user = user,
                )
            }

            composable<MenuRoute> {
                MenuScreen(
                    user = user,
                    onNavigate = { item ->
                        navController.navigate(item.route)
                    },
                    onSignOut = onSignOut,
                )
            }

            // Screens opened from the Menu
            roleScreen(
                user = user,
                destination = AppDestination.NOTIFICATIONS,
                route = NotificationsRoute::class,
            ) {
                NotificationsScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.USERS,
                route = UsersRoute::class,
            ) {
                UsersScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.MEMBERS,
                route = MembersRoute::class,
            ) {
                MembersScreen(
                    onBack = back,
                    canManage = isAdmin,
                    onOpenMember = { memberId ->
                        navController.navigate(MemberDetailRoute(memberId))
                    },
                    onAddMember = {
                        navController.navigate(MemberFormRoute())
                    },
                )
            }

            roleScreen(
                user = user,
                destination = AppDestination.MEMBERS,
                route = MemberDetailRoute::class,
            ) {
                MemberDetailScreen(
                    onBack = back,
                    canManage = isAdmin,
                    onEdit = { memberId ->
                        navController.navigate(MemberFormRoute(memberId))
                    },
                )
            }

            roleScreen(
                user = user,
                destination = AppDestination.MEMBERS,
                route = MemberFormRoute::class,
                adminOnly = true,
            ) {
                MemberFormScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.MINISTRIES,
                route = MinistriesRoute::class,
            ) {
                MinistriesScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.ACTIVITIES,
                route = ActivitiesRoute::class,
            ) {
                ActivitiesScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.ATTENDANCE,
                route = AttendanceRoute::class,
            ) {
                AttendanceScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.MY_ATTENDANCE,
                route = MyAttendanceRoute::class,
            ) {
                MyAttendanceScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.DONATIONS,
                route = DonationsRoute::class,
            ) {
                DonationsScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.REPORTS,
                route = ReportsRoute::class,
            ) {
                ReportsScreen(back)
            }

            roleScreen(
                user = user,
                destination = AppDestination.SETTINGS,
                route = SettingsRoute::class,
            ) {
                SettingsScreen(
                    user = user,
                    onBack = back,
                    onSignOut = onSignOut,
                )
            }

            roleScreen(
                user = user,
                destination = AppDestination.HELP,
                route = HelpRoute::class,
            ) {
                HelpScreen(back)
            }
        }
    }
}

private fun <T : Any> NavGraphBuilder.roleScreen(
    user: UserProfile,
    destination: AppDestination,
    route: KClass<T>,
    adminOnly: Boolean = false,
    content: @Composable () -> Unit,
) {
    composable(route) {
        val allowed =
            destination.isAllowedFor(user.role) &&
                    (!adminOnly || user.role == Role.ADMINISTRATOR)

        if (allowed) {
            content()
        } else {
            EmptyState(
                title = "Access denied",
                message = "Your role (${user.role.displayName}) " +
                        "cannot open ${destination.label}.",
            )
        }
    }
}

private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}