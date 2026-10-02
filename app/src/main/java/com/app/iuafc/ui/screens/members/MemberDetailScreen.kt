package com.app.iuafc.ui.screens.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Member
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.components.toDisplayDate

@Composable
fun MemberDetailScreen(
    onBack: () -> Unit,
    canManage: Boolean,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberDetailViewModel = viewModel(factory = MemberDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }

    val member = state.member
    ScreenScaffold(
        title = "Member details",
        modifier = modifier,
        onBack = onBack,
        actions = {
            if (canManage && member != null) {
                IconButton(onClick = { onEdit(member.id) }) { Icon(Icons.Outlined.Edit, contentDescription = "Edit member") }
                IconButton(onClick = { confirmDelete = true }, enabled = !state.isDeleting) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Remove member")
                }
            }
        },
    ) { padding ->
        val contentModifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingState(contentModifier)
            member == null -> ErrorState(state.errorMessage ?: "Member not found.", contentModifier, onRetry = viewModel::load)
            else -> MemberDetails(member, state.ministryName, state.errorMessage, contentModifier)
        }
    }

    if (confirmDelete && member != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Remove member?") },
            text = { Text("${member.fullName} will be removed from the church records. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete() }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MemberDetails(member: Member, ministryName: String?, errorMessage: String?, modifier: Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Initials(member.fullName, size = 64)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(member.fullName, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Member since ${member.joinDate.toDisplayDate()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Card(Modifier.fillMaxWidth()) {
            DetailRow(Icons.Outlined.Phone, "Contact number", member.contactNumber)
            HorizontalDivider()
            DetailRow(Icons.Outlined.Email, "Email", member.email)
            HorizontalDivider()
            DetailRow(Icons.Outlined.Home, "Address", member.address)
        }

        Card(Modifier.fillMaxWidth()) {
            DetailRow(Icons.Outlined.Badge, "Gender", member.gender.name)
            HorizontalDivider()
            DetailRow(Icons.Outlined.Cake, "Date of birth", member.dateOfBirth.toDisplayDate())
            HorizontalDivider()
            DetailRow(Icons.Outlined.VolunteerActivism, "Ministry", ministryName ?: "No ministry")
            HorizontalDivider()
            DetailRow(Icons.Outlined.CalendarMonth, "Join date", member.joinDate.toDisplayDate())
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    ListItem(
        overlineContent = { Text(label) },
        headlineContent = { Text(value) },
        leadingContent = { Icon(icon, contentDescription = null) },
    )
}
