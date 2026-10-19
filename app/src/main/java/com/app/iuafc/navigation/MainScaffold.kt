package com.app.iuafc.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.iuafc.R
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

private val HeaderNavy = Color(0xFF112548)

/** The signed-in part of the app: shared header, bottom navigation bar and screens. */
@Composable
fun MainScaffold(
    user: UserProfile,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val bottomItems = AppDestination.bottomBarFor(user.role)

    val showBottomBar =
        bottomItems.any { item ->
            currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
        } ||
                currentDestination?.hierarchy?.any {
                    it.hasRoute(NotificationsRoute::class) ||
                            it.hasRoute(SettingsRoute::class)
                } == true

    fun openNotifications() {
        navController.navigate(NotificationsRoute) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        // The TopAppBar handles status-bar insets; avoid fixed top padding.
        contentWindowInsets = WindowInsets(0),
        topBar = {
            SharedChurchHeader(
                user = user,
                onOpenNotifications = ::openNotifications,
            )
        },
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
                            label = { Text(item.label) },
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
            val back: () -> Unit = { navController.popBackStack() }
            val isAdmin = user.role == Role.ADMINISTRATOR

            composable<DashboardRoute> {
                DashboardScreen(
                    user = user,
                    onOpenNotifications = ::openNotifications,
                    onOpenEvents = {
                        navController.navigateToTab(EventsRoute)
                    },
                    onOpenAnnouncements = {
                        navController.navigateToTab(AnnouncementsRoute)
                    },
                )
            }

            composable<EventsRoute> {
                EventsScreen(user = user)
            }

            composable<AnnouncementsRoute> {
                AnnouncementsScreen(user = user)
            }

            composable<MenuRoute> {
                MenuScreen(
                    user = user,
                    onNavigate = { item ->
                        navController.navigate(item.route) {
                            launchSingleTop = true
                        }
                    },
                    onSignOut = onSignOut,
                )
            }

            roleScreen(user, AppDestination.NOTIFICATIONS, NotificationsRoute::class) {
                NotificationsScreen(back)
            }

            roleScreen(user, AppDestination.USERS, UsersRoute::class) {
                UsersScreen(back)
            }

            roleScreen(user, AppDestination.MEMBERS, MembersRoute::class) {
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

            roleScreen(user, AppDestination.MEMBERS, MemberDetailRoute::class) {
                MemberDetailScreen(
                    onBack = back,
                    canManage = isAdmin,
                    onEdit = { memberId ->
                        navController.navigate(MemberFormRoute(memberId))
                    },
                )
            }

            roleScreen(
                user,
                AppDestination.MEMBERS,
                MemberFormRoute::class,
                adminOnly = true,
            ) {
                MemberFormScreen(back)
            }

            roleScreen(user, AppDestination.MINISTRIES, MinistriesRoute::class) {
                MinistriesScreen(back, canManage = isAdmin)
            }

            roleScreen(user, AppDestination.ACTIVITIES, ActivitiesRoute::class) {
                ActivitiesScreen(back, user)
            }

            roleScreen(user, AppDestination.ATTENDANCE, AttendanceRoute::class) {
                AttendanceScreen(user = user, onBack = back)
            }

            roleScreen(user, AppDestination.MY_ATTENDANCE, MyAttendanceRoute::class) {
                MyAttendanceScreen(user = user, onBack = back)
            }

            roleScreen(user, AppDestination.DONATIONS, DonationsRoute::class) {
                DonationsScreen(back, user)
            }

            roleScreen(user, AppDestination.REPORTS, ReportsRoute::class) {
                ReportsScreen(back)
            }

            roleScreen(user, AppDestination.SETTINGS, SettingsRoute::class) {
                SettingsScreen(
                    user = user,
                    onBack = back,
                    onSignOut = onSignOut,
                    onOpenHelp = {
                        navController.navigate(HelpRoute)
                    },
                )
            }

            roleScreen(user, AppDestination.HELP, HelpRoute::class) {
                HelpScreen(back)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharedChurchHeader(
    user: UserProfile,
    onOpenNotifications: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
    ) {
        TopAppBar(
            modifier = Modifier.heightIn(min = 100.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                ) {
                    Card(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White,
                        ),
                    ){
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.church_logo),
                                contentDescription = "IUAFC church logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp),
                            )
                        }
                    }

                    Text(
                        text = "IUAFC",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsNone,
                        contentDescription = "Open notifications",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Surface(
                    modifier = Modifier
                        .padding(start = 4.dp, end = 16.dp)
                        .size(42.dp),
                    shape = RoundedCornerShape(50),
                    color = HeaderNavy,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initialsFor(user.fullName),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            },
        )
    }
}

private fun initialsFor(fullName: String): String {
    val parts = fullName.trim()
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }

    return when {
        parts.size >= 2 ->
            "${parts.first().first()}${parts.last().first()}".uppercase()

        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "?"
    }
}

/**
 * Registers a screen that only some roles can open; others see "Access denied".
 * [adminOnly] restricts it further to Administrators (e.g. forms that change data).
 */
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

/** Switches bottom bar tabs without stacking them on top of each other. */
private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}