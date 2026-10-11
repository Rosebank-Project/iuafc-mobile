
package com.app.iuafc.ui.screens.announcements

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.common.UiState
import com.app.iuafc.ui.components.ScreenScaffold
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DeleteRed = Color(0xFFD92D35)

@Composable
fun AnnouncementsScreen(
    user: UserProfile,
    modifier: Modifier = Modifier,
) {
    val announcementViewModel: AnnouncementViewModel =
        viewModel(factory = AnnouncementViewModel.Factory)

    val uiState by announcementViewModel.uiState.collectAsState()
    val authors by announcementViewModel.authors.collectAsState()
    val colors = MaterialTheme.colorScheme

    val canManageAnnouncements =
        user.role == Role.ADMINISTRATOR || user.role == Role.PASTOR

    var showEditor by remember { mutableStateOf(false) }

    var editingAnnouncement by remember {
        mutableStateOf<Announcement?>(null)
    }

    var deletingAnnouncement by remember {
        mutableStateOf<Announcement?>(null)
    }

    ScreenScaffold(
        title = "Announcements",
        modifier = modifier,
        showTopBar = false,
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text(
                    text = "Announcements",
                    fontSize = 28.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Stay updated with church news and announcements.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = colors.onSurfaceVariant,
                )

                if (canManageAnnouncements) {
                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            editingAnnouncement = null
                            showEditor = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        Text(
                            text = "New Announcement",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            when (val currentState = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = currentState.message,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = colors.error,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = announcementViewModel::loadAnnouncements,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                            )

                            Spacer(modifier = Modifier.size(8.dp))

                            Text(
                                text = "Try Again",
                                fontSize = 16.sp,
                            )
                        }
                    }
                }

                is UiState.Success<*> -> {
                    val announcements =
                        (currentState.data as? List<*>)
                            ?.filterIsInstance<Announcement>()
                            .orEmpty()

                    if (announcements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No announcements yet.",
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 16.dp,
                                end = 16.dp,
                                bottom = padding.calculateBottomPadding() + 24.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(
                                items = announcements,
                                key = { it.id },
                            ) { announcement ->
                                val authorName = when {
                                    announcement.createdBy == user.id ->
                                        user.fullName.ifBlank { "Church office" }

                                    else ->
                                        authors[announcement.createdBy]
                                            ?.takeIf { it.isNotBlank() }
                                            ?: "Church office"
                                }

                                AnnouncementCard(
                                    announcement = announcement,
                                    authorName = authorName,
                                    canManage = canManageAnnouncements,
                                    onEdit = {
                                        editingAnnouncement = announcement
                                        showEditor = true
                                    },
                                    onDelete = {
                                        deletingAnnouncement = announcement
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AnnouncementDialog(
            announcement = editingAnnouncement,
            onDismiss = {
                showEditor = false
                editingAnnouncement = null
            },
            onSave = { title, message ->
                val existing = editingAnnouncement

                if (existing == null) {
                    announcementViewModel.createAnnouncement(
                        title = title,
                        message = message,
                        createdBy = user.id,
                    )
                } else {
                    announcementViewModel.updateAnnouncement(
                        existing.copy(
                            title = title,
                            message = message,
                        ),
                    )
                }

                showEditor = false
                editingAnnouncement = null
            },
        )
    }

    deletingAnnouncement?.let { announcement ->
        AlertDialog(
            onDismissRequest = {
                deletingAnnouncement = null
            },
            title = {
                Text(
                    text = "Delete announcement?",
                    fontSize = 22.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${announcement.title}\"? This action cannot be undone.",
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        announcementViewModel.deleteAnnouncement(
                            announcement.id,
                        )
                        deletingAnnouncement = null
                    },
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        color = DeleteRed,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingAnnouncement = null
                    },
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                    )
                }
            },
        )
    }
}

@Composable
private fun AnnouncementCard(
    announcement: Announcement,
    authorName: String,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = colors.outlineVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = announcement.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )

                if (canManage) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit announcement",
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete announcement",
                            tint = DeleteRed,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = announcement.message,
                fontSize = 16.sp,
                lineHeight = 25.sp,
                color = colors.onSurface,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "By $authorName",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = colors.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDate(announcement.publishDate),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AnnouncementDialog(
    announcement: Announcement?,
    onDismiss: () -> Unit,
    onSave: (title: String, message: String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    var title by remember(announcement) {
        mutableStateOf(announcement?.title.orEmpty())
    }

    var message by remember(announcement) {
        mutableStateOf(announcement?.message.orEmpty())
    }

    var titleWasSubmitted by remember(announcement) {
        mutableStateOf(false)
    }

    val isEditing = announcement != null
    val canSave = title.isNotBlank() && message.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) {
                    "Edit Announcement"
                } else {
                    "New Announcement"
                },
                fontSize = 23.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleWasSubmitted = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Announcement title",
                            fontSize = 16.sp,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                    ),
                    singleLine = true,
                    isError = titleWasSubmitted && title.isBlank(),
                    supportingText = {
                        if (titleWasSubmitted && title.isBlank()) {
                            Text("Title is required")
                        }
                    },
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "Announcement message",
                            fontSize = 16.sp,
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 17.sp,
                        lineHeight = 25.sp,
                    ),
                    minLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    titleWasSubmitted = true

                    if (canSave) {
                        onSave(title.trim(), message.trim())
                    }
                },
            ) {
                Text(
                    text = "Save",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontSize = 17.sp,
                )
            }
        },
        containerColor = colors.surface,
    )
}

private fun formatDate(value: String): String {
    return try {
        LocalDate.parse(value).format(
            DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"),
        )
    } catch (_: Exception) {
        value
    }
}