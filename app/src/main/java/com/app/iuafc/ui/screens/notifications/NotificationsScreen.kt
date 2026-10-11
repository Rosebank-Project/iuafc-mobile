
package com.app.iuafc.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.AppNotification
import com.app.iuafc.data.model.NotificationType
import com.app.iuafc.ui.common.UiState
import com.app.iuafc.ui.components.ScreenScaffold
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.Factory,
    )

    val uiState by viewModel.uiState.collectAsState()

    ScreenScaffold(
        title = "Notifications",
        modifier = modifier,
        onBack = onBack,
    ) { padding ->

        when (val state = uiState) {
            UiState.Loading -> {
                Text(
                    text = "Loading notifications...",
                    modifier = Modifier
                        .padding(padding)
                        .padding(24.dp),
                )
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Unable to load notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )

                    TextButton(
                        onClick = viewModel::loadNotifications,
                    ) {
                        Text("Retry")
                    }
                }
            }

            is UiState.Success -> {
                val notifications = state.data
                val unreadCount = notifications.count { !it.isRead }

                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "No notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text = "You don't have any notifications at the moment.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = if (unreadCount == 0) {
                                        "All notifications read"
                                    } else {
                                        "$unreadCount unread"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )

                                if (unreadCount > 0) {
                                    TextButton(
                                        onClick = viewModel::markAllAsRead,
                                    ) {
                                        Text("Mark all as read")
                                    }
                                }
                            }
                        }

                        items(
                            items = notifications,
                            key = { it.id },
                        ) { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = notificationIcon(notification.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) {
                        FontWeight.Medium
                    } else {
                        FontWeight.Bold
                    },
                )

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (!notification.isRead) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Unread",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun notificationIcon(
    type: NotificationType,
) = when (type) {
    NotificationType.Announcement -> Icons.Outlined.Campaign
    NotificationType.Event -> Icons.Outlined.CalendarMonth
    NotificationType.ActivityApproval -> Icons.Outlined.CheckCircle
}

private fun formatTimestamp(value: String): String =
    try {
        LocalDateTime.parse(value).format(
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy, HH:mm",
                Locale.getDefault(),
            ),
        )
    } catch (_: Exception) {
        value
    }