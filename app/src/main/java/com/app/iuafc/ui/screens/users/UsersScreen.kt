package com.app.iuafc.ui.screens.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.DropdownField
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.screens.members.Initials

/** User accounts and roles (Administrator only). */
@Composable
fun UsersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel(factory = UsersViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    ScreenScaffold(
        title = "Users",
        modifier = modifier,
        onBack = onBack,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage!!, onRetry = viewModel::load)
                else -> {
                    UsersHeader(state, viewModel)
                    val users = state.visibleUsers
                    if (users.isEmpty()) {
                        EmptyState("No users found", message = "Try a different name or role.", icon = Icons.Outlined.SearchOff)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(users, key = { it.id }) { user ->
                                UserCard(user, state) { viewModel.startEditing(user) }
                            }
                        }
                    }
                }
            }
        }
    }

    state.editing?.let { edit ->
        RoleDialog(edit, state.members, state.ministries, viewModel)
    }
}

@Composable
private fun UsersHeader(state: UsersUiState, viewModel: UsersViewModel) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                "Accounts are created when people register or sign in with Google. Tap a user to change their role.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    OutlinedTextField(
        value = state.query,
        onValueChange = viewModel::onQueryChange,
        placeholder = { Text("Search by name, email or role") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (state.query.isNotEmpty()) {
                IconButton(onClick = { viewModel.onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = state.roleFilter == null,
            onClick = { viewModel.onRoleFilterChange(null) },
            label = { Text("All (${state.users.size})") },
        )
        Role.entries.forEach { role ->
            FilterChip(
                selected = state.roleFilter == role,
                onClick = { viewModel.onRoleFilterChange(role) },
                label = { Text("${role.displayName} (${state.roleCounts[role] ?: 0})") },
            )
        }
    }
}

@Composable
private fun UserCard(user: UserProfile, state: UsersUiState, onClick: () -> Unit) {
    val link = when (user.role) {
        Role.MINISTRY_LEADER -> state.ministries.firstOrNull { it.id == user.ministryId }?.name
        Role.CHURCH_MEMBER -> state.members.firstOrNull { it.id == user.memberId }?.let { "Member record: ${it.fullName}" }
        else -> null
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Initials(user.fullName)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (link != null) {
                    Text(link, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(8.dp))
            RoleBadge(user.role)
        }
    }
}

@Composable
private fun RoleBadge(role: Role) {
    val (container, content) = when (role) {
        Role.ADMINISTRATOR -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        Role.PASTOR -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = container, contentColor = content, shape = MaterialTheme.shapes.small) {
        Text(role.displayName, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
private fun RoleDialog(edit: RoleEdit, members: List<Member>, ministries: List<Ministry>, viewModel: UsersViewModel) {
    AlertDialog(
        onDismissRequest = { if (!edit.isSaving) viewModel.cancelEditing() },
        title = { Text("Change role") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(edit.user.fullName, style = MaterialTheme.typography.titleSmall)
                Column(Modifier.selectableGroup().padding(vertical = 8.dp)) {
                    Role.entries.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = edit.role == role,
                                    onClick = { viewModel.onEditRoleChange(role) },
                                    role = SemanticsRole.RadioButton,
                                    enabled = !edit.isSaving,
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = edit.role == role, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(role.displayName)
                        }
                    }
                }
                when (edit.role) {
                    Role.MINISTRY_LEADER -> DropdownField(
                        label = "Ministry they lead",
                        options = ministries,
                        selected = ministries.firstOrNull { it.id == edit.ministryId },
                        onSelected = { viewModel.onEditMinistryChange(it.id) },
                        optionLabel = { it.name },
                        enabled = !edit.isSaving,
                    )
                    Role.CHURCH_MEMBER -> DropdownField(
                        label = "Member record (optional)",
                        options = listOf<Member?>(null) + members,
                        selected = members.firstOrNull { it.id == edit.memberId },
                        onSelected = { viewModel.onEditMemberChange(it?.id) },
                        optionLabel = { it?.fullName ?: "Not linked" },
                        placeholder = "Not linked",
                        enabled = !edit.isSaving,
                    )
                    else -> Unit
                }
                edit.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = viewModel::saveRole, enabled = !edit.isSaving) {
                Text(if (edit.isSaving) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::cancelEditing, enabled = !edit.isSaving) { Text("Cancel") }
        },
    )
}
