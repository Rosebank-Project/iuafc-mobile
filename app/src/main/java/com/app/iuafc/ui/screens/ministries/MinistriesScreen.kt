package com.app.iuafc.ui.screens.ministries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.DropdownField
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.screens.features.MinistriesViewModel

/** Ministry directory. Administrators can create and edit entries. */
@Composable
fun MinistriesScreen(
    onBack: () -> Unit,
    canManage: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MinistriesViewModel = viewModel(factory = MinistriesViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Ministry?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var saveVersionOnOpen by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.saveVersion) {
        if (showEditor && state.saveVersion > saveVersionOnOpen) showEditor = false
    }

    ScreenScaffold(
        title = "Ministries",
        modifier = modifier,
        onBack = onBack,
        floatingActionButton = {
            if (canManage) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editing = null
                        saveVersionOnOpen = state.saveVersion
                        showEditor = true
                    },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Add ministry") },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null && state.ministries.isEmpty() ->
                ErrorState(state.errorMessage!!, Modifier.padding(padding), onRetry = viewModel::load)
            state.ministries.isEmpty() -> EmptyState(
                title = "No ministries yet",
                message = "Add a ministry to get started.",
                modifier = Modifier.padding(padding),
            )
            else -> Column(Modifier.padding(padding).fillMaxSize()) {
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
                Text(
                    "${state.ministries.size} ministries",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.ministries, key = { it.id }) { ministry ->
                        MinistryCard(ministry, if (canManage) {
                            {
                                editing = ministry
                                saveVersionOnOpen = state.saveVersion
                                showEditor = true
                            }
                        } else null)
                    }
                }
            }
        }
    }

    if (showEditor) {
        MinistryEditor(
            ministry = editing,
            saving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = { showEditor = false },
            onSave = viewModel::save,
        )
    }
}

@Composable
private fun MinistryCard(ministry: Ministry, onEdit: (() -> Unit)?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(ministry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(ministry.description.ifBlank { "No description provided." }, style = MaterialTheme.typography.bodyMedium)
            val leaderName = MockData.users.firstOrNull { it.id == ministry.leaderId }?.fullName
            Text(
                "Leader: ${leaderName ?: "Not assigned"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onEdit != null) {
                TextButton(onClick = onEdit) { Text("Edit ministry") }
            }
        }
    }
}

@Composable
private fun MinistryEditor(
    ministry: Ministry?,
    saving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Ministry) -> Unit,
) {
    var name by remember(ministry?.id) { mutableStateOf(ministry?.name.orEmpty()) }
    var description by remember(ministry?.id) { mutableStateOf(ministry?.description.orEmpty()) }
    var leaderId by remember(ministry?.id) { mutableStateOf(ministry?.leaderId) }
    val leaders = remember { MockData.users.filter { it.role == Role.MINISTRY_LEADER } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ministry == null) "Add ministry" else "Edit ministry") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = "Ministry leader",
                    options = listOf<UserProfile?>(null) + leaders,
                    selected = leaders.firstOrNull { it.id == leaderId },
                    onSelected = { leaderId = it?.id },
                    optionLabel = { it?.fullName ?: "Not assigned" },
                    placeholder = "Not assigned",
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(Ministry(ministry?.id.orEmpty(), name, description, leaderId))
                    }
                },
                enabled = !saving && name.isNotBlank(),
            ) { Text(if (saving) "Saving..." else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } },
    )
}
