package com.app.iuafc.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
            IconButton(onClick = onOpenNotifications) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Here’s an overview of your church activities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = user.role.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            SummaryGrid(data.summaries)
        }

        item {
            Text(
                text = "Upcoming Events",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (data.upcomingEvents.isEmpty()) {

            item {
                Text(
                    text = "No upcoming events.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

        item {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "Latest Announcements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (data.latestAnnouncements.isEmpty()) {

            item {
                Text(
                    text = "No announcements available.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Spacer(Modifier.weight(1f))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Icon(
                imageVector = summaryIcon(summary.label),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = summary.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = summary.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun summaryIcon(label: String) =
    when {
        label.contains("Member", ignoreCase = true) ->
            Icons.Outlined.People

        label.contains("Event", ignoreCase = true) ->
            Icons.Outlined.CalendarMonth

        label.contains("Announcement", ignoreCase = true) ->
            Icons.Outlined.Campaign

        else ->
            Icons.Outlined.Campaign
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
        shape = RoundedCornerShape(14.dp),
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "${formatDate(date)} • $time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )

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
        shape = RoundedCornerShape(14.dp),
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

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
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun formatDate(value: String): String =
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