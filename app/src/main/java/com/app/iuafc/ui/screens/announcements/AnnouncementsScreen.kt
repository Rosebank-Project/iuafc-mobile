package com.app.iuafc.ui.screens.announcements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
                Text(
                    text = "Loading announcements...",
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
                        text = "Unable to load announcements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )

                    Button(
                        onClick = viewModel::loadAnnouncements,
                    ) {
                        Text("Retry")
                    }
                }
            }

            is UiState.Success -> {
                val announcements = state.data

                if (announcements.isEmpty()) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "No announcements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text = "There are currently no church announcements available.",
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
                        items(
                            items = announcements,
                            key = { it.id },
                        ) { announcement ->

                            AnnouncementCard(
                                announcement = announcement,
                                canManage = canManageAnnouncements,
                                onEdit = {
                                    announcementToEdit = announcement
                                },
                                onDelete = {
                                    announcementToDelete = announcement
                                },
                            )
                        }
                    }
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
                Text("Delete Announcement")
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${announcement.title}\"?"
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
                    Text("Delete")
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
private fun AnnouncementCard(
    announcement: Announcement,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

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
                            )
                        }
                    }
                }
            }

            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = formatDate(announcement.publishDate),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
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
            Text(title)
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
                Text(confirmText)
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