package com.app.iuafc.ui.screens.announcements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.common.UiState
import com.app.iuafc.ui.components.ScreenScaffold
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Announcements screen – owner: Alexandra, issue #9. */
@Composable
fun AnnouncementsScreen(
    user: UserProfile,
    modifier: Modifier = Modifier,
) {
    val viewModel: AnnouncementViewModel = viewModel(
        factory = AnnouncementViewModel.factory(),
    )

    val uiState by viewModel.uiState.collectAsState()

    val canManageAnnouncements =
        user.role == Role.ADMINISTRATOR ||
                user.role == Role.PASTOR

    var showCreateDialog by remember {
        mutableStateOf(false)
    }

    var announcementToEdit by remember {
        mutableStateOf<Announcement?>(null)
    }

    var announcementToDelete by remember {
        mutableStateOf<Announcement?>(null)
    }

    ScreenScaffold(
        title = "Announcements",
        modifier = modifier,
        actions = {
            if (canManageAnnouncements) {
                IconButton(
                    onClick = {
                        showCreateDialog = true
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Create announcement",
                    )
                }
            }
        },
    ) { padding ->

        when (val state = uiState) {

            UiState.Loading -> {
                AnnouncementLoadingState(
                    padding = padding,
                )
            }

            is UiState.Error -> {
                AnnouncementErrorState(
                    padding = padding,
                    message = state.message,
                    onRetry = viewModel::loadAnnouncements,
                )
            }

            is UiState.Success -> {
                val announcements = state.data

                if (announcements.isEmpty()) {
                    AnnouncementEmptyState(
                        padding = padding,
                    )
                } else {
                    AnnouncementList(
                        padding = padding,
                        announcements = announcements,
                        canManage = canManageAnnouncements,
                        onEdit = { announcement ->
                            announcementToEdit = announcement
                        },
                        onDelete = { announcement ->
                            announcementToDelete = announcement
                        },
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AnnouncementDialog(
            title = "Create Announcement",
            confirmText = "Create",
            onDismiss = {
                showCreateDialog = false
            },
            onConfirm = { title, message ->
                viewModel.createAnnouncement(
                    title = title,
                    message = message,
                    createdBy = user.id,
                )

                showCreateDialog = false
            },
        )
    }

    announcementToEdit?.let { announcement ->

        AnnouncementDialog(
            title = "Edit Announcement",
            confirmText = "Save",
            initialTitle = announcement.title,
            initialMessage = announcement.message,
            onDismiss = {
                announcementToEdit = null
            },
            onConfirm = { title, message ->

                viewModel.updateAnnouncement(
                    announcement.copy(
                        title = title,
                        message = message,
                    ),
                )

                announcementToEdit = null
            },
        )
    }

    announcementToDelete?.let { announcement ->

        AlertDialog(
            onDismissRequest = {
                announcementToDelete = null
            },
            title = {
                Text(
                    text = "Delete Announcement",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${announcement.title}\"?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAnnouncement(
                            announcement.id,
                        )

                        announcementToDelete = null
                    },
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        announcementToDelete = null
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun AnnouncementList(
    padding: PaddingValues,
    announcements: List<Announcement>,
    canManage: Boolean,
    onEdit: (Announcement) -> Unit,
    onDelete: (Announcement) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = padding.calculateTopPadding() + 16.dp,
            end = 16.dp,
            bottom = padding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Stay informed",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(
                    modifier = Modifier.height(4.dp),
                )

                Text(
                    text = "Keep up with the latest news, events, and updates from the church.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(
            items = announcements,
            key = { it.id },
        ) { announcement ->

            AnnouncementCard(
                announcement = announcement,
                canManage = canManage,
                onEdit = {
                    onEdit(announcement)
                },
                onDelete = {
                    onDelete(announcement)
                },
            )
        }
    }
}

@Composable
private fun AnnouncementCard(
    announcement: Announcement,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.size(12.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp),
                    )

                    Text(
                        text = formatDate(announcement.publishDate),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (canManage) {
                    Row {
                        IconButton(
                            onClick = onEdit,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit announcement",
                            )
                        }

                        IconButton(
                            onClick = onDelete,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete announcement",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnnouncementLoadingState(
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = padding.calculateTopPadding() + 24.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = padding.calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Loading announcements...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "Please wait while the latest church announcements are loaded.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnnouncementErrorState(
    padding: PaddingValues,
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = padding.calculateTopPadding() + 24.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = padding.calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        Text(
            text = "Unable to load announcements",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )

        Button(
            onClick = onRetry,
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun AnnouncementEmptyState(
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = padding.calculateTopPadding() + 32.dp,
                start = 24.dp,
                end = 24.dp,
                bottom = padding.calculateBottomPadding(),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text(
            text = "No announcements",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "There are currently no church announcements available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnnouncementDialog(
    title: String,
    confirmText: String,
    initialTitle: String = "",
    initialMessage: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var announcementTitle by remember {
        mutableStateOf(initialTitle)
    }

    var announcementMessage by remember {
        mutableStateOf(initialMessage)
    }

    val isValid =
        announcementTitle.isNotBlank() &&
                announcementMessage.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = announcementTitle,
                    onValueChange = {
                        announcementTitle = it
                    },
                    label = {
                        Text("Title")
                    },
                    placeholder = {
                        Text("Enter announcement title")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = announcementMessage,
                    onValueChange = {
                        announcementMessage = it
                    },
                    label = {
                        Text("Message")
                    },
                    placeholder = {
                        Text("Enter announcement message")
                    },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onConfirm(
                        announcementTitle.trim(),
                        announcementMessage.trim(),
                    )
                },
            ) {
                Text(
                    text = confirmText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("Cancel")
            }
        },
    )
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