package com.app.iuafc.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.DashboardData
import com.app.iuafc.data.repository.DashboardSummary
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.components.UiStateContent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Home screen – owner: Alexandra, issue #9. */
@Composable
fun DashboardScreen(
    user: UserProfile,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(user),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "Home",
        modifier = modifier,
        actions = {
            IconButton(
                onClick = onOpenNotifications,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
    ) { padding ->

        UiStateContent(
            state = state,
            modifier = Modifier.padding(padding),
            onRetry = viewModel::load,
        ) { data ->
            DashboardContent(
                user = user,
                data = data,
            )
        }
    }
}

@Composable
private fun DashboardContent(
    user: UserProfile,
    data: DashboardData,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {

        /*
         * Header
         */
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Here's an overview of your church activities.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = user.role.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        /*
         * Summary cards
         */
        item {
            SummaryGrid(
                summaries = data.summaries,
            )
        }

        /*
         * Upcoming events
         */
        item {
            SectionHeader(
                title = "Upcoming Events",
                icon = Icons.Outlined.CalendarMonth,
            )
        }

        if (data.upcomingEvents.isEmpty()) {
            item {
                EmptySection(
                    message = "No upcoming events.",
                )
            }
        } else {
            items(
                items = data.upcomingEvents,
                key = { it.id },
            ) { event ->
                EventSummaryCard(
                    title = event.name,
                    date = event.date,
                    time = event.time,
                    venue = event.venue,
                )
            }
        }

        /*
         * Latest announcements
         */
        item {
            SectionHeader(
                title = "Latest Announcements",
                icon = Icons.Outlined.Campaign,
            )
        }

        if (data.latestAnnouncements.isEmpty()) {
            item {
                EmptySection(
                    message = "No announcements available.",
                )
            }
        } else {
            items(
                items = data.latestAnnouncements,
                key = { it.id },
            ) { announcement ->
                AnnouncementSummaryCard(
                    title = announcement.title,
                    message = announcement.message,
                    date = announcement.publishDate,
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(
    summaries: List<DashboardSummary>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        summaries.chunked(2).forEach { rowItems ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { summary ->

                    SummaryCard(
                        summary = summary,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    summary: DashboardSummary,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = summaryIcon(summary.label),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(21.dp),
                )
            }

            Text(
                text = summary.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = summary.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(22.dp),
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun EventSummaryCard(
    title: String,
    date: String,
    time: String,
    venue: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(
                    modifier = Modifier.width(8.dp),
                )

                Text(
                    text = "${formatDate(date)} • $time",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = venue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnnouncementSummaryCard(
    title: String,
    message: String,
    date: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp),
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = formatDate(date),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EmptySection(
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun summaryIcon(
    label: String,
) =
    when {
        label.contains("Member", ignoreCase = true) ->
            Icons.Outlined.People

        label.contains("Event", ignoreCase = true) ->
            Icons.Outlined.CalendarMonth

        label.contains("Announcement", ignoreCase = true) ->
            Icons.Outlined.Campaign

        label.contains("Donation", ignoreCase = true) ||
                label.contains("Giving", ignoreCase = true) ->
            Icons.Outlined.VolunteerActivism

        else ->
            Icons.Outlined.Campaign
    }

private fun formatDate(
    value: String,
): String =
    try {
        LocalDate.parse(value).format(
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy",
                Locale.getDefault(),
            ),
        )
    } catch (_: Exception) {
        value
    }